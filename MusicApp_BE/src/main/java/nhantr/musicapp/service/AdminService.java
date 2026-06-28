package nhantr.musicapp.service;

import java.util.UUID;
import nhantr.musicapp.dto.request.BanUserRequest;
import nhantr.musicapp.dto.response.PageResponse;
import nhantr.musicapp.dto.response.StatsResponse;
import nhantr.musicapp.dto.response.UserAdminResponse;

public interface AdminService {

    PageResponse<UserAdminResponse> getUsers(int page, int size);

    UserAdminResponse getUser(UUID id);

    void ban(UUID id, BanUserRequest request);

    void unban(UUID id);

    void deleteSong(UUID id);

    StatsResponse stats();
}
