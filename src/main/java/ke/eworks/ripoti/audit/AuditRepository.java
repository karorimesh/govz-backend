package ke.eworks.ripoti.audit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Repository;


@Repository
public class AuditRepository {

    private static final String COLLECTION_NAME = "CallAudits";

    private final Firestore firestore;

    public AuditRepository(final Firestore firestore) {
        this.firestore = firestore;
    }

    public AuditLog save(final AuditLog auditLog) {
        try {
            final CollectionReference collection = firestore.collection(COLLECTION_NAME);
            final DocumentReference documentReference = collection.document();
            auditLog.setId(documentReference.getId());
            final ApiFuture<WriteResult> future = documentReference.set(auditLog);
            future.get();
            return auditLog;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to save audit log", e);
        }
    }

    public List<AuditDTO> findAll(final int page, final int size, final String requestId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME);
            if (requestId != null && !requestId.isBlank()) {
                query = query.whereEqualTo("requestId", requestId);
            }
            final QuerySnapshot snapshot = query.orderBy("timestamp", Query.Direction.DESCENDING)
                    .offset(page * size)
                    .limit(size)
                    .get()
                    .get();
            return snapshot.getDocuments().stream()
                    .map(document -> document.toObject(AuditDTO.class))
                    .toList();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch audit logs", e);
        }
    }

    public List<AuditLog> findUnprocessed() {
        try {
            final QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("processed", false)
                    .get()
                    .get();
            var logs = snapshot.getDocuments().stream()
                    .map(document -> document.toObject(AuditLog.class))
                    .toList();
            return logs;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch unprocessed audit logs", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch unprocessed audit logs", e);
        }
    }

    public void markProcessed(final String id) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .update("processed", true)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to mark audit log as processed", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to mark audit log as processed", e);
        }
    }

    public long count(final String requestId) {
        try {
            Query query = firestore.collection(COLLECTION_NAME);
            if (requestId != null && !requestId.isBlank()) {
                query = query.whereEqualTo("requestId", requestId);
            }
            return query.count().get().get().getCount();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to count audit logs", e);
        }
    }

}
