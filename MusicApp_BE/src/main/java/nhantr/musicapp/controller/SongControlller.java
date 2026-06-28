package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import nhantr.musicapp.dto.request.SongRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.service.SongService;
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
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/songs")
public class SongControlller {

    private final SongService songService;

    public SongControlller(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<SongResponse>>> getSongs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(APIResponse.success(songService.getSongs(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<SongResponse>> getSong(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResponse.success(songService.getSong(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<PageResponse<SongResponse>>> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "song") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(APIResponse.success(songService.search(query, type, page, size)));
    }

    @GetMapping("/trending")
    public ResponseEntity<APIResponse<PageResponse<SongResponse>>> trending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(APIResponse.success(songService.getTrending(page, size)));
    }

    @PostMapping
    public ResponseEntity<APIResponse<SongResponse>> create(@Valid @RequestBody SongRequest request) {
        APIResponse<SongResponse> response = APIResponse.success(songService.create(request));
        response.setCode(201);
        response.setMessage("Song created");
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<SongResponse>> createWithUpload(
            @RequestParam("fileSound") MultipartFile fileSound,
            @RequestParam(value = "fileImage", required = false) MultipartFile fileImage,
            @RequestParam("title") String title,
            @RequestParam(value = "artistId", required = false) UUID artistId,
            @RequestParam(value = "albumId", required = false) UUID albumId,
            @RequestParam(value = "duration", required = false, defaultValue = "0") int duration
    ) {
        SongRequest request = SongRequest.builder()
                .title(title)
                .artistId(artistId)
                .albumId(albumId)
                .duration(duration)
                .build();

        APIResponse<SongResponse> response = APIResponse.success(songService.createWithUpload(request, fileSound, fileImage));
        response.setCode(201);
        response.setMessage("Song uploaded and pending review");
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<SongResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SongRequest request) {
        return ResponseEntity.ok(APIResponse.success(songService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable UUID id) {
        songService.delete(id);
        return ResponseEntity.ok(APIResponse.success("Song deleted"));
    }
}
