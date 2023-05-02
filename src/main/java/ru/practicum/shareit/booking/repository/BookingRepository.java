package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItem_IdInAndStatus(List<Long> itemsIds, BookingStatus bookingStatus, Sort sort);

    List<Booking> findAllByBooker_Id(Long id);

    List<Booking> findAllByItem_Owner_Id(Long id);

    List<Booking> findAllByItem_Owner_Id(Long userId, Pageable pageable);

    List<Booking> findAllByBooker_IdOrderByIdDesc(Long userId, Pageable pageable);

    List<Booking> findAllByItem_Owner_IdOrderByStartDesc(Long userId);

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long userId, Pageable pageable);

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long userId);

    List<Booking> findBookingByBookerIdAndStartIsBeforeAndEndIsAfter(long userId,
                                                                     LocalDateTime dateTime1, LocalDateTime dateTime2, Pageable pageable);

    List<Booking> findBookingByItemOwnerIdAndStartIsBeforeAndEndIsAfter(long userId,
                                                                        LocalDateTime dateTime1, LocalDateTime dateTime2, Pageable pageable);


    Optional<Booking> findFirstByItem_IdAndEndBeforeAndStatusOrderByEndDesc(Long itemId, LocalDateTime now, BookingStatus bookingStatus);

    Optional<Booking> findTopByItem_IdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now, BookingStatus bookingStatus);

    Booking findFirstByItem_IdAndEndAfterAndStatusOrderByEndDesc(Long itemId, LocalDateTime end, BookingStatus bookingStatus);

    List<Booking> findByBooker_IdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    Booking findFirstByItem_IdAndEndBeforeOrderByEndDesc(Long itemId, LocalDateTime end);

    Booking findTopByItem_IdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime start);

    List<Booking> findAllByItem_Owner_IdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :id AND b.end < :currentTime AND upper(b.status) = ('APPROVED')" +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdStatePastPageable(@Param("id") long id, @Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.booker.id = :id AND b.end < :currentTime AND upper(b.status) = UPPER('APPROVED')" +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdStatePast(@Param("id") long id, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.start > :currentTime ORDER BY b.start DESC")
    List<Booking> findFuturePageable(@Param("userId") long useId, @Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :userId AND b.start > :currentTime ORDER BY b.start DESC")
    List<Booking> findFuture(@Param("userId") long useId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE i.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findOwnerAll(long ownerId);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE i.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findOwnerAllPageable(long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE  i.owner.id = :userId AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findOwnerFuture(@Param("userId") long userId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE  i.owner.id = :userId AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findOwnerFuturePageable(@Param("userId") long userId, @Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE i.owner.id = :userId AND b.end < :currentTime ORDER BY b.id DESC")
    List<Booking> findOwnerPast(@Param("userId") long userId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE i.owner.id = :userId AND b.end < :currentTime ORDER BY b.id DESC")
    List<Booking> findOwnerPastPageable(@Param("userId") long userId, @Param("currentTime") LocalDateTime currentTime, Pageable pageable);

}