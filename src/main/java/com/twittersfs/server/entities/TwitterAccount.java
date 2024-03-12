package com.twittersfs.server.entities;

import com.twittersfs.server.enums.GroupStatus;
import com.twittersfs.server.enums.TwitterAccountStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TwitterAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String restId;
    private String authToken;
    @Column(length = 500)
    private String csrfToken;
    @Column(length = 2000)
    private String cookie;
    private String username;
    private String email;
    private String password;
    private Integer groups;
    private Integer friends;
    private Integer messagesSent;
    private Integer retweets;
    private Integer friendsDifference;
    private Integer messagesDifference;
    private Integer retweetsDifference;
    @Enumerated(EnumType.STRING)
    private GroupStatus groupStatus;
    @CreatedDate
    private LocalDate registrationDate;
    private LocalDateTime payedTo;
    @Enumerated(EnumType.STRING)
    private TwitterAccountStatus status;
    @ManyToOne
    private ModelEntity model;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Proxy proxy;
    @OneToMany(mappedBy = "twitterAccount", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TwitterChatGroup> twitterChatGroups;
    @OneToMany(mappedBy = "twitterAccount", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TwitterChatMessage> messages;
}
