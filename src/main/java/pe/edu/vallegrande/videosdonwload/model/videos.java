package pe.edu.vallegrande.videosdonwload.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue; 
import jakarta.persistence.GenerationType;

import lombok.Data;

@Data
@Table("videos")
@Entity
public class videos { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String avatar;    
    private String descarga_video;
    private String descarga_audio;
    private String enlace_video;
    private String nickname;
}
