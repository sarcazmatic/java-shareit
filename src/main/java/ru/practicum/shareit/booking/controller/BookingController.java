package ru.practicum.shareit.booking.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.util.List;

import static ru.practicum.shareit.item.controller.ItemController.USER_ID;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createBooking(@RequestHeader(USER_ID) Long userId,
                                    @RequestBody @Valid BookingDto bookingDto) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateBooking(@RequestHeader(USER_ID) Long userId,
                                    @PathVariable Long bookingId, @PathParam("approved") @NonNull Boolean approved) {
        return bookingService.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(USER_ID) Long userId, @PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingForBooker(@RequestHeader(USER_ID) Long userId,
                                                   @PathParam("state") String state) {
        return bookingService.getAllBookingByUserId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingForOwner(@RequestHeader(USER_ID) Long userId,
                                                  @PathParam("state") String state) {
        return bookingService.getAllBookingByOwnerId(userId, state);
    }

}
