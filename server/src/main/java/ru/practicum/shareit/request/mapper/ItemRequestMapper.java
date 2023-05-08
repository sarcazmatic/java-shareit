package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDtoRequest itemRequestDtoRequest, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDtoRequest.getId());
        itemRequest.setDescription(itemRequestDtoRequest.getDescription());
        itemRequest.setRequester(user);
        itemRequest.setCreated(itemRequestDtoRequest.getCreated());
        return itemRequest;
    }

    public static ItemRequestDtoResponse toItemRequestDtoResponse(ItemRequest itemRequest, List<Item> items) {
        List<ItemDtoResponse> itemRequestDtos = null;
        if (items != null) {
            itemRequestDtos = items
                    .stream()
                    .map(item -> ItemDtoResponse.builder()
                            .id(item.getId())
                            .requestId(itemRequest.getId())
                            .available(item.getAvailable())
                            .description(item.getDescription())
                            .name(item.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        return ItemRequestDtoResponse.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(itemRequestDtos)
                .build();

    }
}

