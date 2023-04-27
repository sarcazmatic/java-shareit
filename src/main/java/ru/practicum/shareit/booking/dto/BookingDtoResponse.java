package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.enums.State;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class BookingDtoResponse {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private Long itemId;
    private User booker;
    private BookingStatus status;
    private State state;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        private Long id;
    }
}
