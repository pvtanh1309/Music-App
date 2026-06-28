package nhantr.musicapp.service;


import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.dto.request.RejectUploadRequest;
import nhantr.musicapp.dto.request.UploadRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.UploadResponse;
import nhantr.musicapp.entity.Upload;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.entity.Song;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.MusicMapper;
import nhantr.musicapp.repository.UploadRepository;
import nhantr.musicapp.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements UploadService {

    private final UploadRepository uploadRepository;
    private final SongRepository songRepository;
    private final CurrentUserService currentUserService;
    private final MusicMapper musicMapper;
    
    @Override
    public UploadResponse create(UploadRequest request) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Create upload userId={}, songId={}", user.getId(), request.getSongId());

        Song song = null;
        if (request.getSongId() != null) {
            song = songRepository.findById(request.getSongId())
                    .orElseThrow(() -> new AppException(ErrorCode.SONG_NOT_FOUND.getCode(), ErrorCode.SONG_NOT_FOUND.getMessage()));
        }

        Upload upload = Upload.builder()
                .user(user)
                .song(song)
                .status("PENDING")
                .createdAt(java.time.LocalDateTime.now())
                .build();

        upload = uploadRepository.save(upload);
        return UploadResponse.builder()
                .id(upload.getId())
                .status(upload.getStatus())
                .createdAt(upload.getCreatedAt())
                .song(upload.getSong() == null ? null : musicMapper.toSongResponse(upload.getSong()))
                .build();
    }

    @Override
    public PageResponse<UploadResponse> myUploads(int page, int size) {
        User user = currentUserService.getCurrentUserEntity();
        log.info("Get my uploads userId={}, page={}, size={}", user.getId(), page, size);

        Page<UploadResponse> responsePage = uploadRepository
                .findByUserId(user.getId(), PageRequest.of(page, size))
                .map(this::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public PageResponse<UploadResponse> getUploads(String status, int page, int size) {
        log.info("Get uploads status={}, page={}, size={}", status, page, size);
        Page<Upload> uploadPage = (status == null || status.isBlank())
                ? uploadRepository.findAll(PageRequest.of(page, size))
                : uploadRepository.findByStatus(status.toUpperCase(), PageRequest.of(page, size));
        return PageResponse.fromPage(uploadPage.map(this::toResponse));
    }

    @Override
    public void approve(UUID id) {
        log.info("Approve upload id={}", id);
        Upload upload = getUpload(id);
        upload.setStatus("APPROVED");
        uploadRepository.save(upload);
    }

    @Override
    public void reject(UUID id, RejectUploadRequest request) {
        log.info("Reject upload id={}, reason={}", id, request.getReason());
        Upload upload = getUpload(id);
        upload.setStatus("REJECTED");
        uploadRepository.save(upload);
    }

    private Upload getUpload(UUID id) {
        return uploadRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND.getCode(), "Upload not found"));
    }

    private UploadResponse toResponse(Upload upload) {
        return UploadResponse.builder()
                .id(upload.getId())
                .status(upload.getStatus())
                .createdAt(upload.getCreatedAt())
                .song(upload.getSong() == null ? null : musicMapper.toSongResponse(upload.getSong()))
                .build();
    }
}
