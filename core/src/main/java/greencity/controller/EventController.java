package greencity.controller;

import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddEventDtoResponse;
import greencity.dto.event.EventDto;
import greencity.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create new event with optional images (up to 5 JPG/PNG files)")
    public ResponseEntity<AddEventDtoResponse> createEvent(
            @Valid @RequestPart("event") AddEventDtoRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Principal principal
    ) {
        AddEventDtoResponse response = eventService.create(request, images, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/visible")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EventDto>> getVisibleEvents(@AuthenticationPrincipal Principal principal) {
        List<EventDto> events = eventService.getVisibleEvents(principal.getName());
        return ResponseEntity.ok(events);
    }
}

