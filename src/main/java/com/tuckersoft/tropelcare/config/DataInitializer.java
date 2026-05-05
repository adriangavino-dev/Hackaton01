package com.tuckersoft.tropelcare.config;

import com.tuckersoft.tropelcare.guardian.Guardian;
import com.tuckersoft.tropelcare.guardian.GuardianRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GuardianRepository guardians;
    private final String adminName;
    private final String adminEmail;
    private final String adminNotificationEmail;

    public DataInitializer(GuardianRepository guardians,
                           @Value("${app.admin.display-name}") String adminName,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.notification-email}") String adminNotificationEmail) {
        this.guardians = guardians;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminNotificationEmail = adminNotificationEmail;
    }

    @Override
    public void run(String... args) {
        if (guardians.existsByEmail(adminEmail)) return;
        Guardian g = new Guardian();
        g.setDisplayName(adminName);
        g.setEmail(adminEmail);
        g.setNotificationEmail(adminNotificationEmail);
        g.setCreatedAt(Instant.now());
        guardians.save(g);
    }
}
