package pe.edu.vallegrande.videosdonwload.rest;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.vallegrande.videosdonwload.service.VideoDownloaderService;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "https://downime.netlify.app/")
@RequestMapping("/api")
public class VideoDownloaderRest {
    private final VideoDownloaderService videoDownloaderService;

    public VideoDownloaderRest(VideoDownloaderService videoDownloaderService) {
        this.videoDownloaderService = videoDownloaderService;
    }

    @GetMapping("/download")
    public Mono<Map<String, String>> getVideoLinks(@RequestParam String url) {
        return videoDownloaderService.getVideoData(url);
    }
}
