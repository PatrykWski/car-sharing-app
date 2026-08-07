package carsharing.app;

import carsharing.app.model.RoleName;
import carsharing.app.model.User;
import carsharing.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.manager.email}")
    private String email;

    @Value("${app.manager.password}")
    private String rawPassword;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User manager = new User();
            manager.setEmail(email);
            manager.setPassword(passwordEncoder.encode(rawPassword));
            manager.setRoleName(RoleName.MANAGER);
            manager.setFirstName("System");
            manager.setLastName("Manager");
            userRepository.save(manager);
        }
    }
}
