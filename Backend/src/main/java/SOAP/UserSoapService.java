package SOAP;

import Config.JpaUtil;
import DAO.UserDao;
import Entity.User;
import Service.UserService;
import jakarta.activation.DataHandler;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.persistence.EntityManager;
import jakarta.xml.ws.soap.MTOM;

import jakarta.mail.util.ByteArrayDataSource;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@MTOM
@WebService
public class UserSoapService {

    private final UserService userService = new UserService();

    @WebMethod
    public String ping() {
        return "User SOAP działa";
    }

    @WebMethod
    public String createUser(
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName,
            @WebParam(name = "email") String email,
            @WebParam(name = "password") String password
    ) {
        try {
            userService.createUser(firstName, lastName, email, password);
            return "Użytkownik utworzony poprawnie.";
        } catch (Exception e) {
            return "Błąd tworzenia użytkownika: " + e.getMessage();
        }
    }

    @WebMethod
    public String uploadAvatar(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "file") DataHandler file
    ) {
        EntityManager em = null;

        try {
            em = JpaUtil.getEntityManager();
            em.getTransaction().begin();

            User user = new UserDao(em).findById(userId);
            if (user == null) {
                throw new RuntimeException("Użytkownik nie istnieje.");
            }

            try (InputStream input = file.getInputStream()) {
                byte[] bytes = input.readAllBytes();
                user.setAvatar(bytes);
            }

            em.merge(user);
            em.getTransaction().commit();

            return "Avatar zapisany poprawnie.";
        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return "Błąd uploadu avatara: " + e.getMessage();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @WebMethod
    public DataHandler getAvatar(@WebParam(name = "userId") Long userId) {
        EntityManager em = null;

        try {
            em = JpaUtil.getEntityManager();

            User user = new UserDao(em).findById(userId);
            if (user == null || user.getAvatar() == null) {
                throw new RuntimeException("Brak avatara.");
            }

            byte[] data = user.getAvatar();

            return new DataHandler(
                    new ByteArrayDataSource(data, "image/jpeg")
            );

        } catch (Exception e) {
            throw new RuntimeException("Błąd pobierania avatara.", e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @WebMethod
    public boolean authenticateUser(
            @WebParam(name = "email") String email,
            @WebParam(name = "password") String password
    ) {
        try {
            return userService.authenticateUser(email, password);
        } catch (Exception e) {
            return false;
        }
    }

    @WebMethod
    public Long getUserIdByEmail(@WebParam(name = "email") String email) {
        try {
            User user = userService.findUserByEmail(email);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @WebMethod
    public String updateUser(
            @WebParam(name = "userId") Long userId,
            @WebParam(name = "firstName") String firstName,
            @WebParam(name = "lastName") String lastName,
            @WebParam(name = "email") String email
    ) {
        try {
            userService.updateUser(userId, firstName, lastName, email);
            return "Użytkownik zaktualizowany poprawnie.";
        } catch (Exception e) {
            return "Błąd aktualizacji użytkownika: " + e.getMessage();
        }
    }

    @WebMethod
    public String getUserEmailById(@WebParam(name = "userId") Long userId) {
        try {
            User user = userService.findUserById(userId);
            return user != null ? user.getEmail() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @WebMethod
    public List<String> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return users.stream()
                    .map(u -> "ID=" + u.getId() + ", email=" + u.getEmail() + "First name=" + u.getFirstName() + "Last Name=" + u.getLastName() + "avatar=" + u.getAvatar())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("Błąd pobierania użytkowników: " + e.getMessage());
        }
    }

    @WebMethod
    public String deleteUser(@WebParam(name = "userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return "Użytkownik usunięty poprawnie.";
        } catch (Exception e) {
            return "Błąd usuwania użytkownika: " + e.getMessage();
        }
    }
}