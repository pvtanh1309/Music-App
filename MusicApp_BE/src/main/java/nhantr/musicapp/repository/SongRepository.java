package nhantr.musicapp.repository;

import java.util.UUID;
import nhantr.musicapp.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongRepository extends JpaRepository<Song, UUID> {

    @Query("""
            select s from Song s
            left join s.artist a
            left join s.album al
            where lower(s.title) like lower(concat('%', :query, '%'))
               or lower(coalesce(a.name, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(al.name, '')) like lower(concat('%', :query, '%'))
            """)
    Page<Song> search(@Param("query") String query, Pageable pageable);

    @Query("""
            select s from Song s
            left join ListeningHistory h on h.song.id = s.id
            group by s.id
            order by count(h.id) desc, s.createdAt desc
            """)
    Page<Song> findTrending(Pageable pageable);

    Page<Song> findByArtistId(UUID artistId, Pageable pageable);

    Page<Song> findByAlbumId(UUID albumId, Pageable pageable);
}
