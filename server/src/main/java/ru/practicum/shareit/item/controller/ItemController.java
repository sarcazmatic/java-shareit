package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utility.PageableMaker;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    public static final String USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDtoWithBooking> getListItemByUserId(@RequestHeader(USER_ID) Long userId,
                                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        Pageable pageable = PageableMaker.makePageable(from, size, Sort.by(Sort.Direction.ASC, "id"));
        return itemService.getListItemByUserId(userId, pageable);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBooking getItemById(@RequestHeader(USER_ID) Long userId,
                                          @PathVariable Long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @PostMapping
    public ItemDtoResponse createItem(@RequestHeader(USER_ID) Long userId,
                                      @RequestBody ItemDtoRequest itemDtoRequest) {
        return itemService.createItem(userId, itemDtoRequest);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse addComment(@RequestHeader(USER_ID) Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody CommentDtoRequest commentDtoRequest) {
        return itemService.addComment(itemId, userId, commentDtoRequest);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse updateItem(@RequestHeader(USER_ID) Long userId,
                                      @PathVariable Long itemId,
                                      @RequestBody ItemDtoResponse itemDtoResponse) {
        return itemService.updateItem(userId, itemId, itemDtoResponse);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> searchItem(@RequestParam String text,
                                            @RequestParam(required = false, defaultValue = "0") Integer from,
                                            @RequestParam(required = false, defaultValue = "10") Integer size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        } else {
            Pageable pageable = PageableMaker.makePageable(from, size, Sort.by(Sort.Direction.ASC, "id"));
            return itemService.searchItem(text, pageable);
        }
    }
}
