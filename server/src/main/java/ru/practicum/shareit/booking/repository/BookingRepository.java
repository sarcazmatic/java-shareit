package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByItemIdInAndStatus(List<Long> itemsIds, BookingStatus bookingStatus, Sort sort);

    List<Booking> findAllByItemOwnerId(Long userId, Pageable pageable);

    List<Booking> findAllByBookerIdOrderByIdDesc(Long userId, Pageable pageable);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId, Pageable pageable);

    List<Booking> findBookingByBookerIdAndStartIsBeforeAndEndIsAfter(long userId,
                                                                     LocalDateTime dateTime1,
                                                                     LocalDateTime dateTime2,
                                                                     Pageable pageable);

    List<Booking> findBookingByItemOwnerIdAndStartIsBeforeAndEndIsAfter(long userId,
                                                                        LocalDateTime dateTime1,
                                                                        LocalDateTime dateTime2,
                                                                        Pageable pageable);


    Optional<Booking> findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(Long itemId,
                                                                            LocalDateTime now,
                                                                            BookingStatus bookingStatus);

    Optional<Booking> findTopByItemIdAndStartAfterAndStatusOrderByStartAsc(Long itemId,
                                                                            LocalDateTime now,
                                                                            BookingStatus bookingStatus);

    Booking findFirstByItemIdAndEndAfterAndStatusOrderByEndDesc(Long itemId,
                                                                 LocalDateTime end,
                                                                 BookingStatus bookingStatus);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

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

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE i.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findOwnerAllPageable(long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE  i.owner.id = :userId AND b.start > :currentTime " +
            "ORDER BY b.start DESC")
    List<Booking> findOwnerFuturePageable(@Param("userId") long userId, @Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN b.item i ON b.item = i WHERE i.owner.id = :userId AND b.end < :currentTime ORDER BY b.id DESC")
    List<Booking> findOwnerPastPageable(@Param("userId") long userId, @Param("currentTime") LocalDateTime currentTime, Pageable pageable);

}