package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    ItemDto getById(long id);

    List<ItemDto> getAll(long userId);

    ItemDto update(long userId, ItemDto itemDto, long itemId);

    List<ItemDto> getSearchResults(String text);
}
