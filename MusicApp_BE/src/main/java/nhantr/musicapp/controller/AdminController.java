package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import nhantr.musicapp.dto.request.BanUserRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.StatsResponse;
import nhantr.musicapp.dto.response.UserAdminResponse;
import nhantr.musicapp.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<APIResponse<PageResponse<UserAdminResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(APIResponse.success(adminService.getUsers(page, size)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<APIResponse<UserAdminResponse>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(APIResponse.success(adminService.getUser(id)));
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<APIResponse<String>> ban(@PathVariable UUID id, @Valid @RequestBody BanUserRequest request) {
        adminService.ban(id, request);
        return ResponseEntity.ok(APIResponse.success("User banned"));
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<APIResponse<String>> unban(@PathVariable UUID id) {
        adminService.unban(id);
        return ResponseEntity.ok(APIResponse.success("User unbanned"));
    }

    @DeleteMapping("/songs/{id}")
    public ResponseEntity<APIResponse<String>> deleteSong(@PathVariable UUID id) {
        adminService.deleteSong(id);
        return ResponseEntity.ok(APIResponse.success("Song deleted"));
    }

    @GetMapping("/stats")
    public ResponseEntity<APIResponse<StatsResponse>> stats() {
        return ResponseEntity.ok(APIResponse.success(adminService.stats()));
    }
}
