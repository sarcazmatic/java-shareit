package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID) long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable int id) {
        return itemService.getById(id);
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(USER_ID) long userId) {
        return itemService.getAll(userId);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@RequestHeader(USER_ID) long userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable("id") long itemId) {
        return itemService.update(userId, itemDto, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> getSearchResults(@RequestParam String text) {
        return itemService.getSearchResults(text);
    }

}
