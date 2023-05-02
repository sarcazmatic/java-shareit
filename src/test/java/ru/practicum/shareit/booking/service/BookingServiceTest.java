package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
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


    ItemRepository itemRepository;

    BookingService bookingService;

    BookingRepository bookingRepository;

    UserRepository userRepository;

    User user = User.builder().id(1L).name("user1000").email("user1000@email.com").build();
    User user2 = User.builder().id(2L).name("user2000").email("user2000@email.com").build();
    Item item = new Item(1L, "item1", "description1", true, user, null);
    Booking booking = Booking.builder().id(1L).start(LocalDateTime.now()).end(LocalDateTime.now().plusDays(1)).booker(user).item(item).status(BookingStatus.WAITING).state(State.CURRENT).build();
    Booking booking2 = Booking.builder().id(2L).start(LocalDateTime.now()).end(LocalDateTime.now().plusDays(1)).booker(user2).item(item).status(BookingStatus.WAITING).state(State.CURRENT).build();
    Booking bookingApproved = new Booking(3L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), item, user2, BookingStatus.APPROVED, State.CURRENT);
    Booking bookingRejected = new Booking(4L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), item, user2, BookingStatus.REJECTED, State.CURRENT);


    @Autowired
    public BookingServiceTest(BookingRepository bookingRepository, BookingService bookingService, UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        userRepository.save(user);
        userRepository.save(user2);
        itemRepository.save(item);
        bookingRepository.save(booking);
        bookingRepository.save(booking2);
        bookingRepository.save(bookingApproved);
        bookingRepository.save(bookingRejected);
    }

    @Test
    void getBookingByIdTest() {
        assertEquals(booking.getId(), bookingRepository.findById(booking.getId()).get().getId());
        assertEquals(booking.getItem().getDescription(), bookingRepository.findById(booking.getId()).get().getItem().getDescription());
    }

    @Test
    void getWrongUserTest() {
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(booking.getId(), 2L));
    }

    @Test
    void createBookingTest() {
        bookingRepository.save(booking);
        assertEquals(booking.getId(), bookingRepository.findById(booking.getId()).orElse(null).getId());
    }

    @Test
    void updateBookingAlreadyApprovedTest() {
        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingApproved.getId(), user.getId(), true));
    }

    @Test
    void updateBookingAlreadyRejectedTest() {
        assertThrows(ValidationException.class, () -> bookingService.updateBooking(bookingRejected.getId(), user.getId(), false));
    }


    @Test
    void updateBookingApprovedByNotOwnerTest() {
        assertThrows(NotFoundException.class, () -> bookingService.updateBooking(booking.getId(), user2.getId(), true));
    }

    @Test
    void getAllBookingByUserIdTest() {
        assertEquals(3, bookingService.getAllBookingByUserId(user2.getId(), "ALL", Pageable.unpaged()).size());
    }

    @Test
    void getPastBookingByUserIdTest() {
        assertEquals(new ArrayList<>(), bookingService.getAllBookingByUserId(user2.getId(), "PAST", Pageable.unpaged()));
    }

    @Test
    void getFutureBookingByUserIdTest() {
        assertEquals(new ArrayList<>(), bookingService.getAllBookingByUserId(user2.getId(), "FUTURE", Pageable.unpaged()));
    }

    @Test
    void getCurrentBookingByUserIdTest() {
        assertEquals(3, bookingService.getAllBookingByUserId(user2.getId(), "CURRENT", Pageable.unpaged()).size());
    }

    @Test
    void getWaitingBookingByUserIdTest() {
        assertEquals(List.of(booking).size(), bookingService.getAllBookingByUserId(user2.getId(), "WAITING", Pageable.unpaged()).size());
    }

    @Test
    void getRejectedBookingByUserIdTest() {
        assertEquals(List.of(bookingRejected).get(0).getId(), bookingService.getAllBookingByUserId(user2.getId(), "REJECTED", Pageable.unpaged()).get(0).getId());
    }

    @Test
    void getAllBookingByUserIdNegativeTest() {
        assertThrows(ValidationException.class, () -> PageableMaker.makePageable(-1, -1, Sort.by("id").ascending()));
    }

    @Test
    void getAllBookingByUserIdBadWithoutBookingTest() {
        assertThrows(NotFoundException.class, () -> bookingService.getAllBookingByOwnerId(user2.getId(), "BAD_STATE", Pageable.unpaged()).get(0).getId());

    }

    @Test
    void getAllBookingByUserIdBadStateTest() {
        assertThrows(ValidationException.class, () -> bookingService.getAllBookingByUserId(user2.getId(), "BAD_STATE", Pageable.unpaged()).get(0).getId());

    }

    @Test
    void getAllBookingByOwnerIdTest() {
        assertEquals(4, bookingService.getAllBookingByOwnerId(user.getId(), "ALL", Pageable.unpaged()).size());
    }

    @Test
    void getPastBookingByOwnerIdTest() {
        assertEquals(new ArrayList<>(), bookingService.getAllBookingByOwnerId(user.getId(), "PAST", Pageable.unpaged()));
    }

    @Test
    void getFutureBookingByOwnerIdTest() {
        assertEquals(new ArrayList<>(), bookingService.getAllBookingByOwnerId(user.getId(), "FUTURE", Pageable.unpaged()));
    }

    @Test
    void getCurrentBookingByOwnerIdTest() {
        assertEquals(4, bookingService.getAllBookingByOwnerId(user.getId(), "CURRENT", Pageable.unpaged()).size());
    }

    @Test
    void getWaitingBookingByOwnerIdTest() {
        assertEquals(List.of(booking).get(0).getId(), bookingService.getAllBookingByOwnerId(user.getId(), "WAITING", Pageable.unpaged()).get(0).getId());
    }

    @Test
    void getRejectedBookingByOwnerIdTest() {
        assertEquals(List.of(bookingRejected).get(0).getId(), bookingService.getAllBookingByOwnerId(user.getId(), "REJECTED", Pageable.unpaged()).get(0).getId());
    }

    @Test
    void getAllBookingByOwnerIdBadStateTest() {
        assertThrows(ValidationException.class, () -> bookingService.getAllBookingByOwnerId(user.getId(), "BAD_STATE", Pageable.unpaged()).get(0).getId());

    }

}
