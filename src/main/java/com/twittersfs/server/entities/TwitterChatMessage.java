package com.twittersfs.server.entities;

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
public class TwitterChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 500)
    private String text;
    @Column(length = 2000)
    private String gifUrl;
    @ManyToOne
    private TwitterAccount twitterAccount;
}
