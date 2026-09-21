package ke.eworks.ripoti.helpline;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;


@Repository
public class HelplineMessageRepository {

    private static final String COLLECTION_NAME = "HelplineMessages";

    private final Firestore firestore;

    public HelplineMessageRepository(final Firestore firestore) {
        this.firestore = firestore;
    }

    public List<HelplineMessage> findAll() {
        try {
            final QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .orderBy("createdAt", Query.Direction.ASCENDING).get().get();
            return snapshot.getDocuments().stream()
                    .map(document -> document.toObject(HelplineMessage.class))
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch helpline messages", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch helpline messages", e);
        }
    }

    public Optional<HelplineMessage> findById(final String id) {
        try {
            final DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(id).get().get();
            return document.exists() ? Optional.ofNullable(document.toObject(HelplineMessage.class)) : Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch helpline message " + id, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch helpline message " + id, e);
        }
    }

    public HelplineMessage save(final HelplineMessage message) {
        try {
            final CollectionReference collection = firestore.collection(COLLECTION_NAME);
            final DocumentReference documentReference = message.getId() != null
                    ? collection.document(message.getId()) : collection.document();
            message.setId(documentReference.getId());
            final ApiFuture<WriteResult> future = documentReference.set(message);
            future.get();
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to save helpline message", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save helpline message", e);
        }
    }

    public void deleteById(final String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to delete helpline message " + id, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to delete helpline message " + id, e);
        }
    }

}