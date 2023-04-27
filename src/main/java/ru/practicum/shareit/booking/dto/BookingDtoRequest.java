package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.Create;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class BookingDtoRequest {

    private Long id;
    @NotNull(groups = Create.class)
    @FutureOrPresent(groups = Create.class)
    private LocalDateTime start;
    @NotNull(groups = Create.class)
    @Future(groups = Create.class)
    private LocalDateTime end;
    private Item item;
    private Long itemId;
    private User booker;
    private BookingStatus status;
    private State state;



}
