package Service;

import Config.JpaUtil;
import DAO.UserDao;
import DAO.WalletDao;
import Entity.User;
import Entity.Wallet;
import Util.PasswordUtil;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {

    public Long createUser(String firstName, String lastName, String email, String password) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            UserDao userDao = new UserDao(em);
            WalletDao walletDao = new WalletDao(em);

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);

            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            user.setPasswordHash(hashed);

            userDao.save(user);


            em.flush();

            Wallet wallet = new Wallet();
            wallet.setUser(user);
            walletDao.save(wallet);

            em.getTransaction().commit();

            return user.getId();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Błąd tworzenia użytkownika", e);
        } finally {
            em.close();
        }
    }

    public User findUserById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDao userDao = new UserDao(em);
            return userDao.findById(id);
        } finally {
            em.close();
        }
    }

    public User findUserByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDao userDao = new UserDao(em);
            return userDao.findByEmail(email);
        } finally {
            em.close();
        }
    }

    public List<User> getAllUsers() {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDao userDao = new UserDao(em);
            return userDao.findAll();
        } finally {
            em.close();
        }
    }

    public boolean authenticateUser(String email, String plainPassword) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDao userDao = new UserDao(em);
            User user = userDao.findByEmail(email);

            if (user == null) {
                return false;
            }

            return PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash());
        } finally {
            em.close();
        }
    }

    public void deleteUser(Long userId) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            UserDao userDao = new UserDao(em);
            User user = userDao.findById(userId);

            if (user == null) {
                throw new RuntimeException("Użytkownik nie istnieje.");
            }

            userDao.delete(user);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Błąd podczas usuwania użytkownika.", e);
        } finally {
            em.close();
        }
    }
}