package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.utility.PageableMaker;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Mock
    ItemRepository itemRepository;

    @InjectMocks
    ItemService itemService;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemRequestService itemRequestService;
    static User user;
    static UserDto userDto;
    static Item item;
    static ItemDtoResponse itemDtoResponse;
    static ItemRequest itemRequest;
    static Comment comment;
    static CommentDtoResponse commentDto;
    static Booking booking1;
    static Booking booking2;

    static User user100 = new User(100L, "user100", "user100@mail.ru");

    static Item item100 = new Item(100L, "item100", "item100description", true, user100, null);
    static ItemDtoRequest itemDto100Request = ItemMapper.toItemDtoReq(item100);

    static Comment comment100 = new Comment(100L, "comment100", item100, user100, LocalDateTime.now());


    @Autowired
    public ItemServiceTest(UserRepository userRepository, UserService userService, ItemService itemService, ItemRepository itemRepository, ItemRequestRepository itemRequestRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.itemService = itemService;
        this.itemRepository = itemRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.bookingRepository = bookingRepository;
    }



@BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("userName")
                .email("name@mail.com")
                .build();
        itemDtoResponse = ItemDtoResponse.builder()
                .id(1L)
                .name("item name")
                .description("item description")
                .available(false)
                .build();
        user = UserMapper.fromDtoToUser(userDto);
        item = ItemMapper.toItem(itemDtoResponse);
        commentDto = CommentDtoResponse.builder()
                .id(1L)
                .text("comment text")
                .authorName("Vasya")
                .build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("request description")
                .build();
        comment = Comment.builder()
                .id(1L)
                .text("asdasd")
                .item(item)
                .user(user)
                .created(LocalDateTime.now())
                .build();
        booking1 = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
        booking2 = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void save_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 50L;
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(userId, itemDto100Request));
        assertThat(exception.getMessage(), equalTo("Пользователь с ID 50 не найден"));
    }

    @Test
    void save_whenRequestNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        User testUser = User.builder()
                .name("TestName")
                .build();
        itemDtoResponse.setRequestId(1L);
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));
        Mockito.when(itemRequestRepository.findById(itemDtoResponse.getRequestId()))
                .thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(userId, ItemMapper.toItemDtoReq(ItemMapper.toItem(itemDtoResponse))));
        assertThat(exception.getMessage(), equalTo("Запрос не существует!"));
    }

    @Test
    void getById_whenItemNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1000L;
        User user = UserMapper.fromDtoToUser(userDto);
        Item item = ItemMapper.toItem(itemDtoResponse);
        item.setOwner(user);
        Mockito.when(itemRepository.findById(userId))
                .thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(itemId, userId));
        assertThat(exception.getMessage(), equalTo("Вещь с ID 1000 не найдена"));
    }

    @Test
    void getSearchResults_whenInvoked_thenReturnListWithOneItem() {
        String text = "item";
        int from = 0;
        int size = 5;
        Pageable pageable = PageableMaker.makePageable(from, size, Sort.by(Sort.Direction.ASC, "id"));
        Mockito.when(itemRepository.searchItem(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.singletonList(item));

        List<ItemDtoResponse> searchResults = itemService.searchItem(text, pageable);
        assertThat(searchResults, hasSize(1));
    }

    @Test
    void update_whenItemNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1000L;
        itemDtoResponse.setName("BeforeUpdate");
        itemDtoResponse.setDescription("BeforeUpdate");
        ItemDtoResponse itemWithUpdates = ItemDtoResponse.builder()
                .name("UpdatedName")
                .description("UpdatedDescription")
                .build();
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(null);


        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemId, itemWithUpdates));
        assertThat(exception.getMessage(), equalTo("Предмет с ID 1000 не найден"));
    }

    @Test
    void update_whenItemDtoWithWrongOwner_thenNotFoundExceptionThrown() {
        long userId = 10L;
        long itemId = 1L;
        item.setName("BeforeUpdate");
        item.setDescription("BeforeUpdate");
        item.setOwner(user);
        ItemDtoResponse itemWithUpdates = ItemDtoResponse.builder()
                .name("UpdatedName")
                .description("UpdatedDescription")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(userId, itemId, itemWithUpdates));
        assertThat(exception.getMessage(), equalTo("Предмет не доступен для брони"));
    }

  @Test
    void postComment_whenStatusWrong_thenValidationExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        commentDto.setText("testText");
        CommentDtoRequest cdr = CommentDtoRequest.builder()
                .text(comment.getText())
                .build();
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.ofNullable(user));
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.ofNullable(item));


      ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(userId, itemId, cdr));
        assertThat(exception.getMessage(), equalTo("Не найдено брони у этого пользователя"));
    }


    @Test
    void postComment_whenUserNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        commentDto.setText("testText");
        CommentDtoRequest cdr = CommentDtoRequest.builder().text(comment.getText()).build();
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());


        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(itemId, userId, cdr));
        assertThat(exception.getMessage(), equalTo("Не найдено брони у этого пользователя"));
    }

    @Test
    void postComment_whenItemNotFound_thenNotFoundExceptionThrown() {
        long userId = 1L;
        long itemId = 1L;
        commentDto.setText("testText");
        CommentDtoRequest cdr = CommentDtoRequest.builder().text(comment.getText()).build();
        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user100));
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.addComment(itemId, userId, cdr));
        assertThat(exception.getMessage(), equalTo("Не найдено брони у этого пользователя"));
    }

}
