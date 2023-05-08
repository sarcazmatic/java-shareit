package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.List;

public interface ItemService {

    List<ItemDtoWithBooking> getListItemByUserId(Long userId, Pageable pageable);

    ItemDtoWithBooking getItemById(Long itemId, Long userId);

    ItemDtoResponse createItem(Long userId, ItemDtoRequest itemDtoRequest);

    ItemDtoResponse updateItem(Long userId, Long itemId, ItemDtoResponse itemDtoResponse);

    List<ItemDtoResponse> searchItem(String text, Pageable pageable);

    CommentDtoResponse addComment(Long itemId, Long userId, CommentDtoRequest commentDtoRequest);

}
