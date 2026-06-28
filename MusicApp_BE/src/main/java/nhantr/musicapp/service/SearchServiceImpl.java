package nhantr.musicapp.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.response.AlbumSummaryResponse;
import nhantr.musicapp.dto.response.ArtistSummaryResponse;
import nhantr.musicapp.dto.response.PlaylistResponse;
import nhantr.musicapp.dto.response.SearchResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.AlbumRepository;
import nhantr.musicapp.repository.ArtistRepository;
import nhantr.musicapp.repository.PlaylistRepository;
import nhantr.musicapp.repository.SongRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final PlaylistRepository playlistRepository;
    private final MusicMapper musicMapper;


    @Override
    public SearchResponse search(String query, int page, int size) {
        log.info("Global search query={}, page={}, size={}", query, page, size);
        String q = query == null ? "" : query;

        List<SongResponse> songs = songRepository.search(q, PageRequest.of(page, size)).map(musicMapper::toSongResponse).getContent();
        List<ArtistSummaryResponse> artists = artistRepository.findAll(PageRequest.of(page, size)).stream()
                .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(q.toLowerCase()))
                .map(a -> ArtistSummaryResponse.builder().id(a.getId()).name(a.getName()).build())
                .toList();
        List<AlbumSummaryResponse> albums = albumRepository.findAll(PageRequest.of(page, size)).stream()
                .filter(a -> a.getName() != null && a.getName().toLowerCase().contains(q.toLowerCase()))
                .map(a -> AlbumSummaryResponse.builder().id(a.getId()).name(a.getName()).build())
                .toList();
        List<PlaylistResponse> playlists = playlistRepository.findByIsPublicTrue(PageRequest.of(page, size)).stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q.toLowerCase()))
                .map(p -> PlaylistResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .isPublic(p.isPublic())
                        .songCount(0)
                        .build())
                .toList();

        return SearchResponse.builder()
                .songs(songs)
                .artists(artists)
                .albums(albums)
                .playlists(playlists)
                .build();
    }
}
