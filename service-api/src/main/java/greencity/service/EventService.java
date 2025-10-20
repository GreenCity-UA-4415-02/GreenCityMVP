package greencity.service;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddEventDtoResponse;
import greencity.dto.event.EventDto;
import greencity.dto.event.UpdateEventDtoRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {
    AddEventDtoResponse create(AddEventDtoRequest request, List<MultipartFile> images, String userEmail);

    List<EventDto> getVisibleEvents(String userEmail);

    void deleteEvent(Long eventId, String userEmail);

    AddEventDtoResponse update(Long eventId, UpdateEventDtoRequest request, List<MultipartFile> images, String userEmail);

}
