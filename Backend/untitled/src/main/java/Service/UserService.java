package Service;

import Config.JpaUtil;
import DAO.UserDao;
import Entity.User;
import Entity.Wallet;
import Util.PasswordUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class UserService {

    public void createUser(String firstName, String lastName, String email, String plainPassword) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            em.getTransaction().begin();

            UserDao userDao = new UserDao(em);

            User existingUser = userDao.findByEmail(email);
            if (existingUser != null) {
                throw new RuntimeException("Użytkownik o podanym emailu już istnieje.");
            }

            String hashedPassword = PasswordUtil.hashPassword(plainPassword);

            User user = new User(firstName, lastName, email, hashedPassword);

            Wallet wallet = new Wallet();
            user.setWallet(wallet);

            userDao.save(user);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Błąd podczas tworzenia użytkownika.", e);
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