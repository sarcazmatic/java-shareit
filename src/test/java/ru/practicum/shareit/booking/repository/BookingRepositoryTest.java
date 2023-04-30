package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    User user1;
    Item item1;
    User user2;
    Item item2;
    Booking booking;
    Booking bookingApproved;


    @BeforeEach
    void beforeEach() {
        user1 = userRepository.save(new User(1L, "user1", "user1@email"));
        user2 = userRepository.save(new User(2L, "user2", "user2@email"));

        item1 = itemRepository.save(new Item(1L, "item1", "description1", true, user1, null));
        item2 = itemRepository.save(new Item(2L, "item2", "description2", true, user1, null));

        booking = bookingRepository.save(new Booking(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                item1, user2, BookingStatus.WAITING, State.WAITING));
        bookingApproved = bookingRepository.save(new Booking(2L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                item2, user2, BookingStatus.APPROVED, State.WAITING));
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findAllByItem_Owner_IdOrderByStartDescTest() {
        List<Booking> bookings = bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(user1.getId());
        assertThat(bookings.size(), is(2));
    }

    @Test
    void findByBookerIdStatePastTest() {

        bookingApproved.setStart(LocalDateTime.now().minusDays(100));
        bookingApproved.setEnd(LocalDateTime.now().minusDays(50));
        bookingRepository.save(bookingApproved);
        Pageable pageable = PageRequest.of(1 / 10, 10);

        List<Booking> bookingsBooker = bookingRepository.findByBookerIdStatePast(user2.getId(), LocalDateTime.now());
        List<Booking> bookingsOwner = bookingRepository.findOwnerPast(user1.getId(), LocalDateTime.now());


        assertThat(bookingsBooker.size(), is(1));
        assertThat(bookingsOwner.size(), is(1));
    }

    @Test
    void findByBookerIdStateCurrentTest() {

        Pageable pageable = PageRequest.of(1 / 10, 10);
        List<Booking> bookingsBooker = bookingRepository.findAllByBooker_Id(user2.getId());
        List<Booking> bookingsOwner = bookingRepository.findAllByItem_Owner_Id(user1.getId());

        assertThat(bookingsBooker.size(), is(2));
        assertThat(bookingsOwner.size(), is(2));
    }

    @Test
    void findFutureTest() {

        booking.setStart(LocalDateTime.now().plusDays(50));
        booking.setEnd(LocalDateTime.now().plusDays(100));
        bookingRepository.save(booking);

        Pageable pageable = PageRequest.of(1 / 10, 10);
        List<Booking> bookingsBooker = bookingRepository.findFuture(user2.getId(), LocalDateTime.now());
        List<Booking> bookingsOwner = bookingRepository.findOwnerFuture(user1.getId(), LocalDateTime.now());

        assertThat(bookingsBooker.size(), is(1));
        assertThat(bookingsOwner.size(), is(1));
    }

    @Test
    void findOwnerAllTest() {

        Pageable pageable = PageRequest.of(1 / 10, 10);
        List<Booking> bookings = bookingRepository.findOwnerAll(user1.getId());
        assertThat(bookings.size(), is(2));
    }

    @Test
    void updateBookingTest() {
        booking.setStatus(BookingStatus.APPROVED);
        assertEquals(BookingStatus.APPROVED, bookingRepository.findByBooker_IdAndStatus(2L, BookingStatus.APPROVED, Pageable.unpaged()).get(0).getStatus());
    }

}


