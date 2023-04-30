package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private final UserDto userDto1 = UserDto.builder().id(1L).name("user1").email("user1@user1.ru").build();

    private final ItemDtoRequest itemDto1 = ItemDtoRequest.builder().id(1L).name("item1").description("description1").available(true).requestId(1L).build();
    private final ItemDtoResponse itemDto2 = ItemDtoResponse.builder().id(1L).name("item1").description("description1").available(true).requestId(1L).build();

    private final ItemDtoWithBooking itemDtoWithBooking = new ItemDtoWithBooking(1L, "name11", "description11",
            true, null, null, null);
    private final CommentDtoResponse commentDto1 = CommentDtoResponse.builder().id(1L).text("comment1").authorName("user1").created(LocalDateTime.now()).build();

    @Test
    void createItemTest() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenReturn(itemDto2);

        mockMvc.perform(post("/items")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto1))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto1.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto1.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto1.getAvailable()), Boolean.class));

        verify(itemService, times(1))
                .createItem(anyLong(), any());
    }

    @Test
    void updateItemTest() throws Exception {

        when(itemService.updateItem(anyLong(), anyLong(), any()))
                .thenReturn(itemDto2);

        mockMvc.perform(patch("/items/" + itemDto1.getId())
                        .content(mapper.writeValueAsString(itemDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto1.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto1.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDto1.getAvailable()), Boolean.class));

        verify(itemService, times(1))
                .updateItem(anyLong(), anyLong(), any());
    }

    @Test
    void getItemByIdTest() throws Exception {

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDtoWithBooking);

        mockMvc.perform(get("/items/" + itemDto1.getId())
                        .header("X-Sharer-User-Id", userDto1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoWithBooking.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoWithBooking.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDtoWithBooking.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(itemDtoWithBooking.getAvailable()), Boolean.class));

        verify(itemService, times(1))
                .getItemById(itemDto1.getId(), userDto1.getId());
    }

    @Test
    void retrieveAllItemTest() throws Exception {
        when(itemService.getListItemByUserId(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userDto1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemService, times(1))
                .getListItemByUserId(anyLong(), any());
    }

    @Test
    void searchByText() throws Exception {
        when(itemService.searchItem(anyString(), any()))
                .thenReturn(List.of(itemDto2));

        mockMvc.perform(get("/items/search").param("text", "item1")
                        .content(mapper.writeValueAsString(itemDto1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto1.getName()), String.class));

        verify(itemService, times(1))
                .searchItem(anyString(), any());
    }

    @Test
    void addCommentTest() throws Exception {

        when(itemService.addComment(anyLong(), anyLong(), any()))
                .thenReturn(commentDto1);

        mockMvc.perform(post("/items/{id}/comment", itemDto1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto1))
                        .header("X-Sharer-User-Id", userDto1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto1.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto1.getText())))
                .andExpect(jsonPath("$.authorName", is(userDto1.getName())));
    }
}
