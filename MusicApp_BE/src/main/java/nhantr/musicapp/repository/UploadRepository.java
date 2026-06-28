package nhantr.musicapp.repository;

import java.util.UUID;
import nhantr.musicapp.entity.Upload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UploadRepository extends JpaRepository<Upload, UUID> {

    Page<Upload> findByUserId(UUID userId, Pageable pageable);

    Page<Upload> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);

    @Modifying(clearAutomatically = true)
    @Query("delete from Upload u where u.song.id = :songId")
    void deleteBySongId(@Param("songId") UUID songId);
}
