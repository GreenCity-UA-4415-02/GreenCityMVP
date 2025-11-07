package greencity.service;

import greencity.dto.event.*;
import greencity.dto.user.UserVO;
import greencity.entity.*;
import greencity.enums.EventStatus;
import greencity.enums.EventType;
import greencity.enums.Role;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.EventAttenderRepo;
import greencity.repository.EventRepo;
import greencity.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import greencity.dto.event.EventNotificationDto;
import greencity.dto.event.EventType;
import greencity.dto.event.EventUpdatePayload;
import greencity.dto.event.EventActionType;
import reactor.core.publisher.Sinks;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final FileService fileService;
    private final EventNotificationProducer notificationProducer;
    private final Sinks.Many<EventUpdatePayload> eventUpdateSink;
    private final EventAttenderRepo eventAttenderRepo;
    private final UserService userService;

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
            .organizer(user)
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

        List<EventImage> eventImages = new ArrayList<>();
        List<String> uploadedPaths = new ArrayList<>();

        if (images != null && !images.isEmpty()) {
            boolean first = true;
            for (MultipartFile file : images) {
                String url;
                try {
                    url = fileService.upload(file);
                    uploadedPaths.add(url);
                } catch (RuntimeException ex) {
                    uploadedPaths.forEach(fileService::delete);
                    throw ex;
                }
                eventImages.add(EventImage.builder()
                    .imagePath(url)
                    .isMain(first)
                    .createdAt(LocalDateTime.now())
                    .event(event)
                    .build());
                first = false;
            }
        } else {
            MultipartFile defaultFile = fileService.convertToMultipartImage("tempImage.png");
            String defaultUrl = fileService.upload(defaultFile);
            uploadedPaths.add(defaultUrl);
            eventImages.add(EventImage.builder()
                .imagePath(defaultUrl)
                .isMain(true)
                .createdAt(LocalDateTime.now())
                .event(event)
                .build());
        }

        event.setImages(eventImages);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    uploadedPaths.forEach(fileService::delete);
                }
            }
        });

        Event saved = eventRepo.save(event);

        notificationProducer.sendNotification(EventNotificationDto.builder()
                .eventId(saved.getId())
                .eventTitle(saved.getTitle())
                .organizerEmail(saved.getOrganizer().getEmail())
                .organizerName(saved.getOrganizer().getName())
                .eventType(EventType.CREATED)
                .build());

        eventUpdateSink.tryEmitNext(EventUpdatePayload.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .eventType(EventActionType.CREATED)
                .build());

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

    @Override
    @Transactional
    public Page<EventPreviewDto> getMyEvents(Long userId, EventType eventType, EventStatus status,
        Double userLatitude, Double userLongitude, Pageable pageable) {
        LocalDateTime currentTime = LocalDateTime.now();

        Page<Event> events;

        if (eventType != null && eventType != EventType.BOTH) {
            Double latitude = (userLatitude != null) ? userLatitude : 0.0;
            Double longitude = (userLongitude != null) ? userLongitude : 0.0;

            events = eventAttenderRepo.findJoinedEventsWithSorting(
                userId, currentTime, eventType.name(), latitude, longitude, pageable);
        } else {
            events = eventAttenderRepo.findJoinedEventsDefaultSorting(
                userId, currentTime, pageable);
        }

        UserVO currentUser = userService.findById(userId);
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        List<EventPreviewDto> eventPreviews = events.getContent().stream()
            .map(event -> toEventPreviewDtoWithContext(event, userId, isAdmin, userLatitude, userLongitude, eventType))
            .filter(event -> status == null || event.getStatus() == status)
            .collect(Collectors.toList());

        return new PageImpl<>(eventPreviews, pageable, events.getTotalElements());
    }

    @Override
    @Transactional
    public Page<EventPreviewDto> getMyCreatedEvents(Long userId, EventStatus status, Pageable pageable) {
        Page<Event> events = eventRepo.findByOrganizerIdOrderByNearestStart(userId, pageable);

        UserVO currentUser = userService.findById(userId);
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        List<EventPreviewDto> eventPreviews = events.getContent().stream()
            .map(event -> toEventPreviewDtoWithCanEdit(event, userId, isAdmin))
            .filter(event -> status == null || event.getStatus() == status)
            .collect(Collectors.toList());

        return new PageImpl<>(eventPreviews, pageable, events.getTotalElements());
    }

    @Override
    @Transactional
    public Page<EventPreviewDto> getRelatedEvents(Long userId, EventStatus status, Pageable pageable) {
        Page<Event> events = eventRepo.findRelatedEventsByUserId(userId, pageable);

        UserVO currentUser = userService.findById(userId);
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        List<EventPreviewDto> eventPreviews = events.getContent().stream()
            .map(event -> toEventPreviewDtoWithCanEdit(event, userId, isAdmin))
            .filter(event -> status == null || event.getStatus() == status)
            .collect(Collectors.toList());

        return new PageImpl<>(eventPreviews, pageable, events.getTotalElements());
    }

    private EventPreviewDto toEventPreviewDtoWithCanEdit(Event event, Long currentUserId, boolean isAdmin) {
        return toEventPreviewDtoWithContext(event, currentUserId, isAdmin, null, null, null);
    }

    private EventPreviewDto toEventPreviewDtoWithContext(
        Event event,
        Long currentUserId,
        boolean isAdmin,
        Double userLatitude,
        Double userLongitude,
        EventType eventType) {
        EventStatusCalculator.EventStatusResult statusResult =
            EventStatusCalculator.computeStatus(event.getDateTimeLocations(), LocalDateTime.now());

        boolean isOrganizer = currentUserId != null && event.getOrganizer().getId() != null
            && event.getOrganizer().getId().equals(currentUserId);
        boolean canEdit = (isOrganizer || isAdmin) && statusResult.getStatus() != EventStatus.PASSED;
        boolean canCancelJoin = statusResult.getStatus() != EventStatus.LIVE
            && statusResult.getStatus() != EventStatus.PASSED;
        boolean hasPlace = event.getDateTimeLocations().stream()
            .anyMatch(loc -> loc.getLatitude() != null && loc.getLongitude() != null);
        boolean hasOnline = event.getDateTimeLocations().stream()
            .anyMatch(loc -> loc.getOnlineLink() != null && !loc.getOnlineLink().isBlank());

        EventTypesDto types = EventTypesDto.builder()
            .place(hasPlace)
            .online(hasOnline)
            .build();

        Double distance = computeMinDistanceKm(event, userLatitude, userLongitude,
            eventType != null ? eventType : EventType.BOTH);

        String titleImage = event.getImages().stream()
            .filter(EventImage::getIsMain)
            .findFirst()
            .map(EventImage::getImagePath)
            .orElse(null);

        return EventPreviewDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .titleImage(titleImage)
            .status(statusResult.getStatus())
            .nearestStart(statusResult.getNearestStart())
            .nearestFinish(statusResult.getNearestFinish())
            .types(types)
            .distance(distance)
            .visibility(event.getIsOpen() ? "open" : "closed")
            .canCancelJoin(canCancelJoin)
            .canEdit(canEdit)
            .isFavourite(false)
            .isSubscribed(false)
            .isOrganizer(isOrganizer)
            .build();
    }

    private Double computeMinDistanceKm(Event event, Double userLatitude, Double userLongitude, EventType eventType) {
        if (userLatitude == null || userLongitude == null) {
            return null;
        }
        if (eventType != EventType.PLACE) {
            return null;
        }
        List<EventDateLocation> locations = event.getDateTimeLocations();
        if (locations == null || locations.isEmpty()) {
            return null;
        }
        Double min = null;
        for (EventDateLocation loc : locations) {
            if (loc.getLatitude() == null || loc.getLongitude() == null) {
                continue;
            }
            double d = haversineKm(userLatitude, userLongitude, loc.getLatitude(), loc.getLongitude());
            if (min == null || d < min) {
                min = d;
            }
        }
        return min;
    }

    private double haversineKm(double lat1, double lon1, BigDecimal lat2, BigDecimal lon2) {
        double rad = 6371.0;
        double diffLat = Math.toRadians(lat2.doubleValue() - lat1);
        double diffLon = Math.toRadians(lon2.doubleValue() - lon1);
        double a = Math.sin(diffLat / 2) * Math.sin(diffLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(diffLon / 2) * Math.sin(diffLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return rad * c;
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId, String userEmail) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new BadRequestException("Event not found"));

        User currentUser = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new BadRequestException("User not found"));
        boolean isAdmin = Role.ROLE_ADMIN.equals(currentUser.getRole());
        boolean isOrganizer = event.getOrganizer() != null
            && event.getOrganizer().getEmail() != null
            && event.getOrganizer().getEmail().equalsIgnoreCase(userEmail);
        if (!(isAdmin || isOrganizer)) {
            throw new BadRequestException("Only organizer or admin can delete event");
        }

        EventNotificationDto notification = EventNotificationDto.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .organizerEmail(event.getOrganizer().getEmail())
                .organizerName(event.getOrganizer().getName())
                .eventType(EventType.DELETED)
                .build();

        List<String> paths = event.getImages() == null
            ? List.of()
            : event.getImages().stream().map(EventImage::getImagePath).toList();

        eventUpdateSink.tryEmitNext(EventUpdatePayload.builder()
                .id(event.getId())
                .title(event.getTitle())
                .eventType(EventActionType.DELETED)
                .build());

        eventRepo.delete(event);

        notificationProducer.sendNotification(notification);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                paths.forEach(fileService::delete);
            }
        });
    }

    @Override
    @Transactional
    public AddEventDtoResponse update(Long eventId, UpdateEventDtoRequest request, List<MultipartFile> images,
        String userEmail) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new BadRequestException("Event not found"));

        User currentUser = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new BadRequestException("User not found"));

        boolean isAdmin = Role.ROLE_ADMIN.equals(currentUser.getRole());
        boolean isOrganizer = event.getOrganizer() != null
            && event.getOrganizer().getEmail() != null
            && event.getOrganizer().getEmail().equalsIgnoreCase(userEmail);

        if (!(isAdmin || isOrganizer)) {
            throw new BadRequestException("Only organizer or admin can edit event");
        }

        boolean hasFutureDates = event.getDateTimeLocations().stream()
            .anyMatch(dl -> dl.getFinishDate().isAfter(LocalDateTime.now()));
        if (!hasFutureDates) {
            throw new BadRequestException("Past events cannot be edited");
        }

        validateRequest(new AddEventDtoRequest(
            request.getTitle(),
            request.getDescription(),
            request.getOpen(),
            request.getTags(),
            request.getDatesLocations()), images);

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setIsOpen(request.getOpen());
        event.setUpdatedAt(LocalDateTime.now());

        event.getDateTimeLocations().clear();
        List<EventDateLocation> updatedDates = request.getDatesLocations().stream()
            .map(dl -> EventDateLocation.builder()
                .startDate(dl.getStartDate())
                .finishDate(dl.getFinishDate())
                .latitude(dl.getLatitude())
                .longitude(dl.getLongitude())
                .onlineLink(dl.getOnlineLink())
                .event(event)
                .build())
            .toList();
        event.getDateTimeLocations().addAll(updatedDates);

        List<String> oldPaths = event.getImages() == null
            ? List.of()
            : event.getImages().stream().map(EventImage::getImagePath).toList();

        if (images != null && !images.isEmpty()) {
            if (event.getImages() != null) {
                event.getImages().clear();
            }

            boolean first = true;
            List<EventImage> updatedImages = new ArrayList<>();
            for (MultipartFile file : images) {
                String url;
                try {
                    url = fileService.upload(file);
                } catch (RuntimeException ex) {
                    oldPaths.forEach(fileService::delete);
                    throw ex;
                }
                updatedImages.add(EventImage.builder()
                    .imagePath(url)
                    .isMain(first)
                    .createdAt(LocalDateTime.now())
                    .event(event)
                    .build());
                first = false;
            }
            event.setImages(updatedImages);
        }

        Event saved = eventRepo.save(event);

        notificationProducer.sendNotification(EventNotificationDto.builder()
                .eventId(saved.getId())
                .eventTitle(saved.getTitle())
                .organizerEmail(saved.getOrganizer().getEmail())
                .organizerName(saved.getOrganizer().getName())
                .eventType(EventType.EDITED)
                .build());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                oldPaths.forEach(fileService::delete);
            }
        });

        eventUpdateSink.tryEmitNext(EventUpdatePayload.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .eventType(EventActionType.EDITED)
                .build());

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

    private EventDto mapToEventDto(Event event) {
        return EventDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .isOpen(event.getIsOpen())
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .build();
    }

    private EventDto toEventDto(Event event) {
        List<EventDateLocationDto> dateDtos = event.getDateTimeLocations().stream()
            .map(loc -> EventDateLocationDto.builder()
                .startDate(loc.getStartDate())
                .finishDate(loc.getFinishDate())
                .latitude(loc.getLatitude())
                .longitude(loc.getLongitude())
                .onlineLink(loc.getOnlineLink())
                .build())
            .toList();

        List<String> imageUrls = event.getImages().stream()
            .map(EventImage::getImagePath)
            .toList();

        EventStatusCalculator.EventStatusResult statusResult =
            EventStatusCalculator.computeStatus(event.getDateTimeLocations(), LocalDateTime.now());

        return EventDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .isOpen((event.getIsOpen()))
            .organizerId(event.getOrganizer().getId())
            .titleImage(event.getImages().stream()
                .filter(EventImage::getIsMain)
                .findFirst()
                .map(EventImage::getImagePath)
                .orElse(null))
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .datesLocations(dateDtos)
            .imageUrls(imageUrls)
            .status(statusResult.getStatus())
            .nearestStart(statusResult.getNearestStart())
            .nearestFinish(statusResult.getNearestFinish())
            .build();
    }

    private void validateRequest(AddEventDtoRequest dto, List<MultipartFile> images) {
        if (dto.getTitle().length() > 70) {
            throw new BadRequestException("Title must be ≤ 70 chars");
        }

        int descLen = dto.getDescription() != null ? dto.getDescription().length() : 0;
        if (descLen < 20 || descLen > 63206) {
            throw new BadRequestException("Description must be between 20 and 63206 chars");
        }

        if (dto.getDatesLocations().isEmpty() || dto.getDatesLocations().size() > 7) {
            throw new BadRequestException("Must have 1–7 date/time pairs");
        }

        if (dto.getDatesLocations().stream().anyMatch(d -> d.getStartDate().isBefore(LocalDateTime.now()))) {
            throw new BadRequestException("Dates must be in the future");
        }

        if (images != null) {
            if (images.size() > 5) {
                throw new BadRequestException("Maximum 5 images allowed");
            }

            for (MultipartFile file : images) {
                String type = Objects.requireNonNull(file.getContentType());
                if (!(type.equals("image/jpeg") || type.equals("image/png"))) {
                    throw new BadRequestException("Only JPG or PNG allowed");
                }

                if (file.getSize() > 10 * 1024 * 1024) {
                    throw new BadRequestException("Image size ≤ 10 MB");
                }
            }
        }
    }

    @Override
    public EventDto getEventById(Long eventId) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        return mapToEventDto(event);
    }

    @Override
    @Transactional
    public boolean addAttender(Long eventId, UserVO user) {
        eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event doesn't exist by this id: " + eventId));

        if (eventAttenderRepo.existsByEventIdAndUserId(eventId, user.getId())) {
            return false;
        }

        EventAttender attender = EventAttender.builder()
            .eventId(eventId)
            .userId(user.getId())
            .createdAt(LocalDateTime.now())
            .build();

        eventAttenderRepo.save(attender);
        return true;
    }

    @Override
    @Transactional
    public boolean removeAttender(Long eventId, UserVO user) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event doesn't exist by this id: " + eventId));

        EventDto eventDto = toEventDto(event);
        if (eventDto.getStatus() == EventStatus.PASSED) {
            throw new BadRequestException("Cannot cancel attendance for events that have already passed");
        }

        if (!eventAttenderRepo.existsByEventIdAndUserId(eventId, user.getId())) {
            return false;
        }

        int deletedCount = eventAttenderRepo.deleteByEventIdAndUserId(eventId, user.getId());
        return deletedCount > 0;
    }
}
