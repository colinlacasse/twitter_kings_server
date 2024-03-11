package com.twittersfs.server.security;

import com.twittersfs.server.entities.TokenEntity;
import com.twittersfs.server.dtos.user.UserRegister;
import com.twittersfs.server.repos.TokenRepo;
import com.twittersfs.server.services.UserService;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;
    private final TokenRepo tokenRepo;
    private final JavaMailSender emailSender;

    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder, JwtProvider jwtProvider, TokenRepo tokenRepo, JavaMailSender emailSender) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.tokenRepo = tokenRepo;
        this.emailSender = emailSender;
    }

    @Override
    public void register(UserRegister dto) {
        userService.register(dto);
    }

    @Override
    public JwtResponse login(@NonNull JwtRequest authRequest) {
        String email = authRequest.getEmail().toLowerCase().trim();
        final AuthDto user = userService.getUserAuthDto(email);
        if (passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);
            saveToken(user.getEmail(), refreshToken);
            return new JwtResponse(accessToken, refreshToken);
        } else {
            throw new RuntimeException("Invalid password");
        }
    }

    @Override
    public JwtResponse getAccessToken(@NonNull String refreshToken) {
        jwtProvider.validateRefreshToken(refreshToken);
        final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
        final String login = claims.getSubject();
        final String saveRefreshToken = tokenRepo.findByEmail(login).getRefreshToken();
        if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
            final AuthDto user = userService.getUserAuthDto(login);
            final String accessToken = jwtProvider.generateAccessToken(user);
            return new JwtResponse(accessToken, null);
        }
        return new JwtResponse(null, null);
    }

    @Override
    public JwtResponse refresh(@NonNull String refreshToken) {
        jwtProvider.validateRefreshToken(refreshToken);
        final Claims claims = jwtProvider.getRefreshClaims(refreshToken);
        final String login = claims.getSubject();
        final String savedRefreshToken = tokenRepo.findByEmail(login).getRefreshToken();
        if (savedRefreshToken != null && savedRefreshToken.equals(refreshToken)) {
            final AuthDto user = userService.getUserAuthDto(login);
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String newRefreshToken = jwtProvider.generateRefreshToken(user);
            saveToken(user.getEmail(), newRefreshToken);
            return new JwtResponse(accessToken, newRefreshToken);
        }
        throw new RuntimeException("Invalid jwt token");
    }

    @Override
    public void logout(String email) {
        TokenEntity entity = Optional.ofNullable(tokenRepo.findByEmail(email))
                .orElseThrow(() -> new RuntimeException("User with such email does not exist"));
        entity.setRefreshToken(null);
        entity.setAccessToken(null);
        tokenRepo.save(entity);
    }

    @Override
    public void sendResetPasswordEmail(ResetPassword resetData) {
        final AuthDto user = userService.getUserAuthDto(resetData.getEmail());
        final String accessToken = jwtProvider.generateAccessToken(user);
        String email = "http://space-traff.site/new-password/" + accessToken;
        sendEmail(email, resetData.getEmail());
    }

    @Override
    public void resetPassword(String email, ResetPassword resetData) {
        userService.resetPassword(email, resetData.getPassword());
    }


    private void saveToken(String email, String token) {
        if (tokenRepo.findByEmail(email) == null) {
            TokenEntity tokenEntity = new TokenEntity();
            tokenEntity.setEmail(email);
            tokenEntity.setRefreshToken(token);
            tokenRepo.save(tokenEntity);
        } else {
            TokenEntity entity = tokenRepo.findByEmail(email);
            entity.setEmail(email);
            entity.setRefreshToken(token);
            tokenRepo.save(entity);
        }
    }

    private void sendEmail(String msg, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset-Password");
        message.setText(msg);
        emailSender.send(message);
    }
}
