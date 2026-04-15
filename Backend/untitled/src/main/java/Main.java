import Config.JpaUtil;
import Entity.User;
import Entity.Wallet;
import jakarta.persistence.EntityManager;

public class Main {
    public static void main(String[] args) {
        EntityManager em = null;

        try {
            em = JpaUtil.getEntityManager();

            em.getTransaction().begin();

            User user = new User(
                    "Maciej",
                    "Testowy",
                    "maciej.testowy2@example.com",
                    "example_hash_123"
            );

            Wallet wallet = new Wallet();
            user.setWallet(wallet);

            em.persist(user);

            em.getTransaction().commit();

            System.out.println("Użytkownik i portfel zapisani poprawnie.");
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            JpaUtil.shutdown();
        }
    }
}