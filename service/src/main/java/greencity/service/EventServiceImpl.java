package greencity.service;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddEventDtoResponse;
import greencity.entity.Event;
import greencity.entity.EventDateLocation;
import greencity.entity.User;
import greencity.exception.exceptions.BadRequestException;
import greencity.repository.EventRepo;
import greencity.repository.UserRepo;
import greencity.service.FileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final FileService fileService;

    @Override
    @Transactional
    public AddEventDtoResponse create(AddEventDtoRequest request, List<MultipartFile> images, String email) {
        validateRequest(request, images);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        List<String> uploadedImages = (images == null || images.isEmpty())
                ? Collections.emptyList()
                : images.stream()
                .map(fileService::upload)
                .collect(Collectors.toList());

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .open(request.getOpen())
                .images(uploadedImages)
                .createdBy(user)
                .build();

        List<EventDateLocation> dateLocations = request.getDatesLocations().stream()
                .map(dl -> EventDateLocation.builder()
                        .startDate(dl.getStartDate())
                        .finishDate(dl.getFinishDate())
                        .address(dl.getAddress())
                        .event(event)
                        .build())
                .collect(Collectors.toList());

        event.setDatesLocations(dateLocations);
        Event saved = eventRepo.save(event);

        return AddEventDtoResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .open(saved.getOpen())
                .images(saved.getImages())
                .datesLocations(request.getDatesLocations())
                .tagNames(request.getTags().stream().map(t -> t.getNameUa()).toList())
                .build();
    }

    private void validateRequest(AddEventDtoRequest dto, List<MultipartFile> images) {
        if (dto.getTitle().length() > 70)
            throw new BadRequestException("Title must be ≤ 70 chars");

        int descLen = dto.getDescription() != null ? dto.getDescription().length() : 0;
        if (descLen < 20 || descLen > 63206)
            throw new BadRequestException("Description must be between 20 and 63206 chars");

        if (dto.getDatesLocations().size() < 1 || dto.getDatesLocations().size() > 7)
            throw new BadRequestException("Must have 1–7 date/time pairs");

        if (dto.getDatesLocations().stream().anyMatch(d -> d.getStartDate().isBefore(LocalDateTime.now())))
            throw new BadRequestException("Dates must be in the future");

        if (images != null) {
            if (images.size() > 5)
                throw new BadRequestException("Maximum 5 images allowed");

            for (MultipartFile file : images) {
                String type = Objects.requireNonNull(file.getContentType());
                if (!(type.equals("image/jpeg") || type.equals("image/png")))
                    throw new BadRequestException("Only JPG or PNG allowed");

                if (file.getSize() > 10 * 1024 * 1024)
                    throw new BadRequestException("Image size ≤ 10 MB");
            }
        }
    }
}
