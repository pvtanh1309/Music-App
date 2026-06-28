package nhantr.musicapp.repository;

import java.util.UUID;
import nhantr.musicapp.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

    Page<Playlist> findByUserId(UUID userId, Pageable pageable);

    Page<Playlist> findByIsPublicTrue(Pageable pageable);

        @Query("""
                        select p from Playlist p
                        where p.user.id = :userId
                            and lower(coalesce(p.name, '')) like lower(concat('%', :query, '%'))
                        """)
        Page<Playlist> searchByUserId(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);

        @Query("""
                        select p from Playlist p
                        where p.isPublic = true
                            and lower(coalesce(p.name, '')) like lower(concat('%', :query, '%'))
                        """)
        Page<Playlist> searchPublic(@Param("query") String query, Pageable pageable);
}
