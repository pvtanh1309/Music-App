package nhantr.musicapp.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import nhantr.musicapp.enums.Role;
import nhantr.musicapp.enums.UserStatus;
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
public class UserAdminResponse {

    private UUID id;
    private String username;
    private String email;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
}
