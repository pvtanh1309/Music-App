package nhantr.musicapp.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.request.SongRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.SongResponse;
import nhantr.musicapp.entity.Album;
import nhantr.musicapp.entity.Artist;
import nhantr.musicapp.entity.Song;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.AlbumRepository;
import nhantr.musicapp.repository.ArtistRepository;
import nhantr.musicapp.repository.SongRepository;
import nhantr.musicapp.repository.UploadRepository;
import nhantr.musicapp.repository.FavoriteRepository;
import nhantr.musicapp.repository.ListeningHistoryRepository;
import nhantr.musicapp.repository.PlaylistSongRepository;
import nhantr.musicapp.entity.Upload;
import nhantr.musicapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class SongServiceImpl implements SongService {

    final SongRepository songRepository;
    final ArtistRepository artistRepository;
    final AlbumRepository albumRepository;
    final UploadRepository uploadRepository;
    final FavoriteRepository favoriteRepository;
    final ListeningHistoryRepository listeningHistoryRepository;
    final PlaylistSongRepository playlistSongRepository;
    final MusicMapper musicMapper;
    final S3Client s3Client;
    final CurrentUserService currentUserService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public PageResponse<SongResponse> getSongs(int page, int size, String sort) {
        log.info("Get songs page={}, size={}, sort={}", page, size, sort);
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        Page<SongResponse> responsePage = songRepository.findAll(pageable).map(musicMapper::toSongResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public SongResponse getSong(UUID id) {
        log.info("Get song detail id={}", id);
        return musicMapper.toSongResponse(getSongEntity(id));
    }

    @Override
    public PageResponse<SongResponse> search(String query, String type, int page, int size) {
        log.info("Search songs query={}, type={}, page={}, size={}", query, type, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Song> songPage = songRepository.search(query == null ? "" : query, pageable);
        return PageResponse.fromPage(songPage.map(musicMapper::toSongResponse));
    }

    @Override
    public PageResponse<SongResponse> getTrending(int page, int size) {
        log.info("Get trending songs page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.fromPage(songRepository.findTrending(pageable).map(musicMapper::toSongResponse));
    }

    @Override
    public SongResponse create(SongRequest request) {
        log.info("Create song title={}, artistId={}", request.getTitle(), request.getArtistId());
        
        Artist artist = null;
        if (request.getArtistId() != null) {
            artist = artistRepository
                    .findById(request.getArtistId())
                    .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND.getCode(), ErrorCode.ARTIST_NOT_FOUND.getMessage()));
        }

        Album album = null;
        if (request.getAlbumId() != null) {
            album = albumRepository
                    .findById(request.getAlbumId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "Album not found"));
        }

        Song song = Song.builder()
                .title(request.getTitle())
                .artist(artist)
                .album(album)
                .duration(request.getDuration())
                .fileUrl(request.getFileUrl())
                .coverUrl(request.getCoverUrl())
                .createdAt(LocalDateTime.now())
                .build();

        return musicMapper.toSongResponse(songRepository.save(song));
    }

    @Override
    public SongResponse createWithUpload(SongRequest request, MultipartFile audioFile, MultipartFile coverImage) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR.getCode(), "Audio file is required");
        }

        User user = currentUserService.getCurrentUserEntity();
        log.info("Create upload userId={}, title={}", user.getId(), request.getTitle());

        String fileUrl = uploadToS3(audioFile, "songs/");
        String coverUrl = uploadToS3(coverImage, "songs/cover_");

        SongRequest newReq = SongRequest.builder()
                .title(request.getTitle())
                .artistId(request.getArtistId())
                .albumId(request.getAlbumId())
                .duration(request.getDuration())
                .fileUrl(fileUrl)
                .coverUrl(coverUrl)
                .build();

        SongResponse songResponse = create(newReq);
        Song song = getSongEntity(songResponse.getId());

        // Auto-create Upload record with PENDING status
        Upload upload = Upload.builder()
                .user(user)
                .song(song)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        uploadRepository.save(upload);
        log.info("Auto-created upload for song id={}", song.getId());

        return songResponse;
    }

    private String uploadToS3(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String objectKey = prefix + UUID.randomUUID() + "_" + file.getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(objectKey).build()).toString();
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_PROCESSING_ERROR.getCode(), "Failed to process uploaded file");
        } catch (Exception e) {
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(), "Failed to upload to S3");
        }
    }

    @Override
    public SongResponse update(UUID id, SongRequest request) {
        log.info("Update song id={}", id);
        Song song = getSongEntity(id);
        Artist artist = artistRepository
                .findById(request.getArtistId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTIST_NOT_FOUND.getCode(), ErrorCode.ARTIST_NOT_FOUND.getMessage()));

        Album album = null;
        if (request.getAlbumId() != null) {
            album = albumRepository
                    .findById(request.getAlbumId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "Album not found"));
        }

        song.setTitle(request.getTitle());
        song.setArtist(artist);
        song.setAlbum(album);
        song.setDuration(request.getDuration());
        song.setFileUrl(request.getFileUrl());
        song.setCoverUrl(request.getCoverUrl());

        return musicMapper.toSongResponse(songRepository.save(song));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Delete song id={}", id);

        try {
            favoriteRepository.deleteBySongId(id);
        } catch (Exception ex) {
            log.warn("Failed to delete favorites for song {}: {}", id, ex.getMessage());
        }

        try {
            playlistSongRepository.deleteBySongId(id);
        } catch (Exception ex) {
            log.warn("Failed to delete playlist-song references for song {}: {}", id, ex.getMessage());
        }

        try {
            listeningHistoryRepository.deleteBySongId(id);
        } catch (Exception ex) {
            log.warn("Failed to delete listening history for song {}: {}", id, ex.getMessage());
        }

        try {
            uploadRepository.deleteBySongId(id);
        } catch (Exception ex) {
            log.warn("Failed to delete uploads for song {}: {}", id, ex.getMessage());
        }

        songRepository.delete(getSongEntity(id));
    }

    private Song getSongEntity(UUID id) {
        return songRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND.getCode(), ErrorCode.SONG_NOT_FOUND.getMessage()));
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
