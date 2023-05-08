package ru.practicum.shareit.item.service;


import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class ItemServiceImplTest {
    CommentRepository commentRepository;

    ItemRepository itemRepository;

    BookingService bookingService;

    BookingRepository bookingRepository;

    UserService userService = Mockito.mock(UserService.class);

    ItemService itemService;

    UserRepository userRepository;
    private final User user = new User(1L, "user1", "user1@mail.ru");
    private final User user2 = new User(2L, "user2", "user2@mail.ru");
    private final Item item = new Item(1L, "item1", "description1", true, user, null);

    private Item item1;
    private ItemDtoWithBooking itemCommentDto;

    private final LocalDateTime localDateTime = LocalDateTime.now();

    @Autowired
    public ItemServiceImplTest(CommentRepository commentRepository, UserRepository userRepository, ItemRepository itemRepository, BookingService bookingService, BookingRepository bookingRepository, ItemService itemService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        userRepository.save(user);
        userRepository.save(user2);
        this.itemRepository = itemRepository;
        this.itemService = itemService;
        item1 = item;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        itemCommentDto = ItemMapper.toItemDtoWithBooking(new ArrayList<>(), bookingRepository.findFirstByItem_IdAndEndBeforeOrderByEndDesc(item1.getId(), localDateTime), bookingRepository.findTopByItem_IdAndStartAfterOrderByStartAsc(item1.getId(), localDateTime), item);


    }

    @Test
    void getItemByIdTest() {
        ItemDtoResponse item1 = itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));
        assertEquals(itemCommentDto.getId(), itemService.getItemById(item1.getId(), user.getId()).getId());
    }

    @Test
    void getAllByUserIdTest() {
        assertEquals(List.of(itemCommentDto).get(0).getId(), itemService.getListItemByUserId(1L, Pageable.unpaged()).get(0).getId());
    }


    @Test
    void createItemDtoTest() {
        ItemDtoResponse item2 = itemService.createItem(item1.getOwner().getId(), ItemMapper.toItemDtoReq(item1));
        assertEquals(itemRepository.findById(item1.getId()).orElse(null).getId(), item2.getId());
    }

    @Test
    void searchItemByBlankTextTest() {
        assertEquals(new ArrayList<>(), itemService.searchItem((" "), Pageable.unpaged()));
    }

    @Test
    void searchItemByTextTest() {
        assertEquals(List.of(ItemMapper.toItemDto(item1)).get(0).getId(), itemService.searchItem(("descrip"), Pageable.unpaged()).get(0).getId());
    }


    @Test
    void updateItemTest() {
        Item item = new Item(1L, "item1", "description1", true, user, null);
        itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));

        Item toUpdateItem = new Item();
        toUpdateItem.setAvailable(false);
        toUpdateItem.setDescription("updated");
        toUpdateItem.setName("updatedName");
        userService.getUserById(1L);
        itemRepository.findById(1L);

        itemService.updateItem(1L, 1L, ItemMapper.toItemDto(toUpdateItem));
        toUpdateItem.setId(1L);
        toUpdateItem.setOwner(user);

        assertEquals(itemRepository.findById(item1.getId()).orElse(null).getDescription(), toUpdateItem.getDescription());
    }

    @Test
    void updateItemNotAcceptedTest() {
        Item item = new Item(1L, "item1", "description1", true, user, null);
        itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));

        Item toUpdateItem = new Item();
        toUpdateItem.setAvailable(false);
        toUpdateItem.setDescription("updated");
        toUpdateItem.setName("updatedName");
        userService.getUserById(1L);
        itemRepository.findById(1L);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, 2L, ItemMapper.toItemDto(toUpdateItem)));
    }

    @Test
    void createBookingTest_ButUserNotFound() {

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(100L)
                .id(100L)
                .start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDtoRequest, 100L));

    }

    @Test
    void createBookingTest_ButItemNotFound() {

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(166L)
                .id(111L)
                .start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDtoRequest, 1L));

    }

    @Test
    void createBookingTest_ValidationOwnerAndBookerAreTheSame() {

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(1L)
                .id(111L)
                .start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDtoRequest, 1L));

    }

    @Test
    void createBookingTest_ItemNotAvailable() {
        item.setAvailable(false);
        itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(1L)
                .id(111L)
                .start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDtoRequest, 2L));

    }

    @Test
    void createBookingTest_BookingGetStartIsAfterEnd() {
        UserDto userDto666 = UserDto.builder().id(666L).name("name666").email("email666@email.ru").build();
        userService.createUser(userDto666);
        User user666 = UserMapper.fromDtoToUser(userDto666);
        Item item = new Item(66L, "item66", "description66", true, user666, null);

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(1L)
                .id(112L)
                .start(LocalDateTime.now().plusDays(6))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertThrows(ValidationException.class, () -> bookingService.createBooking(bookingDtoRequest, 1L));

    }

    @Test
    void createBookingTest_BookingGetEndEqualsStart() {
        Item item = new Item(66L, "item66", "description66", true, user, null);
        itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));
        LocalDateTime ldt = LocalDateTime.now();
        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(66L)
                .id(111L)
                .start(ldt)
                .end(ldt)
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(bookingDtoRequest, 1L));

    }

    @Test
    void createBookingTest_Found() {

        Item item = new Item(16L, "item16", "description16", true, user, null);
        itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(1L)
                .id(111L)
                .start(LocalDateTime.now().plusMinutes(10))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        BookingDtoResponse keyBookingResponse = bookingService.createBooking(bookingDtoRequest, 2L);

        assertEquals(keyBookingResponse.getItemId(), 1L);

    }

    @Test
    void itemMapperTest() {
        Item item16 = new Item(16L, "item16", "description16", true, user, null);
        itemService.createItem(item.getOwner().getId(), ItemMapper.toItemDtoReq(item));
        User user16 = new User(16L, "user16", "user16@mail.ru");
        userService.createUser(UserMapper.userToDto(user16));
        User user6 = new User(6L, "user6", "user6@mail.ru");
        userService.createUser(UserMapper.userToDto(user16));

        Booking nextBooking = new Booking(1L,
                LocalDateTime.now().plusMinutes(2),
                localDateTime.now().plusDays(2),
                item16,
                user16,
                BookingStatus.APPROVED,
                State.CURRENT);
        Booking lastBooking = new Booking(1L,
                LocalDateTime.now().minusDays(2),
                localDateTime.now().minusDays(1),
                item16,
                user6,
                BookingStatus.APPROVED,
                State.CURRENT);

        Comment comment = new Comment(1L, "hi", item16, user6, LocalDateTime.now().minusDays(1));
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);

        ItemDtoWithBooking itemDtoWithBooking = ItemMapper.toItemDtoWithBooking(comments, lastBooking, nextBooking, item16);
        assertNotNull(itemDtoWithBooking);
        assertEquals(itemDtoWithBooking.getComments().size(), (comments).size());
        assertEquals(itemDtoWithBooking.getComments().get(0).getText(), (comments).get(0).getText());
        assertEquals(itemDtoWithBooking.getComments().get(0).getId(), (comments).get(0).getId());
        assertEquals(itemDtoWithBooking.getComments().get(0).getAuthorName(), (comments).get(0).getUser().getName());
        assertEquals(itemDtoWithBooking.getComments().get(0).getCreated(), (comments).get(0).getCreated());
        assertEquals(itemDtoWithBooking.getLastBooking().getId(), lastBooking.getId());
        assertEquals(itemDtoWithBooking.getLastBooking().getBookerId(), lastBooking.getBooker().getId());
    }

}
