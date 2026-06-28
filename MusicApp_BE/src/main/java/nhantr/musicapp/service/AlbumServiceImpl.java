package nhantr.musicapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.request.AlbumRequest;
import nhantr.musicapp.dto.response.AlbumResponse;
import nhantr.musicapp.dto.response.ArtistSummaryResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.entity.Album;
import nhantr.musicapp.entity.Artist;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.AlbumRepository;
import nhantr.musicapp.repository.ArtistRepository;
import nhantr.musicapp.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final MusicMapper musicMapper;

    @Override
    public PageResponse<AlbumResponse> getAlbums(int page, int size, String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        log.info("Get albums page={}, size={}, query={}", page, size, normalizedQuery);
        Page<AlbumResponse> responsePage = (normalizedQuery.isEmpty()
                ? albumRepository.findAll(PageRequest.of(page, size))
                : albumRepository.search(normalizedQuery, PageRequest.of(page, size)))
                .map(this::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public PageResponse<AlbumResponse> getAlbumsByArtistId(UUID artistId, int page, int size, String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        log.info("Get albums by artistId={}, page={}, size={}, query={}", artistId, page, size, normalizedQuery);
        Page<AlbumResponse> responsePage = (normalizedQuery.isEmpty()
                ? albumRepository.findByArtistId(artistId, PageRequest.of(page, size))
                : albumRepository.searchByArtistId(artistId, normalizedQuery, PageRequest.of(page, size)))
                .map(this::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public AlbumResponse getAlbum(UUID id) {
        log.info("Get album detail id={}", id);
        return toResponse(getAlbumEntity(id));
    }

    @Override
    public AlbumResponse create(AlbumRequest request) {
        log.info("Create album name={}", request.getName());
        Artist artist = artistRepository
                .findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND.getCode(), ErrorCode.ARTIST_NOT_FOUND.getMessage()));

        Album album = Album.builder()
                .name(request.getName())
                .artist(artist)
                .releaseDate(request.getReleaseDate())
                .coverUrl(request.getCoverUrl())
                .build();
        return toResponse(albumRepository.save(album));
    }

    @Override
    public AlbumResponse update(UUID id, AlbumRequest request) {
        log.info("Update album id={}", id);
        Album album = getAlbumEntity(id);
        Artist artist = artistRepository
                .findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND.getCode(), ErrorCode.ARTIST_NOT_FOUND.getMessage()));

        album.setName(request.getName());
        album.setArtist(artist);
        album.setReleaseDate(request.getReleaseDate());
        album.setCoverUrl(request.getCoverUrl());
        return toResponse(albumRepository.save(album));
    }

    @Override
    public void delete(UUID id) {
        log.info("Delete album id={}", id);
        albumRepository.delete(getAlbumEntity(id));
    }

    private Album getAlbumEntity(UUID id) {
        return albumRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "Album not found"));
    }

    private AlbumResponse toResponse(Album album) {
        List<SongResponse> songs = new ArrayList<>(songRepository
                .findByAlbumId(album.getId(), PageRequest.of(0, 100))
                .map(musicMapper::toSongResponse)
                .getContent());
        int totalDuration = songs.stream().mapToInt(s -> s.getDuration()).sum();

        return AlbumResponse.builder()
                .id(album.getId())
                .name(album.getName())
                .releaseDate(album.getReleaseDate())
                .coverUrl(album.getCoverUrl())
                .artist(album.getArtist() == null
                        ? null
                        : ArtistSummaryResponse.builder()
                                .id(album.getArtist().getId())
                                .name(album.getArtist().getName())
                                .build())
                .songs(songs)
                .totalSongs(songs.size())
                .totalDuration(totalDuration)
                .build();
    }
}
