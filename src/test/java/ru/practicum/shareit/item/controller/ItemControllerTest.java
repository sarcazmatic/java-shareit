package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ItemService itemService;
    public static final String SHARER_USER_ID = "X-Sharer-User-Id";
    private UserDto userDto;
    private ItemDtoResponse itemDto;
    private CommentDtoResponse commentDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("userName")
                .email("name@mail.com")
                .build();
        itemDto = ItemDtoResponse.builder()
                .id(1L)
                .name("item name")
                .description("item description")
                .available(false)
                .build();
        commentDto = CommentDtoResponse.builder()
                .id(1L)
                .text("comment text")
                .authorName("Vasya")
                .build();
    }

    @SneakyThrows
    @Test
    void create_whenItemDtoWithValidFields_thenReturnItemDto() {
        ItemDtoRequest itemToSave = ItemDtoRequest.builder()
                .name("item name")
                .description("item description")
                .available(false)
                .build();

        when(itemService.createItem(anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemToSave))
                        .header(SHARER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));
        verify(itemService, times(1))
                .createItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void create_whenItemDtoWithNotValidFields_thenReturnStatusBadRequest() {
        ItemDtoResponse itemDto1 = ItemDtoResponse.builder().build();

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto1))
                        .header(SHARER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(itemService, never())
                .createItem(anyLong(), any());
    }

    @SneakyThrows
    @Test
    void getById_whenInvoked_thenGetItemDtoResponse() {
        long itemId = 1L;
        long userId = 1L;
        ItemDtoWithBooking itemDtoResponse = ItemDtoWithBooking.builder()
                .id(itemId)
                .name("item name")
                .description("item description")
                .available(false)
                .build();
        Mockito
                .when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDtoResponse);

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));
        verify(itemService, times(1))
                .getItemById(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    void getAll_whenInvoked_thenGetEmptyList() {
        long userId = 1L;
        when(itemService.getListItemByUserId(anyLong(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/items")
                        .header(SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        verify(itemService, times(1))
                .getListItemByUserId(anyLong(), any(Pageable.class));
    }

    @SneakyThrows
    @Test
    void update_whenDtoHasFields_thenReturnItemDtoUpdated() {
        long userId = 1L;
        long itemId = 1L;
        ItemDtoRequest itemUpdate = ItemDtoRequest.builder()
                .name("item name")
                .description("item description")
                .available(false)
                .build();
        when(itemService.updateItem(anyLong(), anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemUpdate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())));
        verify(itemService, times(1))
                .updateItem(anyLong(), anyLong(), any());
    }

    @SneakyThrows
    @Test
    void getSearchResults_whenTextNotEmpty_thenGetListWithItemDto() {
        when(itemService.searchItem(anyString(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", "asasd")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$", hasSize(1)));
        verify(itemService, times(1))
                .searchItem(anyString(), any(Pageable.class));
    }

    @SneakyThrows
    @Test
    void getSearchResults_whenTextIsEmpty_thenGetEmptyList() {
        mvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        verify(itemService, never())
                .searchItem(anyString(), any(Pageable.class));
    }

    @SneakyThrows
    @Test
    void postComment_whenInvoked_thenStatusIsOk() {
        long itemId = 1L;
        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(mapper.writeValueAsString(commentDto))
                        .header(SHARER_USER_ID, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
        verify(itemService, times(1))
                .addComment(anyLong(), anyLong(), any());
    }
}
