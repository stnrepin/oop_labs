package utils;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class PersistenceEntityManagerUtils {
    private static EntityManager entityManager;

    private PersistenceEntityManagerUtils() {}

    public static EntityManager getEntityManager() {
        if (entityManager == null) {
                var emf =
                        Persistence.createEntityManagerFactory("pharmacy_persistence");
                entityManager = emf.createEntityManager();
        }
        return entityManager;
    }

    public interface EntityManagerCallback<R> {
        R call(EntityManager entityManager);
    }

    public static <T> T doTransaction(EntityManagerCallback<T> callback) {
        var em = getEntityManager();
        em.getTransaction().begin();
        var res = callback.call(entityManager);
        em.getTransaction().commit();
        return res;
    }

    public interface EntityManagerVoidCallback {
        void call(EntityManager entityManager);
    }

    public static void doTransaction(EntityManagerVoidCallback callback) {
        doTransaction((em) -> {
            callback.call(em);
            return null;
        });
    }
}