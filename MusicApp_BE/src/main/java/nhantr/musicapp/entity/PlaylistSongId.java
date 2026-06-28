package nhantr.musicapp.entity;

import java.io.Serializable;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongId implements Serializable {

    UUID playlistId;
    UUID songId;
}