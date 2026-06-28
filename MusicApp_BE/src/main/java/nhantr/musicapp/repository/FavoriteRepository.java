package nhantr.musicapp.repository;

import java.util.List;
import java.util.UUID;
import nhantr.musicapp.entity.Favorite;
import nhantr.musicapp.entity.FavoriteId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    Page<Favorite> findByUserId(UUID userId, Pageable pageable);

        @Query("""
                        select f from Favorite f
                        left join f.song s
                        left join s.artist a
                        left join s.album al
                        where f.user.id = :userId
                            and (
                                lower(coalesce(s.title, '')) like lower(concat('%', :query, '%'))
                                or lower(coalesce(a.name, '')) like lower(concat('%', :query, '%'))
                                or lower(coalesce(al.name, '')) like lower(concat('%', :query, '%'))
                            )
                        """)
        Page<Favorite> searchByUserId(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);

    boolean existsByUserIdAndSongId(UUID userId, UUID songId);

    void deleteByUserIdAndSongId(UUID userId, UUID songId);

    long countBySongId(UUID songId);

    List<Favorite> findBySongId(UUID songId);

    @Modifying(clearAutomatically = true)
    @Query("delete from Favorite f where f.song.id = :songId")
    void deleteBySongId(@Param("songId") UUID songId);
}
