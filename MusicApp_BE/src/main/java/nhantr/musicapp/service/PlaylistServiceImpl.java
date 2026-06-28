package nhantr.musicapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.request.CreatePlaylistRequest;
import nhantr.musicapp.dto.request.PlaylistSongRequest;
import nhantr.musicapp.dto.request.UpdatePlaylistRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.PlaylistResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.entity.Playlist;
import nhantr.musicapp.entity.PlaylistSong;
import nhantr.musicapp.entity.PlaylistSongId;
import nhantr.musicapp.entity.Song;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.PlaylistRepository;
import nhantr.musicapp.repository.PlaylistSongRepository;
import nhantr.musicapp.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final SongRepository songRepository;
    private final CurrentUserService currentUserService;
    private final MusicMapper musicMapper;

    public PlaylistServiceImpl(
            PlaylistRepository playlistRepository,
            PlaylistSongRepository playlistSongRepository,
            SongRepository songRepository,
            CurrentUserService currentUserService,
            MusicMapper musicMapper) {
        this.playlistRepository = playlistRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.songRepository = songRepository;
        this.currentUserService = currentUserService;
        this.musicMapper = musicMapper;
    }

    @Override
    public PageResponse<PlaylistResponse> getMyPlaylists(int page, int size, String query) {
        User user = currentUserService.getCurrentUserEntity();
        String normalizedQuery = query == null ? "" : query.trim();
        log.info("Get my playlists userId={}, page={}, size={}, query={}", user.getId(), page, size, normalizedQuery);
        Page<PlaylistResponse> responsePage = (normalizedQuery.isEmpty()
                ? playlistRepository.findByUserId(user.getId(), PageRequest.of(page, size))
                : playlistRepository.searchByUserId(user.getId(), normalizedQuery, PageRequest.of(page, size)))
                .map(this::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public PlaylistResponse getPlaylist(UUID id) {
        User user = null;
        try {
            user = currentUserService.getCurrentUserEntity();
        } catch (Exception ignored) {
            user = null;
        }

        log.info("Get playlist detail id={}", id);
        Playlist playlist = getPlaylistEntity(id);
        boolean allowed = playlist.isPublic() || (user != null && playlist.getUser() != null
                && playlist.getUser().getId().equals(user.getId()));
        if (!allowed) {
            throw new AppException(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
        }
        return toResponse(playlist);
    }

    @Override
    public PlaylistResponse create(CreatePlaylistRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Create playlist userId={}, name={}", user.getId(), request.getName());
        Playlist playlist = Playlist.builder()
                .user(user)
                .name(request.getName())
                .isPublic(request.isPublic())
                .createdAt(LocalDateTime.now())
                .build();
        return toResponse(playlistRepository.save(playlist));
    }

    @Override
    public PlaylistResponse update(UUID id, UpdatePlaylistRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Update playlist id={}, userId={}", id, user.getId());
        Playlist playlist = getOwnedPlaylist(id, user.getId());
        playlist.setName(request.getName());
        playlist.setPublic(request.isPublic());
        return toResponse(playlistRepository.save(playlist));
    }

    @Override
    public void delete(UUID id) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Delete playlist id={}, userId={}", id, user.getId());
        Playlist playlist = getOwnedPlaylist(id, user.getId());
        playlistRepository.delete(playlist);
    }

    @Override
    public void addSong(UUID id, PlaylistSongRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Add song to playlist playlistId={}, songId={}, userId={}", id, request.getSongId(), user.getId());

        Playlist playlist = getOwnedPlaylist(id, user.getId());
        Song song = songRepository
                .findById(request.getSongId())
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND.getCode(), ErrorCode.SONG_NOT_FOUND.getMessage()));

        if (playlistSongRepository.existsByPlaylistIdAndSongId(id, song.getId())) {
            return;
        }

        playlistSongRepository.save(PlaylistSong.builder()
                .id(new PlaylistSongId(id, song.getId()))
                .playlist(playlist)
                .song(song)
                .build());
    }

    @Override
    public void removeSong(UUID id, UUID songId) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Remove song from playlist playlistId={}, songId={}, userId={}", id, songId, user.getId());
        getOwnedPlaylist(id, user.getId());
        playlistSongRepository.deleteByPlaylistIdAndSongId(id, songId);
    }

    @Override
    public PageResponse<PlaylistResponse> getPublicPlaylists(int page, int size, String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        log.info("Get public playlists page={}, size={}, query={}", page, size, normalizedQuery);
        return PageResponse.fromPage((normalizedQuery.isEmpty()
                ? playlistRepository.findByIsPublicTrue(PageRequest.of(page, size))
                : playlistRepository.searchPublic(normalizedQuery, PageRequest.of(page, size)))
                .map(this::toResponse));
    }

    private Playlist getPlaylistEntity(UUID id) {
        return playlistRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PLAYLIST_NOT_FOUND.getCode(), ErrorCode.PLAYLIST_NOT_FOUND.getMessage()));
    }

    private Playlist getOwnedPlaylist(UUID id, UUID userId) {
        Playlist playlist = getPlaylistEntity(id);
        if (playlist.getUser() == null || !playlist.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
        }
        return playlist;
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        List<SongResponse> songs = playlistSongRepository.findByPlaylistId(playlist.getId()).stream()
                .map(ps -> ps.getSong())
                .map(s -> musicMapper.toSongResponse(s))
                .toList();

        return musicMapper.toPlaylistResponse(playlist, songs, songs.size());
    }
}
