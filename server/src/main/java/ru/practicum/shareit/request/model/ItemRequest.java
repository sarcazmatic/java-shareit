package ru.practicum.shareit.request.model;

import lombok.*;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "requests", schema = "public")
public class ItemRequest {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(nullable = false)
        private String description;
        @ManyToOne
        @JoinColumn(name = "requester_id")
        private User requester;
        @Column(name = "created")
        private LocalDateTime created;

}
