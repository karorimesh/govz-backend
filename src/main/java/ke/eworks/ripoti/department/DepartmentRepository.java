package ke.eworks.ripoti.department;

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
public class DepartmentRepository {

    private static final String COLLECTION_NAME = "Departments";

    private final Firestore firestore;

    public DepartmentRepository(final Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Department> findAll() {
        try {
            final CollectionReference collection = firestore.collection(COLLECTION_NAME);
            final QuerySnapshot snapshot = collection.orderBy("createdAt", Query.Direction.ASCENDING).get().get();
            return snapshot.getDocuments().stream()
                    .map(document -> document.toObject(Department.class))
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch departments", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch departments", e);
        }
    }

    public Optional<Department> findById(final String id) {
        try {
            final DocumentSnapshot document = firestore.collection(COLLECTION_NAME).document(id).get().get();
            return document.exists() ? Optional.ofNullable(document.toObject(Department.class)) : Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to fetch department " + id, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to fetch department " + id, e);
        }
    }

    public Department save(final Department department) {
        try {
            final CollectionReference collection = firestore.collection(COLLECTION_NAME);
            final DocumentReference documentReference = department.getId() != null
                    ? collection.document(department.getId())
                    : collection.document();
            department.setId(documentReference.getId());
            final ApiFuture<WriteResult> future = documentReference.set(department);
            future.get();
            return department;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to save department", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to save department", e);
        }
    }

    public void deleteById(final String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to delete department " + id, e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to delete department " + id, e);
        }
    }

}
