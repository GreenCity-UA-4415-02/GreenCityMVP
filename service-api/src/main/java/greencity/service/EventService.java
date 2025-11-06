package greencity.service;

import greencity.dto.event.*;
import greencity.dto.user.UserVO;
import greencity.enums.EventStatus;
import greencity.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface EventService {
    // EventDto
    AddEventDtoResponse create(AddEventDtoRequest request, List<MultipartFile> images, String userEmail);

    List<EventDto> getVisibleEvents(String userEmail);

    void deleteEvent(Long eventId, String userEmail);

    // EventDto
    AddEventDtoResponse update(Long eventId, UpdateEventDtoRequest request, List<MultipartFile> images,
        String userEmail);

    EventDto getEventById(Long eventId);

    Page<EventPreviewDto> getMyEvents(Long userId, EventType eventType, EventStatus status,
                                      Double userLatitude, Double userLongitude, Pageable pageable);

    Page<EventPreviewDto> getMyCreatedEvents(Long userId, EventStatus status, Pageable pageable);

    Page<EventPreviewDto> getRelatedEvents(Long userId, EventStatus status, Pageable pageable);

    boolean addAttender(Long eventId, UserVO user);

    boolean removeAttender(Long eventId, UserVO user);
}
