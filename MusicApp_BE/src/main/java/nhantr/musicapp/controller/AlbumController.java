package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import nhantr.musicapp.dto.request.AlbumRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.AlbumResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.service.AlbumService;
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
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<AlbumResponse>>> getAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UUID artistId) {
        return ResponseEntity.ok(APIResponse.success(
                artistId == null
                        ? albumService.getAlbums(page, size, q)
                        : albumService.getAlbumsByArtistId(artistId, page, size, q)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<AlbumResponse>> getAlbum(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResponse.success(albumService.getAlbum(id)));
    }

    @PostMapping
    public ResponseEntity<APIResponse<AlbumResponse>> create(@Valid @RequestBody AlbumRequest request) {
        APIResponse<AlbumResponse> response = APIResponse.success(albumService.create(request));
        response.setCode(201);
        response.setMessage("Album created");
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<AlbumResponse>> update(@PathVariable UUID id, @Valid @RequestBody AlbumRequest request) {
        return ResponseEntity.ok(APIResponse.success(albumService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable UUID id) {
        albumService.delete(id);
        return ResponseEntity.ok(APIResponse.success("Album deleted"));
    }
}
