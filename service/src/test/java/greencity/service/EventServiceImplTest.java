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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class EventServiceImplTest {
    @Mock private EventRepo eventRepo;
    @Mock private UserRepo userRepo;
    @Mock private FileService fileService;
    @Mock private EventAttenderRepo eventAttenderRepo;
    @Mock private UserService userService;

    @InjectMocks
    private EventServiceImpl service;

    // --- Helpers -------------------------------------------------------------

    private static final AtomicLong IDS = new AtomicLong(1);

    private User makeUser(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    private EventDateLocation locFuture(int days) {
        return EventDateLocation.builder()
                .startDate(LocalDateTime.now().plusDays(days))
                .finishDate(LocalDateTime.now().plusDays(days).plusHours(2))
                .latitude(BigDecimal.valueOf(50.4501))
                .longitude(BigDecimal.valueOf(30.5234))
                .build();
    }

    private EventDateLocation locLive() {
        return EventDateLocation.builder()
                .startDate(LocalDateTime.now().minusHours(1))
                .finishDate(LocalDateTime.now().plusHours(1))
                .latitude(BigDecimal.valueOf(50.4501))
                .longitude(BigDecimal.valueOf(30.5234))
                .build();
    }

    private EventDateLocation locPast() {
        return EventDateLocation.builder()
                .startDate(LocalDateTime.now().minusDays(2))
                .finishDate(LocalDateTime.now().minusDays(1))
                .latitude(BigDecimal.valueOf(50.4501))
                .longitude(BigDecimal.valueOf(30.5234))
                .build();
    }

    private EventImage img(String path, boolean main) {
        return EventImage.builder()
                .imagePath(path)
                .isMain(main)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Event eventWith(List<EventDateLocation> dls, List<EventImage> imgs, User organizer, boolean open) {
        Event e = Event.builder()
                .id(IDS.getAndIncrement())
                .title("Title")
                .description("Description 1234567890 1234567890")
                .isOpen(open)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .organizer(organizer)
                .dateTimeLocations(new ArrayList<>(dls))
                .images(new ArrayList<>(imgs))
                .build();
        dls.forEach(dl -> dl.setEvent(e));
        imgs.forEach(i -> i.setEvent(e));
        return e;
    }

    private AddEventDtoRequest addReq(boolean open, List<DateLocationDto> dls) {
        return new AddEventDtoRequest(
                "Green City Workshop",
                "Learn about sustainable living in details --- long enough text",
                open,
                Collections.emptyList(), // tags
                dls
        );
    }

    private UpdateEventDtoRequest updReq(boolean open, List<DateLocationDto> dls) {
        return UpdateEventDtoRequest.builder()
                .title("Updated")
                .description("Updated Description 1234567890 ...")
                .open(open)
                .tags(Collections.emptyList())
                .datesLocations(dls)
                .build();
    }

    private DateLocationDto dlDtoFuture(int days) {
        return DateLocationDto.builder()
                .startDate(LocalDateTime.now().plusDays(days))
                .finishDate(LocalDateTime.now().plusDays(days).plusHours(2))
                .latitude(BigDecimal.valueOf(50.4501))
                .longitude(BigDecimal.valueOf(30.5234))
                .onlineLink(null)
                .build();
    }

    private MultipartFile jpg(String name) {
        return new MockMultipartFile(name, name, "image/jpeg", new byte[]{1,2,3});
    }

    // --- create(...) --------------------------------------------------------

    @Test
    void create_success_withImages() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            String email = "org@example.com";
            User organizer = makeUser(10L, email, Role.ROLE_USER);
            when(userRepo.findByEmail(email)).thenReturn(Optional.of(organizer));
            when(fileService.upload(any())).thenReturn("http://img/1.jpg", "http://img/2.jpg");
            when(eventRepo.save(any(Event.class))).thenAnswer(inv -> {
                Event e = inv.getArgument(0);
                e.setId(42L);
                return e;
            });

            AddEventDtoRequest req = addReq(true, List.of(dlDtoFuture(1)));
            List<MultipartFile> imgs = List.of(jpg("a.jpg"), jpg("b.jpg"));

            AddEventDtoResponse resp = service.create(req, imgs, email);

            assertNotNull(resp);
            assertEquals(42L, resp.getId());
            assertEquals(2, resp.getImages().size());
            verify(fileService, times(2)).upload(any());
            verify(eventRepo).save(any(Event.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void create_success_withoutImages_usesDefault() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            String email = "org@example.com";
            User organizer = makeUser(11L, email, Role.ROLE_USER);
            when(userRepo.findByEmail(email)).thenReturn(Optional.of(organizer));
            MultipartFile defaultFile = jpg("default.jpg");
            when(fileService.convertToMultipartImage("tempImage.png")).thenReturn(defaultFile);
            when(fileService.upload(defaultFile)).thenReturn("http://img/default.jpg");
            when(eventRepo.save(any(Event.class))).thenAnswer(inv -> {
                Event e = inv.getArgument(0);
                e.setId(43L);
                return e;
            });

            AddEventDtoRequest req = addReq(true, List.of(dlDtoFuture(2)));

            AddEventDtoResponse resp = service.create(req, Collections.emptyList(), email);

            assertEquals(43L, resp.getId());
            assertEquals(1, resp.getImages().size());
            assertEquals("http://img/default.jpg", resp.getImages().getFirst());
            verify(fileService).convertToMultipartImage("tempImage.png");
            verify(fileService).upload(defaultFile);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // --- getVisibleEvents(...) ---------------------------------------------

    @Test
    void getVisibleEvents_returnsOpenEventsMapped() {
        User org = makeUser(1L, "a@a", Role.ROLE_USER);
        Event e1 = eventWith(List.of(locFuture(1)), List.of(img("p1", true)), org, true);
        Event e2 = eventWith(List.of(locFuture(2)), List.of(img("p2", true)), org, true);
        when(eventRepo.findAllOpenEvents()).thenReturn(List.of(e1, e2));

        List<EventDto> out = service.getVisibleEvents("ignored@x");

        assertEquals(2, out.size());
        assertEquals(e1.getTitle(), out.getFirst().getTitle());
    }

    @Test
    void getVisibleEvents_empty_whenNoOpenEvents() {
        when(eventRepo.findAllOpenEvents()).thenReturn(Collections.emptyList());
        List<EventDto> out = service.getVisibleEvents("x@y");
        assertTrue(out.isEmpty());
    }

    // --- getMyEvents(...) ---------------------------------------------------

    @Test
    void getMyEvents_defaultSorting_returnsPagedResults() {
        Long userId = 100L;
        Pageable pg = PageRequest.of(0, 10);
        User org = makeUser(10L, "org@x", Role.ROLE_USER);
        Event future = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);

        when(eventAttenderRepo.findJoinedEventsDefaultSorting(eq(userId), any(LocalDateTime.class), eq(pg)))
                .thenReturn(new PageImpl<>(List.of(future), pg, 1));
        when(userService.findById(userId)).thenReturn(UserVO.builder().id(userId).role(Role.ROLE_USER).build());

        Page<EventPreviewDto> res = service.getMyEvents(userId, null, null, null, null, pg);

        assertEquals(1, res.getContent().size());
        assertEquals(future.getId(), res.getContent().getFirst().getId());
    }

    @Test
    void getMyEvents_placeType_computesDistanceAndAppliesStatusFilter() {
        Long userId = 101L;
        Pageable pg = PageRequest.of(0, 10);
        User org = makeUser(20L, "org2@x", Role.ROLE_USER);
        Event future = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);

        when(eventAttenderRepo.findJoinedEventsWithSorting(
                eq(userId), any(LocalDateTime.class), eq(EventType.PLACE.name()),
                anyDouble(), anyDouble(), eq(pg)))
                .thenReturn(new PageImpl<>(List.of(future), pg, 1));
        when(userService.findById(userId)).thenReturn(UserVO.builder().id(userId).role(Role.ROLE_USER).build());

        Page<EventPreviewDto> res = service.getMyEvents(userId, EventType.PLACE, EventStatus.UPCOMING,
                50.45, 30.523, pg);

        assertEquals(1, res.getContent().size());
        assertNotNull(res.getContent().getFirst().getDistance());
    }

    // --- getMyCreatedEvents(...) -------------------------------------------

    @Test
    void getMyCreatedEvents_canEditTrueForOrganizer() {
        Long userId = 200L;
        Pageable pg = PageRequest.of(0, 10);
        User org = makeUser(userId, "org@me", Role.ROLE_USER);
        Event future = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);

        when(eventRepo.findByOrganizerIdOrderByNearestStart(userId, pg))
                .thenReturn(new PageImpl<>(List.of(future), pg, 1));
        when(userService.findById(userId)).thenReturn(UserVO.builder().id(userId).role(Role.ROLE_USER).build());

        Page<EventPreviewDto> res = service.getMyCreatedEvents(userId, null, pg);

        EventPreviewDto dto = res.getContent().getFirst();
        assertTrue(dto.isOrganizer());
        assertTrue(dto.isCanEdit());
    }

    @Test
    void getMyCreatedEvents_statusFilterApplied() {
        Long userId = 201L;
        Pageable pg = PageRequest.of(0, 10);
        User org = makeUser(userId, "me@x", Role.ROLE_USER);
        Event future = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);
        Event past = eventWith(List.of(locPast()), List.of(img("p2", true)), org, true);

        when(eventRepo.findByOrganizerIdOrderByNearestStart(userId, pg))
                .thenReturn(new PageImpl<>(List.of(future, past), pg, 2));
        when(userService.findById(userId)).thenReturn(UserVO.builder().id(userId).role(Role.ROLE_USER).build());

        Page<EventPreviewDto> res = service.getMyCreatedEvents(userId, EventStatus.UPCOMING, pg);

        assertEquals(1, res.getContent().size());
        assertEquals(EventStatus.UPCOMING, res.getContent().getFirst().getStatus());
    }

    // --- getRelatedEvents(...) ---------------------------------------------

    @Test
    void getRelatedEvents_returnsMapped() {
        Long userId = 300L;
        Pageable pg = PageRequest.of(0, 10);
        User org = makeUser(2L, "o@x", Role.ROLE_USER);
        Event e = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);

        when(eventRepo.findRelatedEventsByUserId(userId, pg))
                .thenReturn(new PageImpl<>(List.of(e), pg, 1));
        when(userService.findById(userId)).thenReturn(UserVO.builder().id(userId).role(Role.ROLE_USER).build());

        Page<EventPreviewDto> res = service.getRelatedEvents(userId, null, pg);

        assertEquals(1, res.getContent().size());
        assertEquals(e.getId(), res.getContent().getFirst().getId());
    }

    @Test
    void getRelatedEvents_statusFilterApplied() {
        Long userId = 301L;
        Pageable pg = PageRequest.of(0, 10);
        User org = makeUser(3L, "o@x", Role.ROLE_USER);
        Event e1 = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);
        Event e2 = eventWith(List.of(locPast()), List.of(img("p2", true)), org, true);

        when(eventRepo.findRelatedEventsByUserId(userId, pg))
                .thenReturn(new PageImpl<>(List.of(e1, e2), pg, 2));
        when(userService.findById(userId)).thenReturn(UserVO.builder().id(userId).role(Role.ROLE_USER).build());

        Page<EventPreviewDto> res = service.getRelatedEvents(userId, EventStatus.UPCOMING, pg);

        assertEquals(1, res.getContent().size());
        assertEquals(EventStatus.UPCOMING, res.getContent().getFirst().getStatus());
    }

    // --- deleteEvent(...) ---------------------------------------------------

    @Test
    void deleteEvent_success_asAdmin() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            Long eventId = 10L;
            User org = makeUser(5L, "org@x", Role.ROLE_USER);
            Event e = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);

            when(eventRepo.findById(eventId)).thenReturn(Optional.of(e));
            when(userRepo.findByEmail("admin@x")).thenReturn(Optional.of(makeUser(99L, "admin@x", Role.ROLE_ADMIN)));

            assertDoesNotThrow(() -> service.deleteEvent(eventId, "admin@x"));
            verify(eventRepo).delete(e);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void deleteEvent_unauthorized_throws() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            Long eventId = 11L;
            User org = makeUser(5L, "org@x", Role.ROLE_USER);
            Event e = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);

            when(eventRepo.findById(eventId)).thenReturn(Optional.of(e));
            when(userRepo.findByEmail("user@x")).thenReturn(Optional.of(makeUser(7L, "user@x", Role.ROLE_USER)));

            assertThrows(BadRequestException.class, () -> service.deleteEvent(eventId, "user@x"));
            verify(eventRepo, never()).delete(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // --- update(...) --------------------------------------------------------

    @Test
    void update_success_asOrganizer_withNewImages() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            Long eventId = 20L;
            User org = makeUser(50L, "org@x", Role.ROLE_USER);
            Event existing = eventWith(List.of(locFuture(1)), new ArrayList<>(List.of(img("old1", true))), org, true);

            when(eventRepo.findById(eventId)).thenReturn(Optional.of(existing));
            when(userRepo.findByEmail("org@x")).thenReturn(Optional.of(org));
            when(fileService.upload(any())).thenReturn("http://n/1.jpg", "http://n/2.jpg");
            when(eventRepo.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateEventDtoRequest req = updReq(true, List.of(dlDtoFuture(2)));
            List<MultipartFile> imgs = List.of(jpg("a.jpg"), jpg("b.jpg"));

            AddEventDtoResponse resp = service.update(eventId, req, imgs, "org@x");

            assertNotNull(resp);
            assertEquals(2, resp.getImages().size());
            assertEquals("Updated", resp.getTitle());
            verify(fileService, times(2)).upload(any());
            verify(eventRepo).save(any(Event.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void update_pastEvent_throwsBadRequest() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            Long eventId = 21L;
            User org = makeUser(60L, "org@x", Role.ROLE_USER);
            Event past = eventWith(List.of(locPast()), List.of(img("old", true)), org, true);

            when(eventRepo.findById(eventId)).thenReturn(Optional.of(past));
            when(userRepo.findByEmail("org@x")).thenReturn(Optional.of(org));

            UpdateEventDtoRequest req = updReq(true, List.of(dlDtoFuture(3)));

            assertThrows(BadRequestException.class, () -> service.update(eventId, req, Collections.emptyList(), "org@x"));
            verify(eventRepo, never()).save(any(Event.class));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // --- getEventById(...) --------------------------------------------------

    @Test
    void getEventById_success_mapsMinimalFields() {
        Long id = 30L;
        User org = makeUser(1L, "o@x", Role.ROLE_USER);
        Event e = eventWith(List.of(locFuture(1)), List.of(img("p", true)), org, true);
        e.setId(id);
        when(eventRepo.findById(id)).thenReturn(Optional.of(e));

        EventDto dto = service.getEventById(id);

        assertEquals(id, dto.getId());
        assertEquals(e.getTitle(), dto.getTitle());
        assertEquals(e.getIsOpen(), dto.isOpen());
    }

    @Test
    void getEventById_notFound_throws() {
        when(eventRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getEventById(999L));
    }

    // --- addAttender(...) ---------------------------------------------------

    @Test
    void addAttender_success_whenNotExists() {
        Long eventId = 40L;
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(eventWith(
                List.of(locFuture(1)), List.of(img("p", true)), makeUser(1L,"o@x", Role.ROLE_USER), true)));
        when(eventAttenderRepo.existsByEventIdAndUserId(eq(eventId), eq(7L))).thenReturn(false);

        boolean ok = service.addAttender(eventId, UserVO.builder().id(7L).role(Role.ROLE_USER).build());

        assertTrue(ok);
        verify(eventAttenderRepo).save(any(EventAttender.class));
    }

    @Test
    void addAttender_whenAlreadyExists_returnsFalse() {
        Long eventId = 41L;
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(eventWith(
                List.of(locFuture(1)), List.of(img("p", true)), makeUser(2L,"o@x", Role.ROLE_USER), true)));
        when(eventAttenderRepo.existsByEventIdAndUserId(eq(eventId), eq(7L))).thenReturn(true);

        boolean ok = service.addAttender(eventId, UserVO.builder().id(7L).role(Role.ROLE_USER).build());

        assertFalse(ok);
        verify(eventAttenderRepo, never()).save(any());
    }

    // --- removeAttender(...) ------------------------------------------------

    @Test
    void removeAttender_success_upcomingAndWasAttending() {
        Long eventId = 50L;
        Event future = eventWith(List.of(locFuture(1)), List.of(img("p", true)),
                makeUser(3L, "o@x", Role.ROLE_USER), true);

        when(eventRepo.findById(eventId)).thenReturn(Optional.of(future));
        when(eventAttenderRepo.existsByEventIdAndUserId(eventId, 9L)).thenReturn(true);
        when(eventAttenderRepo.deleteByEventIdAndUserId(eventId, 9L)).thenReturn(1);

        boolean ok = service.removeAttender(eventId, UserVO.builder().id(9L).role(Role.ROLE_USER).build());

        assertTrue(ok);
        verify(eventAttenderRepo).deleteByEventIdAndUserId(eventId, 9L);
    }

    @Test
    void removeAttender_passedEvent_throwsBadRequest() {
        Long eventId = 51L;
        Event past = eventWith(List.of(locPast()), List.of(img("p", true)),
                makeUser(4L, "o@x", Role.ROLE_USER), true);

        when(eventRepo.findById(eventId)).thenReturn(Optional.of(past));

        assertThrows(BadRequestException.class,
                () -> service.removeAttender(eventId, UserVO.builder().id(9L).role(Role.ROLE_USER).build()));
        verify(eventAttenderRepo, never()).deleteByEventIdAndUserId(anyLong(), anyLong());
    }
}
