package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(BookingDto bookingDto, Long userId);

    BookingDto updateBooking(Long bookingDto, Long userId, Boolean isApproved);

    BookingDto getBookingById(Long bookingId, Long userId);

    List<BookingDto> getAllBookingByOwnerId(Long userId, String state);

    List<BookingDto> getAllBookingByUserId(Long userId, String state);
}
