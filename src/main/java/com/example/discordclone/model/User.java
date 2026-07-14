package com.example.discordclone.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String nickname;
    private String avatarUrl;
    
    private String theme = "dark"; // dark / light
    private String language = "en"; // en, ru, be, pl

    private LocalDateTime lastNicknameChange;
    private LocalDateTime lastLoginChange;
    private LocalDateTime lastEmailOrPasswordChange;

    private boolean emailVerified = false;
    private String emailVerificationCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public LocalDateTime getLastNicknameChange() { return lastNicknameChange; }
    public void setLastNicknameChange(LocalDateTime lastNicknameChange) { this.lastNicknameChange = lastNicknameChange; }

    public LocalDateTime getLastLoginChange() { return lastLoginChange; }
    public void setLastLoginChange(LocalDateTime lastLoginChange) { this.lastLoginChange = lastLoginChange; }

    public LocalDateTime getLastEmailOrPasswordChange() { return lastEmailOrPasswordChange; }
    public void setLastEmailOrPasswordChange(LocalDateTime lastEmailOrPasswordChange) { this.lastEmailOrPasswordChange = lastEmailOrPasswordChange; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getEmailVerificationCode() { return emailVerificationCode; }
    public void setEmailVerificationCode(String emailVerificationCode) { this.emailVerificationCode = emailVerificationCode; }
}