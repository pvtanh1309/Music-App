package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.request.AlbumRequest;
import nhantr.musicapp.dto.response.AlbumResponse;
import nhantr.musicapp.dto.response.PageResponse;

public interface AlbumService {

    PageResponse<AlbumResponse> getAlbums(int page, int size, String query);

    PageResponse<AlbumResponse> getAlbumsByArtistId(UUID artistId, int page, int size, String query);

    AlbumResponse getAlbum(UUID id);

    AlbumResponse create(AlbumRequest request);

    AlbumResponse update(UUID id, AlbumRequest request);

    void delete(UUID id);
}
