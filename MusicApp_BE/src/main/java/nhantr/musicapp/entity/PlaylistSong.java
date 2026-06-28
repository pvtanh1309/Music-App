package nhantr.musicapp.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "playlist_songs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlaylistSong {

    @EmbeddedId
    PlaylistSongId id;

    @ManyToOne
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    Playlist playlist;

    @ManyToOne
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    Song song;
}