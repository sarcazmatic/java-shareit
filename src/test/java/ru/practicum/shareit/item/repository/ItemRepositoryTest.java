package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.utility.PageableMaker;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    User user1;
    Item item1;
    User user2;
    Item item2;
    Pageable pageable;

    @BeforeEach
    void beforeEach() {
        user1 = userRepository.save(User.builder().id(1L).name("user1").email("user1@email").build());
        item1 = itemRepository.save(Item.builder().id(1L).owner(user1).name("item1").description("description1").available(true).request(null).build());


        user2 = userRepository.save(User.builder().id(2L).name("user2").email("user2@email").build());
        item2 = itemRepository.save(Item.builder().id(2L).owner(user2).name("item2").description("description2").available(true).request(null).build());
        pageable = PageableMaker.makePageable(0,10, Sort.by("id").ascending());
    }

    @Test
    void findByOwner() {
        final List<Item> byOwner = (List<Item>) itemRepository.findAllByOwnerId(user1.getId(), pageable);

        assertNotNull(byOwner);
        assertEquals(1, byOwner.size());
        assertEquals("item1", byOwner.get(0).getName());
    }

    @Test
    void findByTextTest() {

        Collection<Item> itemList = itemRepository.searchItem("descrip", pageable);
        assertThat(itemList.size(), is(2));
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        userRepository.deleteAll();
    }
}
