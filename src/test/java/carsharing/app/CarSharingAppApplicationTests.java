package carsharing.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "file:.env")
class CarSharingAppApplicationTests {

    @Test
    void contextLoads() {
    }
}
