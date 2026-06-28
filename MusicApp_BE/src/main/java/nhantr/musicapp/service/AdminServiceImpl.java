package nhantr.musicapp.service;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.request.BanUserRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.StatsResponse;
import nhantr.musicapp.dto.response.UserAdminResponse;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.enums.UserStatus;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.repository.ArtistRepository;
import nhantr.musicapp.repository.ListeningHistoryRepository;
import nhantr.musicapp.repository.PlaylistRepository;
import nhantr.musicapp.repository.SongRepository;
import nhantr.musicapp.repository.UploadRepository;
import nhantr.musicapp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final PlaylistRepository playlistRepository;
    private final UploadRepository uploadRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;


    @Override
    public PageResponse<UserAdminResponse> getUsers(int page, int size) {
        log.info("Admin get users page={}, size={}", page, size);
        Page<UserAdminResponse> responsePage = userRepository
                .findAll(PageRequest.of(page, size))
                .map(this::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public UserAdminResponse getUser(UUID id) {
        log.info("Admin get user detail id={}", id);
        return toResponse(getUserEntity(id));
    }

    @Override
    public void ban(UUID id, BanUserRequest request) {
        log.info("Admin ban user id={}, reason={}", id, request.getReason());
        User user = getUserEntity(id);
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
    }

    @Override
    public void unban(UUID id) {
        log.info("Admin unban user id={}", id);
        User user = getUserEntity(id);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public void deleteSong(UUID id) {
        log.info("Admin delete song id={}", id);
        songRepository.deleteById(id);
    }

    @Override
    public StatsResponse stats() {
        log.info("Admin get system stats");
        return StatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalSongs(songRepository.count())
                .totalArtists(artistRepository.count())
                .totalPlaylists(playlistRepository.count())
                .pendingUploads(uploadRepository.countByStatus("PENDING"))
                .totalListens(listeningHistoryRepository.count())
                .build();
    }

    private User getUserEntity(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage()));
    }

    private UserAdminResponse toResponse(User user) {
        return UserAdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
            .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
