package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.request.id IS not null")
    List<Item> findAllByRequestIsPresent();

    @Query("SELECT i.id FROM Item i WHERE i.owner.id = :id")
    List<Long> findAllIdByOwnerId(@Param("id") Long ownerId, Pageable pageable);

    List<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    List<Item> findAllByRequest_Id(Long requestId, Sort sort);

    @Query("SELECT i FROM Item i WHERE i.available = true " +
            " AND (LOWER(i.name) LIKE LOWER(CONCAT('%',:text,'%') ) " +
            " OR LOWER(i.description) LIKE LOWER(CONCAT('%',:text,'%') ) )")
    List<Item> searchItem(@Param("text") String text, Pageable pageable);
}
