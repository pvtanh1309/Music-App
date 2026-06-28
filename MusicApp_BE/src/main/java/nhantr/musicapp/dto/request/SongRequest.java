package nhantr.musicapp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.Data;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SongRequest {

	@NotBlank(message = "title is required")
	private String title;

	@NotNull(message = "artistId is required")
	private UUID artistId;

	private UUID albumId;

	@Min(value = 1, message = "duration must be > 0")
	private int duration;

	@NotBlank(message = "fileUrl is required")
	private String fileUrl;

	private String coverUrl;
}
