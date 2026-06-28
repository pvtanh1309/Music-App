package nhantr.musicapp.service;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.request.ArtistRequest;
import nhantr.musicapp.dto.response.ArtistResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.entity.Artist;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.ArtistRepository;
import nhantr.musicapp.repository.FollowArtistRepository;
import nhantr.musicapp.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final FollowArtistRepository followArtistRepository;
    private final SongRepository songRepository;
    private final CurrentUserService currentUserService;
    private final MusicMapper musicMapper;

    @Override
    public PageResponse<ArtistResponse> getArtists(int page, int size, String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        log.info("Get artists page={}, size={}, query={}", page, size, normalizedQuery);
        Page<ArtistResponse> responsePage = (normalizedQuery.isEmpty()
                ? artistRepository.findAll(PageRequest.of(page, size))
                : artistRepository.search(normalizedQuery, PageRequest.of(page, size)))
                .map(this::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public ArtistResponse getArtist(UUID id) {
        log.info("Get artist detail id={}", id);
        return toResponse(getArtistEntity(id));
    }

    @Override
    public PageResponse<SongResponse> getArtistSongs(UUID id, int page, int size) {
        log.info("Get artist songs artistId={}, page={}, size={}", id, page, size);
        return PageResponse.fromPage(songRepository
                .findByArtistId(id, PageRequest.of(page, size))
                .map(musicMapper::toSongResponse));
    }

    @Override
    public ArtistResponse create(ArtistRequest request) {
        log.info("Create artist name={}", request.getName());
        Artist artist = Artist.builder()
                .name(request.getName())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .createdAt(LocalDateTime.now())
                .build();
        return toResponse(artistRepository.save(artist));
    }

    @Override
    public ArtistResponse update(UUID id, ArtistRequest request) {
        log.info("Update artist id={}", id);
        Artist artist = getArtistEntity(id);
        artist.setName(request.getName());
        artist.setBio(request.getBio());
        artist.setAvatarUrl(request.getAvatarUrl());
        return toResponse(artistRepository.save(artist));
    }

    @Override
    public void delete(UUID id) {
        log.info("Delete artist id={}", id);
        artistRepository.delete(getArtistEntity(id));
    }

    @Override
    public long getFollowers(UUID id) {
        log.info("Get followers count artistId={}", id);
        return followArtistRepository.countByArtistId(id);
    }

    private Artist getArtistEntity(UUID id) {
        return artistRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND.getCode(), ErrorCode.ARTIST_NOT_FOUND.getMessage()));
    }

    private ArtistResponse toResponse(Artist artist) {
        Boolean isFollowing = null;
        try {
            User user = currentUserService.getCurrentUserEntity();
            isFollowing = followArtistRepository.existsByUserIdAndArtistId(user.getId(), artist.getId());
        } catch (Exception ignored) {
            isFollowing = null;
        }

        return ArtistResponse.builder()
                .id(artist.getId())
                .name(artist.getName())
                .bio(artist.getBio())
                .avatarUrl(artist.getAvatarUrl())
                .followerCount(followArtistRepository.countByArtistId(artist.getId()))
                .songCount(songRepository.findByArtistId(artist.getId(), PageRequest.of(0, 1)).getTotalElements())
                .isFollowing(isFollowing)
                .createdAt(artist.getCreatedAt())
                .build();
    }
}
