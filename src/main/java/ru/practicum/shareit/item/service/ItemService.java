package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.List;

public interface ItemService {

    List<ItemDtoWithBooking> getListItemByUserId(Long userId);

    ItemDtoWithBooking getItemById(Long itemId, Long userId);

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> searchItem(String text);

    CommentDtoResponse addComment(Long itemId, Long userId, CommentDtoRequest commentDtoRequest);

}
