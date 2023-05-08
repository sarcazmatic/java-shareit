package ru.practicum.shareit.booking.model;

import lombok.*;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings", schema = "public")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "start_date")
    private LocalDateTime start;
    @Column(name = "end_date")
    private LocalDateTime end;
    @ManyToOne
    @CollectionTable(name = "booking_items", joinColumns = @JoinColumn(name = "item_id", nullable = false))
    private Item item;
    @ManyToOne
    @CollectionTable(name = "booking_users", joinColumns = @JoinColumn(name = "booker_id", nullable = false))
    private User booker;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;

}
