package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import nhantr.musicapp.dto.request.RejectUploadRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.UploadResponse;
import nhantr.musicapp.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    // POST /api/uploads - DEPRECATED: Upload records are now auto-created when uploading a song
    // Use POST /api/songs (multipart) instead

    @GetMapping("/me")
    public ResponseEntity<APIResponse<PageResponse<UploadResponse>>> myUploads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResponse.success(uploadService.myUploads(page, size)));
    }

    @GetMapping
    public ResponseEntity<APIResponse<PageResponse<UploadResponse>>> getUploads(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResponse.success(uploadService.getUploads(status, page, size)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<APIResponse<String>> approve(@PathVariable UUID id) {
        uploadService.approve(id);
        return ResponseEntity.ok(APIResponse.success("Upload approved"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<APIResponse<String>> reject(@PathVariable UUID id, @Valid @RequestBody RejectUploadRequest request) {
        uploadService.reject(id, request);
        return ResponseEntity.ok(APIResponse.success("Upload rejected"));
    }
}
