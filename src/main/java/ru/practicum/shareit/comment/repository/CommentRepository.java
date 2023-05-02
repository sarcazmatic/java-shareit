package ru.practicum.shareit.comment.repository;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByItem_IdIn(List<Long> itemsIds, Sort sort);

    List<Comment> findByItem_IdOrderByCreatedDesc(Long itemId);

}
