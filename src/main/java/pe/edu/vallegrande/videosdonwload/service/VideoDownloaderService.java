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
import com.fasterxml.jackson.databind.node.ObjectNode;

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
            @Value("${rapidapi.base-url}") String baseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("x-rapidapi-host", apiHost)
                .build();
        this.videosRepository = videosRepository;
    }

    public Mono<Object> getVideoData(String url) {
        logger.info("Fetching video data for URL: {}", url);
        return webClient.get()
                .uri("/smvd/get/all?url={url}", url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode jsonNode = objectMapper.readTree(response);

                        return Mono.just(jsonNode);
                    } catch (Exception e) {
                        logger.error("Error al procesar video data", e);
                        return Mono.error(e);
                    }
                });
    }

    public Mono<Object> getVideoProcess(String url) {
        return getVideoData(url)
                .flatMap(jsonNode -> {
                    try {

                        Boolean responseStatus;
                        JsonNode responseApi = (JsonNode) jsonNode;
                        JsonNode dataVideoResponse = getStateVideo(url, responseApi);

                        Map<String, Object> resultado = new HashMap<>();
                        responseStatus = dataVideoResponse.path("responseStatus").asBoolean();
                        if (responseStatus) {
                            resultado.put("responseStatus", dataVideoResponse.path("responseStatus").asBoolean());

                            resultado.put("nickname", dataVideoResponse.path("nickname").asText());
                            resultado.put("avatar", dataVideoResponse.path("avatar").asText());
                            resultado.put("frontPage", dataVideoResponse.path("frontPage").asText());
                            resultado.put("videoUrl", url);
                            resultado.put("title", dataVideoResponse.path("title").asText());
                            resultado.put("linkVideo", dataVideoResponse.path("linkVideo").asText());
                            resultado.put("linkAudio", dataVideoResponse.path("linkAudio").asText());
                            resultado.put("like", dataVideoResponse.path("like").asText());
                            resultado.put("views", dataVideoResponse.path("views").asText());
                            resultado.put("comments", dataVideoResponse.path("comments").asText());
                            resultado.put("shared", dataVideoResponse.path("shared").asText());

                            videos video = mapToVideoEntity(url, dataVideoResponse);
                            videosRepository.save(video).subscribe();
                        } else {
                            resultado.put("responseStatus", dataVideoResponse.path("responseStatus").asBoolean());
                            resultado.put("message", "video not found");

                        }

                        return Mono.just(resultado);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

    private videos mapToVideoEntity(String url, JsonNode dataVideoResponse) {
        videos video = new videos();
        video.setTitulo(dataVideoResponse.path("title").asText());
        video.setAvatar(dataVideoResponse.path("picture").asText());
        video.setDescarga_video(dataVideoResponse.path("linkVideo").asText());
        video.setDescarga_audio(dataVideoResponse.path("linkAudio").asText());
        video.setEnlace_video(url);
        video.setNickname(dataVideoResponse.path("nickname").asText());
        return video;
    }

    private JsonNode getStateVideo(String url, JsonNode responseApi) {

        JsonNode links = responseApi.path("links");
        JsonNode dataStats = responseApi.path("stats");
        Boolean responseStatus = true;
        String like = null;
        String shared = null;
        String comments = null;
        String views = null;
        String video = null;
        String audio = null;
        String avatar = null;
        String title = null;
        String frontPage = null;
        String nickname = null;

        String data[] = { "video_hd_original", "audio", "video_hd_0", "audio_0", "(video+audio)", "(only_audio)",
                "video_hd_original_0", };

        title = responseApi.path("title").asText();
        frontPage = responseApi.path("picture").asText();

        String getDomainNetwork = methodRecognizeNetwork(url);

        switch (getDomainNetwork) {
            case "tiktok":

                video = data[0];
                audio = data[1];

                avatar = responseApi.path("author").path("avatar").asText();
                nickname = responseApi.path("author").path("nickname").asText();

                like = dataStats.path("diggCount").asText();
                shared = dataStats.path("shareCount").asText();
                comments = dataStats.path("commentCount").asText();
                views = dataStats.path("playCount").asText();
                break;

            case "instagram":
                like = dataStats.path("like_count").asText();
                shared = dataStats.path("play_count").asText();
                comments = dataStats.path("comment_count").asText();
                views = dataStats.path("view_count").asText();

                avatar = responseApi.path("author").path("profile_pic_url").asText();
                nickname = responseApi.path("author").path("username").asText();

                video = data[6];
                audio = data[3];
                break;
            case "youtu":
            case "youtube":
                like = dataStats.path("likes").asText();
                comments = "privado";
                avatar = responseApi.path("author").path("thumbnails").get(0).path("url").asText();
                nickname = responseApi.path("author").path("name").asText();

                views = dataStats.path("viewCount").asText();
                video = data[4];
                audio = data[5];
                break;

            case "facebook":
                video = data[2];
                audio = data[3];
                break;
            default:
                responseStatus = false;

                break;
        }

        String getLinkVideo = searchVideo(video, links);
        String getLinkAudio = searchAudio(audio, links);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("responseStatus", responseStatus);
        json.put("shared", shared);
        json.put("like", like);
        json.put("views", views);
        json.put("comments", comments);
        json.put("avatar", avatar);
        json.put("nickname", nickname);
        json.put("linkVideo", getLinkVideo);
        json.put("linkAudio", getLinkAudio);
        json.put("title", title);
        json.put("frontPage", frontPage);
        return json;

    }

    private String methodRecognizeNetwork(String url) {
        try {
            String[] parts = url.split("/");
            String fullDomain = parts[2];
            String[] domainParts = fullDomain.split("\\.");
            return domainParts.length >= 2 ? domainParts[domainParts.length - 2] : fullDomain;
        } catch (Exception e) {
            logger.warn("No se pudo reconocer el dominio de: " + url);
            return "unknown";
        }

    }

    private String searchVideo(String video, JsonNode links) {

        String quality;
        String linkVideo = null;
        try {
            for (int index = 0; index < links.size(); index++) {
                quality = links.get(index).path("quality").asText();
                if (quality.contains(video)) {
                    linkVideo = links.get(index).path("link").asText();
                    break;
                }
            }
            return linkVideo;
        } catch (Exception e) {
            logger.error("No se encontro el video");
            return "Unknow";
        }

    }

    private String searchAudio(String audio, JsonNode links) {
        String quality;
        String linkAudio = null;
        try {
            for (int index = 0; index < links.size(); index++) {
                quality = links.get(index).path("quality").asText();
                if (quality.contains(audio)) {
                    linkAudio = links.get(index).path("link").asText();
                    break;
                }
            }
            return linkAudio;
        } catch (Exception e) {
            logger.error("No se encontro el audio");
            return "Unknow";
        }
    }

}