package nhantr.musicapp.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.request.HistoryRequest;
import nhantr.musicapp.dto.response.HistoryResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.entity.ListeningHistory;
import nhantr.musicapp.entity.Song;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.ListeningHistoryRepository;
import nhantr.musicapp.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class HistoryServiceImpl implements HistoryService {

    private final ListeningHistoryRepository listeningHistoryRepository;
    private final SongRepository songRepository;
    private final CurrentUserService currentUserService;
    private final MusicMapper musicMapper;

    public HistoryServiceImpl(
            ListeningHistoryRepository listeningHistoryRepository,
            SongRepository songRepository,
            CurrentUserService currentUserService,
            MusicMapper musicMapper) {
        this.listeningHistoryRepository = listeningHistoryRepository;
        this.songRepository = songRepository;
        this.currentUserService = currentUserService;
        this.musicMapper = musicMapper;
    }

    @Override
    public PageResponse<HistoryResponse> getHistory(int page, int size, String query) {
        User user = currentUserService.getCurrentUserEntity();
        String normalizedQuery = query == null ? "" : query.trim();
        log.info("Get listening history userId={}, page={}, size={}, query={}", user.getId(), page, size, normalizedQuery);

        Page<HistoryResponse> responsePage = (normalizedQuery.isEmpty()
                ? listeningHistoryRepository.findByUserIdOrderByListenedAtDesc(user.getId(), PageRequest.of(page, size))
                : listeningHistoryRepository.searchByUserId(user.getId(), normalizedQuery, PageRequest.of(page, size)))
                .map(history -> HistoryResponse.builder()
                        .id(history.getId())
                        .song(musicMapper.toSongResponse(history.getSong()))
                        .listenedAt(history.getListenedAt())
                        .build());

        return PageResponse.fromPage(responsePage);
    }

    @Override
    public void add(HistoryRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Add listening history userId={}, songId={}", user.getId(), request.getSongId());
        Song song = songRepository
                .findById(request.getSongId())
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND.getCode(), ErrorCode.SONG_NOT_FOUND.getMessage()));

        listeningHistoryRepository.save(ListeningHistory.builder()
                .user(user)
                .song(song)
                .listenedAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void clearAll() {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Clear listening history userId={}", user.getId());
        listeningHistoryRepository.deleteAllByUser(user);
    }

    @Override
    public void delete(UUID id) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Delete history item id={}, userId={}", id, user.getId());
        ListeningHistory history = listeningHistoryRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "History not found"));
        if (history.getUser() == null || !history.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
        }
        listeningHistoryRepository.delete(history);
    }
}
