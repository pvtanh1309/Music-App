package nhantr.musicapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UpdatePasswordRequest {

    @NotBlank(message = "newPassword is required")
    String newPassword;

    @NotBlank(message = "confirmNewPassword is required")
    String confirmNewPassword;
}
