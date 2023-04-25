package ru.practicum.shareit.booking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking", schema = "public")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "start_date")
    private LocalDateTime start;
    @Column(name = "end_date")
    private LocalDateTime end;
    @ManyToOne
    @CollectionTable(name = "items", joinColumns = @JoinColumn(name = "item_id", nullable = false))
    private Item item;
    @ManyToOne
    @CollectionTable(name = "users", joinColumns = @JoinColumn(name = "booker_id", nullable = false))
    private User booker;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

}
