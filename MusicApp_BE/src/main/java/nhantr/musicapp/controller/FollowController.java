package nhantr.musicapp.controller;

import java.util.Map;
import java.util.UUID;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.ArtistSummaryResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/artists/{artistId}")
    public ResponseEntity<APIResponse<String>> follow(@PathVariable UUID artistId) {
        followService.followArtist(artistId);
        return ResponseEntity.ok(APIResponse.success("Following artist"));
    }

    @DeleteMapping("/artists/{artistId}")
    public ResponseEntity<APIResponse<String>> unfollow(@PathVariable UUID artistId) {
        followService.unfollowArtist(artistId);
        return ResponseEntity.ok(APIResponse.success("Unfollowed artist"));
    }

    @GetMapping("/artists")
    public ResponseEntity<APIResponse<PageResponse<ArtistSummaryResponse>>> followedArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResponse.success(followService.myFollowingArtists(page, size)));
    }

    @GetMapping("/artists/{artistId}/status")
    public ResponseEntity<APIResponse<Map<String, Boolean>>> status(@PathVariable UUID artistId) {
        return ResponseEntity.ok(APIResponse.success(Map.of("following", followService.isFollowing(artistId))));
    }
}
