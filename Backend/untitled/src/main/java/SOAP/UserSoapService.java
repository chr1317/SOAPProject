package SOAP;

import Entity.User;
import Service.UserService;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

import java.util.List;
import java.util.stream.Collectors;

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
                    .map(u -> "ID=" + u.getId() + ", email=" + u.getEmail())
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