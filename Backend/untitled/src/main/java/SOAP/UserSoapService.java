package SOAP;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService
public class UserSoapService {

    @WebMethod
    public String ping() {
        return "SOAP backend działa";
    }

    @WebMethod
    public String hello(String name) {
        return "Cześć, " + name;
    }
}