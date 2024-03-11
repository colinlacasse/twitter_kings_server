package com.twittersfs.server.entities;

import com.twittersfs.server.enums.ProxyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Proxy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ip;
    private String port;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private ProxyType type;
}
