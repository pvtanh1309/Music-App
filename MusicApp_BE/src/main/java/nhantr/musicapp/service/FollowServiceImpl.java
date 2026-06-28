package nhantr.musicapp.service;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.response.ArtistSummaryResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.entity.Artist;
import nhantr.musicapp.entity.FollowArtist;
import nhantr.musicapp.entity.FollowArtistId;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.repository.ArtistRepository;
import nhantr.musicapp.repository.FollowArtistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final FollowArtistRepository followArtistRepository;
    private final ArtistRepository artistRepository;
    private final CurrentUserService currentUserService;

    public FollowServiceImpl(
            FollowArtistRepository followArtistRepository,
            ArtistRepository artistRepository,
            CurrentUserService currentUserService) {
        this.followArtistRepository = followArtistRepository;
        this.artistRepository = artistRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public void followArtist(UUID artistId) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Follow artist artistId={}, userId={}", artistId, user.getId());

        if (followArtistRepository.existsByUserIdAndArtistId(user.getId(), artistId)) {
            return;
        }

        Artist artist = artistRepository
                .findById(artistId)
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND.getCode(), ErrorCode.ARTIST_NOT_FOUND.getMessage()));

        followArtistRepository.save(FollowArtist.builder()
                .id(new FollowArtistId(user.getId(), artistId))
                .user(user)
                .artist(artist)
                .build());
    }

    @Override
    @Transactional
    public void unfollowArtist(UUID artistId) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Unfollow artist artistId={}, userId={}", artistId, user.getId());
        followArtistRepository.deleteByUserIdAndArtistId(user.getId(), artistId);
    }

    @Override
    public PageResponse<ArtistSummaryResponse> myFollowingArtists(int page, int size) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Get followed artists userId={}, page={}, size={}", user.getId(), page, size);

        Page<ArtistSummaryResponse> result = followArtistRepository
                .findByUserId(user.getId(), PageRequest.of(page, size))
                .map(it -> ArtistSummaryResponse.builder()
                        .id(it.getArtist().getId())
                        .name(it.getArtist().getName())
                        .build());
        return PageResponse.fromPage(result);
    }

    @Override
    public boolean isFollowing(UUID artistId) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Check following status artistId={}, userId={}", artistId, user.getId());
        return followArtistRepository.existsByUserIdAndArtistId(user.getId(), artistId);
    }
}
