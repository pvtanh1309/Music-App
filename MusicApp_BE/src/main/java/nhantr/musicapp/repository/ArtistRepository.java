package nhantr.musicapp.repository;

import java.util.UUID;
import nhantr.musicapp.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {

	@Query("""
			select a from Artist a
			where lower(coalesce(a.name, '')) like lower(concat('%', :query, '%'))
			   or lower(coalesce(a.bio, '')) like lower(concat('%', :query, '%'))
			""")
	Page<Artist> search(@Param("query") String query, Pageable pageable);
}
