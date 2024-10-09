package pe.edu.vallegrande.videosdonwload.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Arrays.asList(new Server().url("http://localhost:8080"),
                        new Server().url("https://bxvdkzkm-8080.brs.devtunnels.ms/")))
                .info(new Info().title("API de Ejemplo").version("1.0").description("Documentación de API"));
    }
}
