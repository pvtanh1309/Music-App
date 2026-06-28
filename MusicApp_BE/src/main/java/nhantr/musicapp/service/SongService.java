package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.request.SongRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.SongResponse;

public interface SongService {

    PageResponse<SongResponse> getSongs(int page, int size, String sort);

    SongResponse getSong(UUID id);

    PageResponse<SongResponse> search(String query, String type, int page, int size);

    PageResponse<SongResponse> getTrending(int page, int size);

    SongResponse create(SongRequest request);
    
    SongResponse createWithUpload(SongRequest request, org.springframework.web.multipart.MultipartFile audioFile, org.springframework.web.multipart.MultipartFile coverImage);

    SongResponse update(UUID id, SongRequest request);

    void delete(UUID id);
}