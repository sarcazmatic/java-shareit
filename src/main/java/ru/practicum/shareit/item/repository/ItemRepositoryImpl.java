package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final UserRepository userRepository;
    private final HashMap<Long, Item> items = new HashMap<>();
    private final HashMap<Long, List<Item>> itemsByUserId = new HashMap<>();
    private long id = 0;

    private long makeId() {
        return ++id;
    }

    @Override
    public boolean checkItemIdExist(long id) {
        return items.containsKey(id);
    }

    @Override
    public Item create(Item item, long userId) {
        List<Item> itemList = new ArrayList<>();

        if (itemsByUserId.containsKey(userId)) {
            itemList = itemsByUserId.get(userId);
        }

        item.setId(makeId());
        item.setOwner(userRepository.read(userId));
        itemList.add(item);

        items.put(item.getId(), item);
        itemsByUserId.put(userId, itemList);
        return item;
    }

    @Override
    public Item getById(long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAll(long userId) {
        return itemsByUserId.get(userId);
    }

    @Override
    public List<Item> getSearchResults(String text) {
        List<Item> itemList = new ArrayList<>();
        String searchText = text.toLowerCase();

        if (!text.isBlank()) {
            itemList = items.values().stream()
                    .filter(item -> item.isAvailable())
                    .filter(item -> item.getName().toLowerCase().contains(searchText)
                            || item.getDescription().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }
        return itemList;
    }
}
