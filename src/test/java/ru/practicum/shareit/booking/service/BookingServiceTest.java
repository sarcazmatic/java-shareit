package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utility.PageableMaker;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BookingServiceTest {
    private final BookingRepository bookingRepository;

    private final BookingService bookingService;

    private final User user = new User(1L, "user1", "user1@mail.ru");
    private final User user2 = new User(2L, "user2", "user2@mail.ru");
    private final Item item = new Item(1L, "item1", "description1", true, user,
            null);
    private final Item itemNotAvailable = new Item(1L, "item1", "description1",
            false, user, null);
    private final Booking booking = new Booking(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
            item, user2, BookingStatus.WAITING, State.CURRENT);

    private final Booking bookingApproved = new Booking(2L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
            item, user2, BookingStatus.APPROVED, State.CURRENT);

    private final Booking bookingRejected = new Booking(3L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
            item, user2, BookingStatus.REJECTED, State.CURRENT);

    @Autowired
    public BookingServiceTest(BookingRepository bookingRepository,
                              BookingService bookingService,
                              ItemService itemService,
                              UserService userService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        userService.createUser(UserMapper.userToDto(user));
        userService.createUser(UserMapper.userToDto(user2));
        itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));
        bookingRepository.save(booking);
        bookingRepository.save(bookingApproved);
        bookingRepository.save(bookingRejected);
    }

    @Test
    void getBookingByIdTest() {
        assertEquals(booking.getId(),
                bookingService.getBookingById(booking.getId(), booking.getBooker().getId()).getId());
    }

    @Test
    void getWrongUserTest() {
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(booking.getId(), 100L));
    }

    @Test
    void createBookingTest() {
        BookingDtoResponse bookingDto = BookingDtoResponse.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();
        BookingDtoRequest bookingDtoReq = BookingDtoRequest.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();
        Booking booking1 = BookingMapper.toBooking(user, item, bookingDtoReq);
        assertEquals(booking1.getId(), bookingRepository.findById(booking1.getId()).orElse(null).getId());
    }

    @Test
    void updateBookingAlreadyApprovedTest() {
        assertThrows(ValidationException.class, () -> bookingService
                .updateBooking(bookingApproved.getId(), user.getId(), true));
    }

    @Test
    void updateBookingAlreadyRejectedTest() {
        assertThrows(ValidationException.class, () -> bookingService
                .updateBooking(bookingRejected.getId(), user.getId(), false));
    }

    @Test
    void updateBookingApprovedByNotOwnerTest() {
        assertThrows(NotFoundException.class, () -> bookingService
                .updateBooking(booking.getId(), user2.getId(), true));
    }

    @Test
    void getAllBookingByUserIdTest() {
        assertEquals(3,
                bookingService.getAllBookingByUserId(user2.getId(), "ALL", Pageable.unpaged()).size());
    }

    @Test
    void getPastBookingByUserIdTest() {
        assertEquals(new ArrayList<>(),
                bookingService.getAllBookingByUserId(user2.getId(), "PAST", Pageable.unpaged()));
    }

    @Test
    void getFutureBookingByUserIdTest() {
        assertEquals(new ArrayList<>(),
                bookingService.getAllBookingByUserId(user2.getId(), "FUTURE", Pageable.unpaged()));
    }

    @Test
    void getCurrentBookingByUserIdTest() {
        assertEquals(3,
                bookingService.getAllBookingByUserId(user2.getId(), "CURRENT", Pageable.unpaged()).size());
    }

    @Test
    void getWaitingBookingByUserIdTest() {
        assertEquals(List.of(booking).get(0).getId(),
                bookingService.getAllBookingByUserId(user2.getId(), "WAITING", Pageable.unpaged()).get(0).getId());
    }

    @Test
    void getRejectedBookingByUserIdTest() {
        assertEquals(List.of(bookingRejected).get(0).getId(),
                bookingService.getAllBookingByUserId(user2.getId(), "REJECTED", Pageable.unpaged()).get(0).getId());
    }

    @Test
    void getAllBookingByUserIdNegativeTest() {
        assertThrows(ValidationException.class,
                () -> PageableMaker.makePageable(-1, -1, Sort.by("id").ascending()));
    }

    @Test
    void getAllBookingByUserIdBadWithoutBookingTest() {
        assertThrows(NotFoundException.class, () -> bookingService
                .getAllBookingByOwnerId(user2.getId(), "BAD_STATE", Pageable.unpaged()).get(0).getId());

    }

    @Test
    void getAllBookingByUserIdBadStateTest() {
        assertThrows(ValidationException.class, () -> bookingService
                .getAllBookingByUserId(user2.getId(), "BAD_STATE", Pageable.unpaged()).get(0).getId());

    }

    @Test
    void getAllBookingByOwnerIdTest() {
        assertEquals(3,
                bookingService.getAllBookingByOwnerId(user.getId(), "ALL", Pageable.unpaged()).size());
    }

    @Test
    void getPastBookingByOwnerIdTest() {
        assertEquals(new ArrayList<>(),
                bookingService.getAllBookingByOwnerId(user.getId(), "PAST", Pageable.unpaged()));
    }

    @Test
    void getFutureBookingByOwnerIdTest() {
        assertEquals(new ArrayList<>(),
                bookingService.getAllBookingByOwnerId(user.getId(), "FUTURE", Pageable.unpaged()));
    }

    @Test
    void getCurrentBookingByOwnerIdTest() {
        assertEquals(3,
                bookingService.getAllBookingByOwnerId(user.getId(), "CURRENT", Pageable.unpaged()).size());
    }

    @Test
    void getWaitingBookingByOwnerIdTest() {
        assertEquals(List.of(booking).get(0).getId(),
                bookingService.getAllBookingByOwnerId(user.getId(), "WAITING", Pageable.unpaged()).get(0).getId());
    }

    @Test
    void getRejectedBookingByOwnerIdTest() {
        assertEquals(List.of(bookingRejected).get(0).getId(),
                bookingService.getAllBookingByOwnerId(user.getId(), "REJECTED", Pageable.unpaged()).get(0).getId());
    }

    @Test
    void getAllBookingByOwnerIdBadStateTest() {
        assertThrows(ValidationException.class, () -> bookingService
                .getAllBookingByOwnerId(user.getId(), "BAD_STATE", Pageable.unpaged()).get(0).getId());

    }

}
