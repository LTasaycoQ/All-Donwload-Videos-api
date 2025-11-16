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
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class VideoDownloaderRest {
    private final VideoDownloaderService videoDownloaderService;

    public VideoDownloaderRest(VideoDownloaderService videoDownloaderService) {
        this.videoDownloaderService = videoDownloaderService;
    }

    @GetMapping("/download")
    public Mono<Object> getVideoLinks(@RequestParam String url) {
        return videoDownloaderService.getVideoProcess(url);
    }

    @GetMapping("/download/tiktok")
    public Mono<Object> getVideoTikTok(@RequestParam String url) {
        return videoDownloaderService.getVideoProcess(url);
    }

    @GetMapping("/download/youtube")
    public Mono<Object> getVideoYoutube(@RequestParam String url) {
        return videoDownloaderService.getVideoProcess(url);
    }

    @GetMapping("/download/instagram")
    public Mono<Object> getVideoInstagram(@RequestParam String url) {
        return videoDownloaderService.getVideoProcess(url);
    }



    @GetMapping("/download/facebook")
    public Mono<Object> getVideoFacebook(@RequestParam String url) {
        return videoDownloaderService.getVideoProcess(url);
    }
}
