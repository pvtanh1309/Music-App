package nhantr.musicapp.service;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.response.FavoriteResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.entity.Favorite;
import nhantr.musicapp.entity.FavoriteId;
import nhantr.musicapp.entity.Song;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.FavoriteRepository;
import nhantr.musicapp.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final SongRepository songRepository;
    private final CurrentUserService currentUserService;
    private final MusicMapper musicMapper;

    @Override
    public PageResponse<FavoriteResponse> getFavorites(int page, int size, String query) {
        User user = currentUserService.getCurrentUserEntity();
        String normalizedQuery = query == null ? "" : query.trim();
        log.info("Get favorites userId={}, page={}, size={}, query={}", user.getId(), page, size, normalizedQuery);
        Page<FavoriteResponse> responsePage = (normalizedQuery.isEmpty()
                ? favoriteRepository.findByUserId(user.getId(), PageRequest.of(page, size))
                : favoriteRepository.searchByUserId(user.getId(), normalizedQuery, PageRequest.of(page, size)))
                .map(favorite -> FavoriteResponse.builder()
                        .song(musicMapper.toSongResponse(favorite.getSong()))
                        .addedAt(LocalDateTime.now())
                        .build());
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public void like(UUID songId) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Like song songId={}, userId={}", songId, user.getId());

        if (favoriteRepository.existsByUserIdAndSongId(user.getId(), songId)) {
            return;
        }

        Song song = songRepository
                .findById(songId)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND.getCode(), ErrorCode.SONG_NOT_FOUND.getMessage()));

        favoriteRepository.save(Favorite.builder()
                .id(new FavoriteId(user.getId(), songId))
                .user(user)
                .song(song)
                .build());
    }

    @Override
    @Transactional
    public void unlike(UUID songId) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Unlike song songId={}, userId={}", songId, user.getId());
        favoriteRepository.deleteByUserIdAndSongId(user.getId(), songId);
    }

    @Override
    public boolean liked(UUID songId) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Check liked status songId={}, userId={}", songId, user.getId());
        return favoriteRepository.existsByUserIdAndSongId(user.getId(), songId);
    }
}
