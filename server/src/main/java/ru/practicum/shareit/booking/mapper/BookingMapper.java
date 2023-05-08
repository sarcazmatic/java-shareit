package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class BookingMapper {

    public BookingDtoResponse toBookingDtoResponse(Booking booking) {
        BookingDtoResponse.Item item = new BookingDtoResponse.Item();
        BookingDtoResponse.User bookerDto = new BookingDtoResponse.User();

        if (booking.getItem() != null) {
            item.setId(booking.getItem().getId());
            item.setName(booking.getItem().getName());
        }

        if (booking.getBooker() != null) {
            bookerDto.setId(booking.getBooker().getId());

        }
        log.info("Собираем бронирование");
        return BookingDtoResponse.builder()
                .id(booking.getId())
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(item)
                .booker(bookerDto)
                .status(booking.getStatus())
                .build();
    }

    public List<BookingDtoResponse> toBookingDtoResponseList(List<Booking> booking) {

        return booking
                .stream()
                .map(BookingMapper::toBookingDtoResponse)
                .collect(Collectors.toList());
    }

    public Booking toBooking(User booker, Item item, BookingDtoRequest bookingDtoRequest) {
        Booking booking = new Booking();
        //booking.setId(bookingDtoRequest.getId());
        booking.setStart(bookingDtoRequest.getStart());
        booking.setEnd(bookingDtoRequest.getEnd());
        booking.setBooker(booker);
        booking.setItem(item);
        return booking;
    }

}
