package nhantr.musicapp.dto.response;

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
public class StatsResponse {

    private long totalUsers;
    private long totalSongs;
    private long totalArtists;
    private long totalPlaylists;
    private long pendingUploads;
    private long totalListens;
}
