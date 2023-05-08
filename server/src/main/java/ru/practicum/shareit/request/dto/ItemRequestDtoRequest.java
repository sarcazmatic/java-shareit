package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDtoRequest {

    private Long id;
    private String description;
    private LocalDateTime created;
    private Long requestId;

}
