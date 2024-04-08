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
public class TelegramUserEntity {
    @Id
    private Long chatId;
    private String language;
    private Integer refillAmount;
    private String email;
    private String tempId;
    private String code;
    private Boolean verified;
    private String verificationEmail;
}
