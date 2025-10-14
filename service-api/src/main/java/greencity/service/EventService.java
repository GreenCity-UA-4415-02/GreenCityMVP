package greencity.service;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddEventDtoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {
    AddEventDtoResponse create(AddEventDtoRequest request, List<MultipartFile> images, String userEmail);
}
