package nhantr.musicapp.repository;

import java.util.UUID;
import nhantr.musicapp.entity.FollowArtist;
import nhantr.musicapp.entity.FollowArtistId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowArtistRepository extends JpaRepository<FollowArtist, FollowArtistId> {

    Page<FollowArtist> findByUserId(UUID userId, Pageable pageable);

    boolean existsByUserIdAndArtistId(UUID userId, UUID artistId);

    void deleteByUserIdAndArtistId(UUID userId, UUID artistId);

    long countByArtistId(UUID artistId);
}
