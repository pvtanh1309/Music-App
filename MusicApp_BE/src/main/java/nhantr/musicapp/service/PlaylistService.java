package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.request.CreatePlaylistRequest;
import nhantr.musicapp.dto.request.PlaylistSongRequest;
import nhantr.musicapp.dto.request.UpdatePlaylistRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.PlaylistResponse;

public interface PlaylistService {

    PageResponse<PlaylistResponse> getMyPlaylists(int page, int size, String query);

    PlaylistResponse getPlaylist(UUID id);

    PlaylistResponse create(CreatePlaylistRequest request);

    PlaylistResponse update(UUID id, UpdatePlaylistRequest request);

    void delete(UUID id);

    void addSong(UUID id, PlaylistSongRequest request);

    void removeSong(UUID id, UUID songId);

    PageResponse<PlaylistResponse> getPublicPlaylists(int page, int size, String query);
}
