package ke.eworks.ripoti.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;


@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirestoreClient.getFirestore();
        }

        ClassPathResource resource =
                new ClassPathResource("ripoti.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(
                        GoogleCredentials.fromStream(
                                resource.getInputStream()
                        )
                )
                .build();

        FirebaseApp.initializeApp(options);
        return FirestoreClient.getFirestore();
    }

}
