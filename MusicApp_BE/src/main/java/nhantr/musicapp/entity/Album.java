package nhantr.musicapp.entity;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;

@Entity
@Table(name = "albums")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String name;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    Artist artist;

    LocalDate releaseDate;

    String coverUrl;
}