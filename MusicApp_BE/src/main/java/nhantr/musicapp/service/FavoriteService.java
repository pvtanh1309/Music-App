package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.response.FavoriteResponse;
import nhantr.musicapp.dto.response.PageResponse;

public interface FavoriteService {

    PageResponse<FavoriteResponse> getFavorites(int page, int size, String query);

    void like(UUID songId);

    void unlike(UUID songId);

    boolean liked(UUID songId);
}
