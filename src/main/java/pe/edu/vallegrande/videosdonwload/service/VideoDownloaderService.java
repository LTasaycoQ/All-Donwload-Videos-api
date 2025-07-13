package pe.edu.vallegrande.videosdonwload.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public VideoDownloaderService(
            WebClient.Builder webClientBuilder,
            VideosRepository videosRepository,
            @Value("${rapidapi.key}") String apiKey,
            @Value("${rapidapi.host}") String apiHost,
            @Value("${rapidapi.base-url}") String baseUrl
    ) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("x-rapidapi-host", apiHost)
                .build();
        this.videosRepository = videosRepository;
    }

    String videoUrl = "", title = "", avatar = "", nickname = "", firstLink = "", audio = "",
            portada = "", dominioCase1 = "", dominioCase2 = "", like = "", comentario = "", vistas = "",
            compartidos = "";

    public Mono<Map<String, String>> getVideoData(String url) {
        logger.info("Fetching video data for URL: {}", url);
        return webClient.get()
                .uri("/smvd/get/all?url={url}", url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        String[] parts = url.split("/");
                        String fullDomain = parts[2];
                        String[] domainParts = fullDomain.split("\\.");
                        String mainDomain = domainParts.length >= 2 ? domainParts[domainParts.length - 2] : fullDomain;

                        String data[] = { "video_hd_original", "audio", "video_render_480p (video+audio)",
                                "audio_quality_medium (pt) (only_audio)", "video_hd_0", "audio_0",
                                "video_hd_original_0", "audio_quality_low (es) (only_audio)" };

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(response);
                        JsonNode links = jsonNode.path("links");
                        JsonNode dataStats = jsonNode.path("stats");

                        videoUrl = jsonNode.path("src_url").asText();
                        title = jsonNode.path("title").asText();
                        portada = jsonNode.path("picture").asText();
                        avatar = jsonNode.path("author").path("avatar").asText();
                        nickname = jsonNode.path("author").path("nickname").asText();
                        switch (mainDomain) {

                            case "tiktok":
                                dominioCase1 = data[0];
                                dominioCase2 = data[1];
                                like = dataStats.path("diggCount").asText();
                                compartidos = dataStats.path("shareCount").asText();
                                comentario = dataStats.path("commentCount").asText();
                                vistas = dataStats.path("playCount").asText();

                                break;
                            case "youtube":
                                avatar = jsonNode.path("author").path("thumbnails").get(0).path("url").asText();

                                nickname = jsonNode.path("author").path("name").asText();

                                dominioCase1 = data[2];
                                dominioCase2 = data[7];

                                like = dataStats.path("likes").asText();
                                comentario = dataStats.path("comments").path("header").path("count").path("runs").get(0).path("text").asText();
                                vistas = dataStats.path("viewCount").asText();

                                break;
                            case "youtu":
                                avatar = jsonNode.path("author").path("thumbnails").get(0).path("url").asText();

                                nickname = jsonNode.path("author").path("name").asText();

                                dominioCase1 = data[2];
                                dominioCase2 = data[3];

                                like = dataStats.path("likes").asText();
                                comentario = dataStats.path("comments").path("header").path("count").path("runs").get(0).path("text").asText();
                                vistas = dataStats.path("viewCount").asText();

                                break;
                            case "facebook":
                                nickname = "";
                                avatar = "";
                                dominioCase1 = data[4];
                                dominioCase2 = data[5];

                                break;
                            case "instagram":
                                avatar = jsonNode.path("author").path("profile_pic_url").asText();

                                nickname = jsonNode.path("author").path("username").asText();

                                dominioCase1 = data[6];
                                dominioCase2 = data[5];
                                like = dataStats.path("like_count").asText();
                                compartidos = dataStats.path("play_count").asText();
                                comentario = dataStats.path("comment_count").asText();
                                vistas = dataStats.path("view_count").asText();

                                break;

                            default:
                                break;
                        }

                        for (int i = 0; i < links.size(); i++) {

                            firstLink = links.get(i).path("quality").asText();

                            if (firstLink.equals(dominioCase1)) {

                                for (int a = 0; a < links.size(); a++) {
                                    audio = links.get(a).path("quality").asText();

                                    if (audio.equals(dominioCase2)) {

                                        audio = links.get(a).path("link").asText();
                                        break;
                                    }
                                }
                                firstLink = links.get(i).path("link").asText();
                                break;
                            }
                        }

                        Map<String, String> mappedResponse = new HashMap<>();
                        mappedResponse.put("Nickname", nickname);
                        mappedResponse.put("Avatar", avatar);
                        mappedResponse.put("Like", like);
                        mappedResponse.put("Comentarios", comentario);
                        mappedResponse.put("Compartidos", compartidos);
                        mappedResponse.put("Vistas", vistas);
                        mappedResponse.put("Portada", portada);
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
                        logger.error("Error al procesar video data", e);
                        return Mono.error(e);
                    }
                });
    }

}