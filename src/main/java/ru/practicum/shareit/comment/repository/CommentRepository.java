package ru.practicum.shareit.comment.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItemIn(List<Item> itemsIds, Sort sort);

    List<Comment> findByItem_IdOrderByCreatedDesc(Long itemId);

}
