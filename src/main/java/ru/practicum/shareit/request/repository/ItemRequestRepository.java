package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {


    List<ItemRequest> findAllByRequester_IdOrderByCreatedAsc(Long userId);

    List<ItemRequest> findByRequester_IdNot(Long userId, Pageable pageable);

}
