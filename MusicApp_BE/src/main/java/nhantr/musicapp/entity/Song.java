package nhantr.musicapp.entity;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    String title;

    @ManyToOne
    @JoinColumn(name = "artist_id")
    Artist artist;

    @ManyToOne
    @JoinColumn(name = "album_id")
    Album album;

    int duration;

    @Column(columnDefinition = "text")
    String fileUrl;

    @Column(columnDefinition = "text")
    String coverUrl;

    LocalDateTime createdAt;
}