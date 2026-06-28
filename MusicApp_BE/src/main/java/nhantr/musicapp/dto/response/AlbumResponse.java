package nhantr.musicapp.dto.response;

import java.time.LocalDate;
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
public class AlbumResponse {

    private UUID id;
    private String name;
    private LocalDate releaseDate;
    private String coverUrl;
    private ArtistSummaryResponse artist;
    private List<SongResponse> songs;
    private int totalSongs;
    private int totalDuration;
}
