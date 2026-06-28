package nhantr.musicapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import nhantr.musicapp.enums.Role;
import nhantr.musicapp.enums.UserStatus;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    String username;
    String email;
    Role role;
    UserStatus status;
}
