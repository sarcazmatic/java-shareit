package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    boolean checkItemIdExist(long id);

    Item getById(long id);

    Item create(Item item, long userId);

    List<Item> getAll(long userId);

    List<Item> getSearchResults(String text);
}
