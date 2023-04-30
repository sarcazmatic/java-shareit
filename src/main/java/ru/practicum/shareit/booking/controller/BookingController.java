package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.utility.Create;
import ru.practicum.shareit.utility.PageableMaker;

import java.util.List;

import static ru.practicum.shareit.item.controller.ItemController.USER_ID;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoResponse createBooking(@RequestHeader(USER_ID) Long userId,
                                            @RequestBody @Validated({Create.class}) BookingDtoRequest bookingDtoRequest) {
        return bookingService.createBooking(bookingDtoRequest, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse updateBooking(@RequestHeader(USER_ID) Long userId,
                                            @PathVariable Long bookingId, @RequestParam("approved") Boolean approved) {
        return bookingService.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse getBookingById(@RequestHeader(USER_ID) Long userId, @PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoResponse> getAllBookingForBooker(@RequestHeader(USER_ID) Long userId,
                                                           @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                           @RequestParam(required = false) Integer from,
                                                           @RequestParam(required = false) Integer size) {
        Pageable pageable = PageableMaker.makePageable(from, size, Sort.by(Sort.Direction.DESC, "id"));
        return bookingService.getAllBookingByUserId(userId, state, pageable);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> getAllBookingForOwner(@RequestHeader(USER_ID) Long userId,
                                                          @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                          @RequestParam(required = false) Integer from,
                                                          @RequestParam(required = false) Integer size) {
        Pageable pageable = PageableMaker.makePageable(from, size, Sort.by(Sort.Direction.DESC, "id"));
        return bookingService.getAllBookingByOwnerId(userId, state, pageable);
    }

}
