package com.helpdesk.helpdesk_api.config;

import com.helpdesk.helpdesk_api.entity.User;
import com.helpdesk.helpdesk_api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

//Add this temporary development-only runner
@Configuration
@Profile("dev")
public class DevUserSeeder {

    @Bean
    CommandLineRunner seedDevelopmentUser(
            UserRepository users,
            PasswordEncoder encoder) {

        return args -> {
            User user = users.findByUsername("helpdesk.dev")
                    .orElseGet(User::new);

            user.setUsername("helpdesk.dev");
            user.setEmail("helpdesk.dev@example.com");
            user.setRole("ADMIN");

            if (user.getId() == null
                    || user.getPasswordHash() == null
                    || user.getPasswordHash().equals(
                    "DEV_ONLY_NOT_A_REAL_PASSWORD_HASH")) {
                user.setPasswordHash(
                        encoder.encode("ChangeMe123!"));
            }

            users.save(user);
        };
    }
}
//This seeds an account only when the dev profile is active.To activate it locally, add:spring.profiles.active=dev
//        Use this account only for local testing, and remove the seeder before production. Do not commit real credentials to Git.