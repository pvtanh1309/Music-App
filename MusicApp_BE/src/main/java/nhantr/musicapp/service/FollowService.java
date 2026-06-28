package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.response.ArtistSummaryResponse;
import nhantr.musicapp.dto.response.PageResponse;

public interface FollowService {

    void followArtist(UUID artistId);

    void unfollowArtist(UUID artistId);

    PageResponse<ArtistSummaryResponse> myFollowingArtists(int page, int size);

    boolean isFollowing(UUID artistId);
}
