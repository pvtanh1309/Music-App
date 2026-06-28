package nhantr.musicapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AlbumRequest {

    @NotBlank(message = "name is required")
    String name;

    @NotNull(message = "artistId is required")
    UUID artistId;

    LocalDate releaseDate;
    String coverUrl;
}
