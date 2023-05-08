package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Дата начала бронирования позже окончания");
        }
        if (booking.getStart().equals(booking.getEnd())) {
            throw new ValidationException("Дата начала совпадает с датой окончания");
        }
        if (Objects.equals(booking.getBooker().getId(), booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
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

    public State getStateByStr(String stateStr) {

        State state;

        try {
            state = State.valueOf(stateStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("Unknown state: %s", stateStr));
        }

        return state;
    }

    public List<BookingDtoResponse> getAllBookingByUser(Long userId, String stateStr, Pageable pageable) {

        State state = getStateByStr(stateStr);
        List<Booking> bookings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBooker_IdOrderByStartDesc(userId, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdStatePastPageable(userId, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findBookingByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findFuturePageable(userId, now, pageable);
                break;

        }

        return BookingMapper.toBookingDtoResponseList(bookings);
    }

    public List<BookingDtoResponse> getAllBookingByOwner(Long userId, String stateStr, Pageable pageable) {

        State state = getStateByStr(stateStr);
        List<Booking> bookings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = bookingRepository.findOwnerAllPageable(userId, pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findOwnerFuturePageable(userId, now, pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findBookingByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, pageable);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findOwnerPastPageable(userId, now, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new NotFoundException("Состояние резервирования не распознано");
        }

        return BookingMapper.toBookingDtoResponseList(bookings);
    }

    @Transactional
    public List<BookingDtoResponse> getAllBookingByOwnerId(Long ownerId, String stateStr, Pageable pageable) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", ownerId)));
        List<Booking> bookings = bookingRepository.findAllByItem_Owner_Id(ownerId, pageable);
        if (bookings.isEmpty()) {
            throw new NotFoundException("Бронирований не найдено");
        }
        return getAllBookingByOwner(ownerId, stateStr, pageable);
    }

    @Transactional
    public List<BookingDtoResponse> getAllBookingByUserId(Long userId, String stateStr, Pageable pageable) {

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID %s не найден", userId)));
        List<Booking> bookings = bookingRepository.findAllByBooker_IdOrderByIdDesc(userId, pageable);

        if (bookings.isEmpty()) {
            throw new NotFoundException("Бронирований не найдено");
        }

        return getAllBookingByUser(userId, stateStr, pageable);
    }


    private void setApprovedStatus(Booking booking, Boolean isApproved) {
        if (isApproved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
    }
}
