package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public BookingDtoResponse createBooking(BookingDtoRequest bookingDtoRequest, Long userId) {

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        Item item = itemRepository.findById(bookingDtoRequest.getItemId())
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с ID %s не найдена", bookingDtoRequest.getItemId())));
        Booking booking = BookingMapper.toBooking(booker, item, bookingDtoRequest);

        if (!booking.getItem().getAvailable()) {
            throw new ValidationException("Вещь не доступна");
        }
        if (Objects.equals(booking.getBooker().getId(), booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Дата начала бронирования позже окончания");
        }
        if (booking.getStart().equals(booking.getEnd())) {
            throw new ValidationException("Дата начала совпадает с датой окончания");
        }
        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования уже прошла");
        }
        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата окончания бронирования уже прошла");
        }
        booking.setStatus(BookingStatus.WAITING);
        log.info("Вещь с  ID {} забронирована", item.getId());

        return BookingMapper.toBookingDtoResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDtoResponse updateBooking(Long bookingId, Long userId, Boolean isApproved) {
        Booking booking = getBookingById(bookingId);

        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("Пользователь не является владельцем");
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED) && isApproved) {
            throw new ValidationException("Уже одобрено");
        }
        if (booking.getStatus().equals(BookingStatus.REJECTED) && !isApproved) {
            throw new ValidationException("Уже отклонено");
        }
        setApprovedStatus(booking, isApproved);
        log.info("Бронирование с ID {} обновлено", booking.getId());

        return BookingMapper.toBookingDtoResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDtoResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));

        if (Objects.equals(booking.getBooker().getId(), userId) || Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            return BookingMapper.toBookingDtoResponse(booking);
        } else {
            throw new NotFoundException(String.format("Бронирование с ID %s не найдено", bookingId));
        }
    }

    @Transactional
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с ID %s не найдено",
                        bookingId)));
    }

    public List<Booking> getAllUser(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        return bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
    }

    public State getStateByStr(String stateStr) {

        State state;
        if (stateStr == null) {
            state = State.ALL;
        } else {
            try {
                state = State.valueOf(stateStr);
            } catch (IllegalArgumentException e) {
                throw new ValidationException(String.format("Unknown state: %s", stateStr));
            }
        }

        return state;
    }

    public List<BookingDtoResponse> getAllBookingByUser(Long userId, String stateStr) {

        State state = getStateByStr(stateStr);
        List<Booking> bookings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = getAllUser(userId);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdStatePast(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING);
                break;
            case CURRENT:
                bookings = bookingRepository.findBookingByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now,now);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED);
                break;
            case FUTURE:
                bookings = bookingRepository.findFuture(userId, now);
                break;
        }

        return BookingMapper.toBookingDtoResponseList(bookings);
    }

    public List<BookingDtoResponse> getAllBookingByOwner(Long userId, String stateStr) {

        State state = getStateByStr(stateStr);
        List<Booking> bookings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findOwnerAll(userId);
                break;
            case FUTURE:
                bookings = bookingRepository.findOwnerFuture(userId, now);
                break;
            case CURRENT:
                bookings = bookingRepository.findBookingByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, now, now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING);
                break;
            case PAST:
                bookings = bookingRepository.findOwnerPast(userId, now);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED);
                break;
        }

        return BookingMapper.toBookingDtoResponseList(bookings);
    }

    @Transactional
    public List<BookingDtoResponse> getAllBookingByOwnerId(Long ownerId, String stateStr) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", ownerId)));
        List<Booking> bookings = bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(ownerId);
        if (bookings.isEmpty()) {
            throw new NotFoundException("Бронирований не найдено");
        }
        return getAllBookingByOwner(ownerId, stateStr);
    }

    @Transactional
    public List<BookingDtoResponse> getAllBookingByUserId(Long userId, String stateStr) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        List<Booking> bookings = bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
        if (bookings.isEmpty()) {
            throw new NotFoundException("Бронирований не найдено");
        }
        return getAllBookingByUser(userId, stateStr);
    }


    private void setApprovedStatus(Booking booking, Boolean isApproved) {
        if (isApproved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
    }
}
