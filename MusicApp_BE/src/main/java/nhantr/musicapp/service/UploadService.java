package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.request.RejectUploadRequest;
import nhantr.musicapp.dto.request.UploadRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.UploadResponse;
public interface UploadService {

    UploadResponse create(UploadRequest request);

    PageResponse<UploadResponse> myUploads(int page, int size);

    PageResponse<UploadResponse> getUploads(String status, int page, int size);

    void approve(UUID id);

    void reject(UUID id, RejectUploadRequest request);
}
