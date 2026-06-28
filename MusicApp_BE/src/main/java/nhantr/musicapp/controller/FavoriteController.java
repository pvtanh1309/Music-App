package nhantr.musicapp.controller;

import java.util.Map;
import java.util.UUID;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.FavoriteResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.service.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<FavoriteResponse>>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(APIResponse.success(favoriteService.getFavorites(page, size, q)));
    }

    @PostMapping("/{songId}")
    public ResponseEntity<APIResponse<String>> like(@PathVariable UUID songId) {
        favoriteService.like(songId);
        return ResponseEntity.ok(APIResponse.success("Added to favorites"));
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<APIResponse<String>> unlike(@PathVariable UUID songId) {
        favoriteService.unlike(songId);
        return ResponseEntity.ok(APIResponse.success("Removed from favorites"));
    }

    @GetMapping("/{songId}/status")
    public ResponseEntity<APIResponse<Map<String, Boolean>>> status(@PathVariable UUID songId) {
        return ResponseEntity.ok(APIResponse.success(Map.of("liked", favoriteService.liked(songId))));
    }
}
