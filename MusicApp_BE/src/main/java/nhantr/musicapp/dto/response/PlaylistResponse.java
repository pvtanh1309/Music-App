package nhantr.musicapp.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {

    private UUID id;
    private String name;
    private boolean isPublic;
    private int songCount;
    private List<SongResponse> songs;
    private LocalDateTime createdAt;
}
