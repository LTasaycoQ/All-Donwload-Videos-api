package pe.edu.vallegrande.videosdonwload.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import pe.edu.vallegrande.videosdonwload.model.videos;
import pe.edu.vallegrande.videosdonwload.repository.VideosRepository;
import reactor.core.publisher.Mono;

@Service
public class VideoDownloaderService {
    private final WebClient webClient;
    private final VideosRepository videosRepository;
    private static final Logger logger = LoggerFactory.getLogger(VideoDownloaderService.class);

    @Autowired
    public VideoDownloaderService(WebClient.Builder webClientBuilder, VideosRepository videosRepository) {
        this.webClient = webClientBuilder
                .baseUrl("https://social-media-video-downloader.p.rapidapi.com")
                .defaultHeader("x-rapidapi-key", "67cbc6ba2amsh44a419b487c7443p184a72jsndc1f0cc49523")
                .defaultHeader("x-rapidapi-host", "social-media-video-downloader.p.rapidapi.com")
                .build();
        this.videosRepository = videosRepository;
    }

    public Mono<Map<String, String>> getVideoData(String url) {
        logger.info("Fetching video data for URL: {}", url);
        return webClient.get()
                .uri("/smvd/get/all?url={url}", url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        String[] parts = url.split("/");
                        String domain = parts[2];

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(response);

                        String videoUrl = "", title = "", avatar = "", nickname = "", firstLink = "", audio = "";
                        switch (domain) {
                            case "www.tiktok.com":
                                videoUrl = jsonNode.path("src_url").asText();
                                title = jsonNode.path("title").asText();

                                avatar = jsonNode.path("author").path("avatar").asText();
                                nickname = jsonNode.path("author").path("nickname").asText();

                                firstLink = jsonNode.path("links").get(0).path("link").asText();
                                audio = jsonNode.path("links").get(2).path("link").asText();
                                break;
                            case "vm.tiktok.com":
                                videoUrl = jsonNode.path("src_url").asText();
                                title = jsonNode.path("title").asText();

                                avatar = jsonNode.path("author").path("avatar").asText();
                                nickname = jsonNode.path("author").path("nickname").asText();

                                firstLink = jsonNode.path("links").get(0).path("link").asText();
                                audio = jsonNode.path("links").get(1).path("link").asText();
                                break;
                            case "youtu.be":
                                videoUrl = jsonNode.path("src_url").asText();
                                title = jsonNode.path("title").asText();

                                avatar = jsonNode.path("author").path("thumbnails").get(2).path("url").asText();
                                nickname = jsonNode.path("author").path("name").asText();
                                firstLink = jsonNode.path("links").get(5).path("link").asText();
                                audio = jsonNode.path("links").get(0).path("link").asText();
                                break;
                            case "www.youtube.com":
                                videoUrl = jsonNode.path("src_url").asText();
                                title = jsonNode.path("title").asText();

                                avatar = jsonNode.path("author").path("thumbnails").get(2).path("url").asText();
                                nickname = jsonNode.path("author").path("name").asText();
                                firstLink = jsonNode.path("links").get(7).path("link").asText();
                                audio = jsonNode.path("links").get(0).path("link").asText();
                                break;
                            case "fb.watch":
                                videoUrl = jsonNode.path("src_url").asText();
                                title = jsonNode.path("title").asText();

                                avatar = jsonNode.path("author").path("thumbnails").get(2).path("url").asText();
                                nickname = jsonNode.path("author").path("name").asText();
                                firstLink = jsonNode.path("links").get(0).path("link").asText();
                                audio = jsonNode.path("links").get(6).path("link").asText();
                                break;
                            case "www.facebook.com":
                                videoUrl = jsonNode.path("src_url").asText();
                                title = jsonNode.path("title").asText();

                                avatar = "No tiene🥺";
                                nickname = "No tiene🥺";

                                firstLink = jsonNode.path("links").get(1).path("link").asText();
                                audio = jsonNode.path("links").get(4).path("link").asText();
                                break;
                            case "www.instagram.com":

                                videoUrl = jsonNode.path("src_url").asText();
                                title = jsonNode.path("title").asText();

                                // avatar =
                                // jsonNode.path("author").path("thumbnails").get(2).path("url").asText();
                                // nickname = jsonNode.path("author").path("name").asText();
                                avatar = "No tiene🥺";
                                nickname = "No tiene🥺";
                                firstLink = jsonNode.path("links").get(1).path("link").asText();
                                audio = jsonNode.path("links").get(0).path("link").asText();

                                Integer verficacion = firstLink.length();

                                if (verficacion < 500) {
                                    videoUrl = jsonNode.path("src_url").asText();
                                    title = jsonNode.path("title").asText();

                                    // avatar =
                                    // jsonNode.path("author").path("thumbnails").get(2).path("url").asText();
                                    // nickname = jsonNode.path("author").path("name").asText();
                                    avatar = "No tiene🥺";
                                    nickname = "No tiene🥺";
                                    firstLink = jsonNode.path("links").get(0).path("link").asText();
                                    audio = jsonNode.path("links").get(1).path("link").asText();
                                }

                                break;
                            default:
                        }

                        Map<String, String> mappedResponse = new HashMap<>();
                        mappedResponse.put("Nickname", nickname);
                        mappedResponse.put("Avatar", avatar);
                        mappedResponse.put("enlace_video", videoUrl);
                        mappedResponse.put("titulo", title);
                        mappedResponse.put("descarga_video", firstLink);
                        mappedResponse.put("descarga_audio", audio);

                        videos video = new videos();
                        video.setTitulo(title);
                        video.setAvatar(avatar);
                        video.setDescarga_video(firstLink);
                        video.setDescarga_audio(audio);
                        video.setEnlace_video(videoUrl);
                        video.setNickname(nickname);

                        return videosRepository.save(video)
                                .then(Mono.just(mappedResponse));
                    } catch (Exception e) {
                        logger.error("Error processing video data", e);
                        return Mono.error(e);
                    }
                });
    }
}

