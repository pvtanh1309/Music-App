package nhantr.musicapp.repository;

import java.util.UUID;
import nhantr.musicapp.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, UUID> {

	Page<Album> findByArtistId(UUID artistId, Pageable pageable);

	@Query("""
			select distinct al from Album al
			left join al.artist ar
			where lower(coalesce(al.name, '')) like lower(concat('%', :query, '%'))
			   or lower(coalesce(ar.name, '')) like lower(concat('%', :query, '%'))
			""")
	Page<Album> search(@Param("query") String query, Pageable pageable);

	@Query("""
			select distinct al from Album al
			left join al.artist ar
			where ar.id = :artistId
			  and (lower(coalesce(al.name, '')) like lower(concat('%', :query, '%'))
			       or lower(coalesce(ar.name, '')) like lower(concat('%', :query, '%')))
			""")
	Page<Album> searchByArtistId(@Param("artistId") UUID artistId, @Param("query") String query, Pageable pageable);
}
