package greencity.service;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddEventDtoResponse;
import greencity.dto.event.DateLocationDto;
import greencity.dto.event.EventDto;
import greencity.entity.*;
import greencity.exception.exceptions.BadRequestException;
import greencity.repository.EventRepo;
import greencity.repository.UserRepo;
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

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isOpen(request.getOpen())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<EventDateLocation> dateLocations = request.getDatesLocations().stream()
                .map(dl -> EventDateLocation.builder()
                        .startDate(dl.getStartDate())
                        .finishDate(dl.getFinishDate())
                        .latitude(dl.getLatitude())
                        .longitude(dl.getLongitude())
                        .onlineLink(dl.getOnlineLink())
                        .event(event)
                        .build())
                .collect(Collectors.toList());
        event.setDateTimeLocations(dateLocations);

        List<EventImage> eventImages = (images == null ? Collections.emptyList() :
                images.stream()
                        .map(file -> EventImage.builder()
                                .imagePath(fileService.upload(file))
                                .isMain(false)
                                .createdAt(LocalDateTime.now())
                                .event(event)
                                .build())
                        .collect(Collectors.toList()));
        event.setImages(eventImages);

        Event saved = eventRepo.save(event);

        return AddEventDtoResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .open(saved.getIsOpen())
                .datesLocations(request.getDatesLocations())
                .images(saved.getImages().stream().map(EventImage::getImagePath).toList())
                .tagNames(request.getTags().stream().map(t -> t.getNameUa()).toList())
                .build();
    }

    @Override
    public List<EventDto> getVisibleEvents(String userEmail) {
        return eventRepo.findAllOpenEvents().stream()
                .map(this::mapToEventDto)
                .toList();
    }


    private EventDto mapToEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .is_open(event.getIsOpen())
                .created_at(event.getCreatedAt())
                //.images(event.getImages().stream().map(EventImage::getImagePath).toList())
                .build();
    }

    private void validateRequest(AddEventDtoRequest dto, List<MultipartFile> images) {
        if (dto.getTitle().length() > 70)
            throw new BadRequestException("Title must be ≤ 70 chars");

        int descLen = dto.getDescription() != null ? dto.getDescription().length() : 0;
        if (descLen < 20 || descLen > 63206)
            throw new BadRequestException("Description must be between 20 and 63206 chars");

        if (dto.getDatesLocations().isEmpty() || dto.getDatesLocations().size() > 7)
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
