package com.twittersfs.server.services;

import com.twittersfs.server.entities.ModelEntity;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.entities.UserEntity;
import com.twittersfs.server.enums.Role;
import com.twittersfs.server.enums.SubscriptionType;
import com.twittersfs.server.dtos.model.ModelDto;
import com.twittersfs.server.dtos.user.UserData;
import com.twittersfs.server.dtos.user.UserRegister;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.repos.UserEntityRepo;
import com.twittersfs.server.security.AuthDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserEntityRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserEntityRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserData getUser(String email) {
        UserEntity user = Optional.ofNullable(userRepo.findByEmail(email))
                .orElseThrow(() -> new RuntimeException("User with such email does not exist"));
        return fromUserEntity(user);
    }

    @Override
    public void register(UserRegister dto) {
        userRepo.save(fromUserRegisterDto(dto));
    }

    @Override
    public AuthDto getUserAuthDto(String email) {
        UserEntity user = Optional.ofNullable(userRepo.findByEmail(email))
                .orElseThrow(() -> new RuntimeException("User with such email does not exist"));
        return toUserAuthDto(user);
    }

    @Override
    public void resetPassword(String email, String password) {
        UserEntity user = Optional.ofNullable(userRepo.findByEmail(email))
                .orElseThrow(() -> new RuntimeException("User with such email does not exist"));
        user.setPassword(passwordEncoder.encode(password));
        userRepo.save(user);
    }

    private UserData fromUserEntity(UserEntity entity) {
        int disabled = 0;
        int active = 0;
        int cooldown = 0;
        int locked = 0;
        int invalid = 0;
        int suspended = 0;
        int error = 0;
        int stopping = 0;
        int updated = 0;
        int all = 0;
        int proxyErr = 0;
        List<ModelEntity> models = entity.getModelEntities();
        List<TwitterAccount> twitterAccounts = models.stream()
                .flatMap(modelEntity -> modelEntity.getTwitterAccounts().stream()).toList();

        for (TwitterAccount twitterAccount : twitterAccounts) {
            if (twitterAccount.getStatus().equals(TwitterAccountStatus.DISABLED)) {
                disabled++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.ACTIVE)) {
                active++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.COOLDOWN)) {
                cooldown++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.LOCKED)) {
                locked++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.INVALID_COOKIES)) {
                invalid++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.SUSPENDED)) {
                suspended++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.UNEXPECTED_ERROR)) {
                error++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.STOPPING)) {
                stopping++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.UPDATED_COOKIES)) {
                updated++;
            } else if (twitterAccount.getStatus().equals(TwitterAccountStatus.PROXY_ERROR)) {
                proxyErr++;
            }
        }
        return UserData.builder()
                .active(active)
                .cooldown(cooldown)
                .disabled(disabled)
                .suspended(suspended)
                .error(error)
                .invalid(invalid)
                .all(twitterAccounts.size())
                .proxyerr(proxyErr)
                .locked(locked)
                .stopping(stopping)
                .updated(updated)
                .subscription(entity.getSubscriptionType())
                .balance(entity.getBalance())
                .models(toTwitterAccountDatasFromEntities(entity.getModelEntities()))
                .build();
    }

    private List<ModelDto> toTwitterAccountDatasFromEntities(List<ModelEntity> entities) {
        return entities.stream().map(this::fromModelEntity).collect(Collectors.toList());
    }

    private ModelDto fromModelEntity(ModelEntity entity) {
        return ModelDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .accounts(entity.getTwitterAccounts().size())
                .build();
    }

    private UserEntity fromUserRegisterDto(UserRegister dto) {
        String email = dto.getEmail().toLowerCase().trim();
        return UserEntity.builder()
                .balance(5F)
                .email(email)
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.USER)
                .subscriptionType(SubscriptionType.BASIC)
                .build();
    }

    private AuthDto toUserAuthDto(UserEntity entity) {
        return AuthDto.builder()
                .email(entity.getEmail())
                .password(entity.getPassword())
                .role(entity.getRole().getAuthority())
                .build();
    }
}
