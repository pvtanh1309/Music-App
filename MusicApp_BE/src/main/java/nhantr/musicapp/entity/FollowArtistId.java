package nhantr.musicapp.entity;

import java.io.Serializable;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowArtistId implements Serializable {

    UUID userId;
    UUID artistId;
}