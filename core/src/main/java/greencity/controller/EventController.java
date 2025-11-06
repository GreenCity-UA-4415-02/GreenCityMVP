package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.event.*;
import greencity.dto.user.UserVO;
import greencity.enums.EventStatus;
import greencity.enums.EventType;
import greencity.exception.exceptions.BadRequestException;
import greencity.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create new event with optional images (up to 5 JPG/PNG files)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST)
    })
    public ResponseEntity<AddEventDtoResponse> createEvent(
            @Valid @RequestPart("event") AddEventDtoRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Principal principal) {
        AddEventDtoResponse response = eventService.create(request, images, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/visible")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EventDto>> getVisibleEvents(@AuthenticationPrincipal Principal principal) {
        List<EventDto> events = eventService.getVisibleEvents(principal.getName());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/myEvents")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get events that the authenticated user has joined")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<EventPreviewDto>> getMyEvents(
            @Parameter(hidden = true) @CurrentUser UserVO currentUser,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable,
            @Parameter(description = "Filter by event type: ONLINE, PLACE, BOTH")
            @RequestParam(value = "eventType", required = false) EventType eventType,
            @Parameter(description = "Filter by status: UPCOMING, LIVE, PASSED")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "User latitude for distance-based sorting (for PLACE events)")
            @RequestParam(value = "userLatitude", required = false) Double userLatitude,
            @Parameter(description = "User longitude for distance-based sorting (for PLACE events)")
            @RequestParam(value = "userLongitude", required = false) Double userLongitude) {

        validateUser(currentUser);

        Page<EventPreviewDto> events = eventService.getMyEvents(
                currentUser.getId(), eventType, parseEventStatus(status), userLatitude, userLongitude, pageable);

        return ResponseEntity.ok(events);
    }

    private EventStatus parseEventStatus(String status) {
        if (status == null) {
            return null;
        }
        try {
            return EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid event status: " + status + ". Allowed values are UPCOMING, LIVE, PASSED.");
        }
    }

    @GetMapping("/myEvents/createdEvents")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get events created by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<EventPreviewDto>> getMyCreatedEvents(
            @Parameter(hidden = true) @CurrentUser UserVO currentUser,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable,
            @Parameter(description = "Filter by status: UPCOMING, LIVE, PASSED")
            @RequestParam(value = "status", required = false) String status) {

        validateUser(currentUser);

        Page<EventPreviewDto> events = eventService.getMyCreatedEvents(currentUser.getId(), parseEventStatus(status), pageable);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/myEvents/relatedEvents")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get all events related to the authenticated user (created and joined)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<EventPreviewDto>> getRelatedEvents(
            @Parameter(hidden = true) @CurrentUser UserVO currentUser,
            @Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable,
            @Parameter(description = "Filter by status: UPCOMING, LIVE, PASSED")
            @RequestParam(value = "status", required = false) String status) {
        validateUser(currentUser);

        Page<EventPreviewDto> events = eventService.getRelatedEvents(currentUser.getId(), parseEventStatus(status), pageable);

        return ResponseEntity.ok(events);
    }

    private void validateUser(UserVO currentUser) {
        if (currentUser == null) {
            throw new BadRequestException("User must be authenticated.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Delete event by id")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal Principal principal) {
        eventService.deleteEvent(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Edit existing event (organizer or admin only).")
    public ResponseEntity<AddEventDtoResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestPart("event") UpdateEventDtoRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Principal principal) {
        AddEventDtoResponse response = eventService.update(id, request, images, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/removeAttender/{eventId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Remove an attender from the event",
            description = "Cancel attendance for an upcoming or live event. Cannot cancel attendance for passed events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    public ResponseEntity<Map<String, Object>> removeAttender(
            @Parameter(description = "Event ID") @PathVariable Long eventId,
            @Parameter(hidden = true) @CurrentUser UserVO currentUser) {
        validateUser(currentUser);

        boolean removed = eventService.removeAttender(eventId, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("removed", removed);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/addAttender/{eventId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Add an attender to the event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    public ResponseEntity<Map<String, Object>> addAttender(
            @PathVariable Long eventId,
            @Parameter(hidden = true) @CurrentUser UserVO currentUser) {
        validateUser(currentUser);

        boolean added = eventService.addAttender(eventId, currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("added", added);

        return ResponseEntity.ok(response);
    }
}
