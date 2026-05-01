package Config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JpaUtil {

    private static EntityManagerFactory emf;

    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            Map<String, String> props = new HashMap<>();

            String dbUrl = System.getenv().getOrDefault(
                    "DB_URL",
                    "jdbc:mysql://localhost:3306/soap_currency_exchange?useSSL=false&serverTimezone=UTC"
            );

            String dbUser = System.getenv().getOrDefault("DB_USER", "root");
            String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "");

            props.put("jakarta.persistence.jdbc.url", dbUrl);
            props.put("jakarta.persistence.jdbc.user", dbUser);
            props.put("jakarta.persistence.jdbc.password", dbPassword);

            emf = Persistence.createEntityManagerFactory("soapCurrencyPU", props);
        }

        return emf.createEntityManager();
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}