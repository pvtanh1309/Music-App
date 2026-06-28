package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import nhantr.musicapp.dto.request.ArtistRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.ArtistResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.service.ArtistService;
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
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<ArtistResponse>>> getArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(APIResponse.success(artistService.getArtists(page, size, q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<ArtistResponse>> getArtist(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResponse.success(artistService.getArtist(id)));
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<APIResponse<PageResponse<SongResponse>>> getArtistSongs(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResponse.success(artistService.getArtistSongs(id, page, size)));
    }

    @GetMapping("/{id}/albums")
    public ResponseEntity<APIResponse<Map<String, Object>>> getArtistAlbums(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResponse.success(Map.of("artistId", id, "message", "Use /api/albums?artistId=... for filtered list")));
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<APIResponse<Map<String, Long>>> getFollowers(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResponse.success(Map.of("followerCount", artistService.getFollowers(id))));
    }

    @PostMapping
    public ResponseEntity<APIResponse<ArtistResponse>> create(@Valid @RequestBody ArtistRequest request) {
        APIResponse<ArtistResponse> response = APIResponse.success(artistService.create(request));
        response.setCode(201);
        response.setMessage("Artist created");
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<ArtistResponse>> update(@PathVariable UUID id, @Valid @RequestBody ArtistRequest request) {
        return ResponseEntity.ok(APIResponse.success(artistService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable UUID id) {
        artistService.delete(id);
        return ResponseEntity.ok(APIResponse.success("Artist deleted"));
    }
}
