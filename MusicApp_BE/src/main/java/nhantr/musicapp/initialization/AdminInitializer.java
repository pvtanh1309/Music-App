package nhantr.musicapp.initialization;

import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.Role;
import nhantr.musicapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import static java.time.LocalDateTime.now;

/**
 * Initialize admin user on application startup
 */
@Component
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        // Check if admin user already exists
        if (userRepository.existsByUsername("admin")) {
            log.info("ℹ️  Admin user already exists, skipping initialization");
            return;
        }

        try {
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@musicapp.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .createdAt(now())
                    .build();

            userRepository.save(adminUser);
            log.info("✅ Admin user created successfully!");
            log.info("   Username: admin");
            log.info("   Password: admin123");
            log.warn("⚠️  IMPORTANT: Change the admin password after first login!");
        } catch (Exception ex) {
            log.error("❌ Failed to create admin user: {}", ex.getMessage(), ex);
        }
    }
}
