package ru.practicum.shareit.booking.dto;

import lombok.*;
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
    @NotNull(groups = Create.class)
    private Long itemId;

}
