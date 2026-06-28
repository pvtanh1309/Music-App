package nhantr.musicapp.dto.response;

import java.time.LocalDateTime;
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
public class SongResponse {

	private UUID id;
	private String title;
	private int duration;
	private String coverUrl;
	private String fileUrl;
	private ArtistSummaryResponse artist;
	private AlbumSummaryResponse album;
	private long listenerCount;
	private LocalDateTime createdAt;
}
