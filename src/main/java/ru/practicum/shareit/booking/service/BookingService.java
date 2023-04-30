package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

public interface BookingService {

    BookingDtoResponse createBooking(BookingDtoRequest bookingDtoRequest, Long userId);

    BookingDtoResponse updateBooking(Long bookingDto, Long userId, Boolean isApproved);

    BookingDtoResponse getBookingById(Long bookingId, Long userId);

    List<BookingDtoResponse> getAllBookingByOwnerId(Long userId, String state, Pageable pageable);

    List<BookingDtoResponse> getAllBookingByUserId(Long userId, String state, Pageable pageable);
}
