package com.example;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:kelompok2gacorrr}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 1; i <= 20; i++) {
            String username = "Kelompok-" + i;
            if (!userRepository.existsByUsername(username)) {
                User user = new User();
                user.setUsername(username);
                String pwdBase = "Kelompok" + i;
                user.setPassword(passwordEncoder.encode(pwdBase + pwdBase + pwdBase));
                HashSet<String> roles = new HashSet<>();
                roles.add("ROLE_USER");
                user.setRoles(roles);
                userRepository.save(user);
            }
        }
        if (!userRepository.existsByUsername("adminkel2")) {
            User admin = new User();
            admin.setUsername("adminkel2");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            HashSet<String> roles = new HashSet<>();
            roles.add("ROLE_USER");
            roles.add("ROLE_ADMIN");
            admin.setRoles(roles);
            userRepository.save(admin);
        }
    }
}
