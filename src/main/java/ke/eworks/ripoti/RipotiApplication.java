package ke.eworks.ripoti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
public class RipotiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(RipotiApplication.class, args);
    }

}
