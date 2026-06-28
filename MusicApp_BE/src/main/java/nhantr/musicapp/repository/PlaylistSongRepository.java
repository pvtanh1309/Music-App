package nhantr.musicapp.repository;

import java.util.List;
import java.util.UUID;
import nhantr.musicapp.entity.PlaylistSong;
import nhantr.musicapp.entity.PlaylistSongId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, PlaylistSongId> {

    List<PlaylistSong> findByPlaylistId(UUID playlistId);

    long countByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSongId(UUID playlistId, UUID songId);

    void deleteByPlaylistIdAndSongId(UUID playlistId, UUID songId);

    @Modifying(clearAutomatically = true)
    @Query("delete from PlaylistSong ps where ps.song.id = :songId")
    void deleteBySongId(@Param("songId") UUID songId);
}
