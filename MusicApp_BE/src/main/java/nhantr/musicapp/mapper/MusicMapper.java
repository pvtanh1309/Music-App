package nhantr.musicapp.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import nhantr.musicapp.dto.response.AlbumSummaryResponse;
import nhantr.musicapp.dto.response.ArtistSummaryResponse;
import nhantr.musicapp.dto.response.PlaylistResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.entity.Playlist;
import nhantr.musicapp.entity.Song;
import nhantr.musicapp.repository.ListeningHistoryRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MusicMapper {

    private final ListeningHistoryRepository listeningHistoryRepository;

    public SongResponse toSongResponse(Song song) {
        if (song == null) {
            return null;
        }

        return SongResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .duration(song.getDuration())
                .coverUrl(song.getCoverUrl())
                .fileUrl(song.getFileUrl())
                .artist(song.getArtist() == null
                        ? null
                        : ArtistSummaryResponse.builder()
                                .id(song.getArtist().getId())
                                .name(song.getArtist().getName())
                                .build())
                .album(song.getAlbum() == null
                        ? null
                        : AlbumSummaryResponse.builder()
                                .id(song.getAlbum().getId())
                                .name(song.getAlbum().getName())
                                .build())
                .listenerCount(song.getId() == null ? 0 : listeningHistoryRepository.countDistinctUserIdBySongId(song.getId()))
                .createdAt(song.getCreatedAt())
                .build();
    }

    public List<SongResponse> toSongResponses(List<Song> songs) {
        return songs.stream().map(this::toSongResponse).collect(Collectors.toList());
    }

    public PlaylistResponse toPlaylistResponse(Playlist playlist, List<SongResponse> songs, int songCount) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .isPublic(playlist.isPublic())
                .songCount(songCount)
                .songs(songs)
                .createdAt(playlist.getCreatedAt() == null ? LocalDateTime.now() : playlist.getCreatedAt())
                .build();
    }
}
