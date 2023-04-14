package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final UserRepository userRepository;
    private final Map<Long, Item> items = new HashMap<>();
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

        item.setId(makeId());
        item.setOwner(userRepository.read(userId));
        itemList.add(item);

        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getById(long itemId) {
        return items.get(itemId);
    }

    @Override
    public List<Item> getAll(long userId) {
        return items.values().
                stream().
                filter(i -> i.getOwner().getId() == userId).
                collect(Collectors.toList());
    }

    @Override
    public List<Item> getSearchResults(String text) {
        List<Item> itemList = new ArrayList<>();

        if (!text.isBlank()) {
            itemList = items.values().stream()
                    .filter(item -> item.isAvailable())
                    .filter(item -> StringUtils.containsIgnoreCase(item.getName(), text)
                            || StringUtils.containsIgnoreCase(item.getDescription(), text))
                    .collect(Collectors.toList());
        }
        return itemList;
    }
}
