package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ItemRequestDtoResponse {

    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDtoResponse> items; //это список ответов, но на деле итемов

}
