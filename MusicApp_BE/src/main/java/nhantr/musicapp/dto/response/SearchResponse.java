package nhantr.musicapp.dto.response;

import java.util.List;
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
public class SearchResponse {

    private List<SongResponse> songs;
    private List<ArtistSummaryResponse> artists;
    private List<AlbumSummaryResponse> albums;
    private List<PlaylistResponse> playlists;
}
