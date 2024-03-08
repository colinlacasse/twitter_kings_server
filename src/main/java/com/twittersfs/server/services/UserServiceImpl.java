package com.twittersfs.server.services;

import com.twittersfs.server.entities.ModelEntity;
import com.twittersfs.server.entities.UserEntity;
import com.twittersfs.server.enums.Role;
import com.twittersfs.server.enums.SubscriptionType;
import com.twittersfs.server.dtos.model.ModelDto;
import com.twittersfs.server.dtos.user.UserData;
import com.twittersfs.server.dtos.user.UserRegister;
import com.twittersfs.server.repos.UserEntityRepo;
import com.twittersfs.server.security.AuthDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    private UserData fromUserEntity(UserEntity entity){
        return UserData.builder()
                .balance(entity.getBalance())
                .models(toTwitterAccountDatasFromEntities(entity.getModelEntities()))
                .build();
    }

    private List<ModelDto> toTwitterAccountDatasFromEntities(List<ModelEntity> entities) {
        return entities.stream().map(this::fromModelEntity).collect(Collectors.toList());
    }

    private ModelDto fromModelEntity(ModelEntity entity){
        return ModelDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    private UserEntity fromUserRegisterDto(UserRegister dto) {
        String email = dto.getEmail().toLowerCase().trim();
        return UserEntity.builder()
                .balance(0)
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
