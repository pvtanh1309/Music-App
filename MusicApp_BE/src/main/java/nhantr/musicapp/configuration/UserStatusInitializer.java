package nhantr.musicapp.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import nhantr.musicapp.repository.UserRepository;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.UserStatus;

import java.util.List;

@Component
public class UserStatusInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserStatusInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Find users with null status and set to ACTIVE
        List<User> users = userRepository.findAll();
        boolean changed = false;
        for (User u : users) {
            if (u.getStatus() == null) {
                u.setStatus(UserStatus.ACTIVE);
                changed = true;
            }
        }

        if (changed) {
            userRepository.saveAll(users);
        }
    }
}
