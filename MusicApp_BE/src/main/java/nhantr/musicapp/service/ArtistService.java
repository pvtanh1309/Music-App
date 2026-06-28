package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.request.ArtistRequest;
import nhantr.musicapp.dto.response.ArtistResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.SongResponse;

public interface ArtistService {

    PageResponse<ArtistResponse> getArtists(int page, int size, String query);

    ArtistResponse getArtist(UUID id);

    PageResponse<SongResponse> getArtistSongs(UUID id, int page, int size);

    ArtistResponse create(ArtistRequest request);

    ArtistResponse update(UUID id, ArtistRequest request);

    void delete(UUID id);

    long getFollowers(UUID id);
}
