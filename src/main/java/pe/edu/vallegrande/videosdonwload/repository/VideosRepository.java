package pe.edu.vallegrande.videosdonwload.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.videosdonwload.model.videos;

public interface VideosRepository extends ReactiveCrudRepository<videos, Long> {
}
