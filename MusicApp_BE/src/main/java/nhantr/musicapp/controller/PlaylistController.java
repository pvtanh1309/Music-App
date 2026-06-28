package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import nhantr.musicapp.dto.request.CreatePlaylistRequest;
import nhantr.musicapp.dto.request.PlaylistSongRequest;
import nhantr.musicapp.dto.request.UpdatePlaylistRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.PlaylistResponse;
import nhantr.musicapp.service.PlaylistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<PlaylistResponse>>> getMyPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(APIResponse.success(playlistService.getMyPlaylists(page, size, q)));
    }

    @GetMapping("/public")
    public ResponseEntity<APIResponse<PageResponse<PlaylistResponse>>> getPublicPlaylists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(APIResponse.success(playlistService.getPublicPlaylists(page, size, q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<PlaylistResponse>> getPlaylist(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResponse.success(playlistService.getPlaylist(id)));
    }

    @PostMapping
    public ResponseEntity<APIResponse<PlaylistResponse>> create(@Valid @RequestBody CreatePlaylistRequest request) {
        APIResponse<PlaylistResponse> response = APIResponse.success(playlistService.create(request));
        response.setCode(201);
        response.setMessage("Playlist created");
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<PlaylistResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlaylistRequest request) {
        return ResponseEntity.ok(APIResponse.success(playlistService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable UUID id) {
        playlistService.delete(id);
        return ResponseEntity.ok(APIResponse.success("Playlist deleted"));
    }

    @PostMapping("/{id}/songs")
    public ResponseEntity<APIResponse<String>> addSong(@PathVariable UUID id, @Valid @RequestBody PlaylistSongRequest request) {
        playlistService.addSong(id, request);
        return ResponseEntity.ok(APIResponse.success("Song added to playlist"));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<APIResponse<String>> removeSong(@PathVariable UUID id, @PathVariable UUID songId) {
        playlistService.removeSong(id, songId);
        return ResponseEntity.ok(APIResponse.success("Song removed from playlist"));
    }
}
