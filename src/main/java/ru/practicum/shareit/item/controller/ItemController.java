package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.utility.Create;
import ru.practicum.shareit.utility.Update;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    public static final String USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDtoWithBooking> getListItemByUserId(@RequestHeader(USER_ID) Long userId) {
        return itemService.getListItemByUserId(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBooking getItemById(@RequestHeader(USER_ID) Long userId,
                                          @PathVariable Long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_ID) Long userId, @Validated({Create.class}) @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID) Long userId,
                                 @PathVariable Long itemId, @Validated({Create.class}) @RequestBody CommentDto commentDto) {
        return itemService.addComment(itemId, userId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID) Long userId, @PathVariable Long itemId, @Validated({Update.class}) @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        } else {
            return itemService.searchItem(text);
        }
    }
}
