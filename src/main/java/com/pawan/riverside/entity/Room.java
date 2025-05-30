package com.pawan.riverside.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name="rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true,nullable = false)
    private String roomName;
    private String roomCode;
    private String host;
    private LocalDateTime createdAt;
    private boolean isActive;
    private String recordingStatus;
    private boolean isMicOnByDefault;
    private boolean isCameraOnByDefault;
}
