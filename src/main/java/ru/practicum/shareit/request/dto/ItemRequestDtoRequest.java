package ru.practicum.shareit.request.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDtoRequest {

    private Long id;
    @NotNull
    private String description;
    private LocalDateTime created;
    private Long requestId;

}
