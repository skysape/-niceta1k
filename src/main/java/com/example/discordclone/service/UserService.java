package com.example.discordclone.service;

import com.example.discordclone.model.User;
import com.example.discordclone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    // Password must be at least 8 symbols, with only letters and digits (supports EN, RU, PL, BE)
    private static final String PASSWORD_REGEX = "^[a-zA-Z0-9а-яА-ЯёЁąęćłńóśźżĄĆĘŁŃÓŚŹŻ]{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public User registerUser(String login, String password, String email, String nickname) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must be at least 8 characters, letters & digits only, no symbols.");
        }

        if (userRepository.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Login already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setNickname(nickname != null ? nickname : login);
        
        String verificationCode = String.format("%06d", new Random().nextInt(1000000));
        user.setEmailVerificationCode(verificationCode);
        
        userRepository.save(user);
        emailService.sendVerificationCode(email, verificationCode);

        return user;
    }

    public boolean verifyEmail(String email, String code) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEmailVerificationCode().equals(code)) {
                user.setEmailVerified(true);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public void updateNickname(User user, String newNickname) {
        if (user.getLastNicknameChange() != null && 
            user.getLastNicknameChange().plusDays(3).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Nickname can only be changed once every 3 days.");
        }
        user.setNickname(newNickname);
        user.setLastNicknameChange(LocalDateTime.now());
        userRepository.save(user);
    }

    public void updateLogin(User user, String newLogin) {
        if (user.getLastLoginChange() != null && 
            user.getLastLoginChange().plusDays(7).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Login can only be changed once every 7 days.");
        }
        if (userRepository.findByLogin(newLogin).isPresent()) {
            throw new IllegalArgumentException("Login already taken.");
        }
        user.setLogin(newLogin);
        user.setLastLoginChange(LocalDateTime.now());
        userRepository.save(user);
    }

    public void updateEmailOrPassword(User user, String newEmail, String newPassword) {
        if (user.getLastEmailOrPasswordChange() != null && 
            user.getLastEmailOrPasswordChange().plusMonths(1).isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Email or Password can only be changed once a month.");
        }
        
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("Email already taken.");
            }
            user.setEmail(newEmail);
            user.setEmailVerified(false);
            String verificationCode = String.format("%06d", new Random().nextInt(1000000));
            user.setEmailVerificationCode(verificationCode);
            emailService.sendVerificationCode(newEmail, verificationCode);
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                throw new IllegalArgumentException("Password must be at least 8 characters, letters & digits only.");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        user.setLastEmailOrPasswordChange(LocalDateTime.now());
        userRepository.save(user);
    }
}