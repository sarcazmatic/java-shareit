package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoRequest;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";


    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getListItemByUserId(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of("from", from, "size", size);
        return get("", userId, parameters);
    }

    public ResponseEntity<Object> getItemById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> createItem(Long userId, ItemDtoRequest itemDtoRequest) {
        return post("", userId, itemDtoRequest);
    }

    public ResponseEntity<Object> addComment(long itemId, long userId, CommentDtoRequest commentDtoRequest) {
        return post("/" + itemId + "/comment", userId, commentDtoRequest);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemDtoRequest itemDtoRequest) {
        return patch("/" + itemId, userId, itemDtoRequest);
    }

    public ResponseEntity<Object> searchItem(Long userId, String text, int from, int size) {
        Map<String, Object> parameters = Map.of("text", text, "from", from, "size", size);
        return get("/search?text={text}", userId, parameters);
    }

}
