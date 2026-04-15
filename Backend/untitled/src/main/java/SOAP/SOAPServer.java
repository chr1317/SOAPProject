package SOAP;

import SOAP.AccountSoapService;
import jakarta.xml.ws.Endpoint;

public class SOAPServer {

    public static void main(String[] args) {
        String userServiceUrl = "http://localhost:8080/UserService";
        String accountServiceUrl = "http://localhost:8081/AccountService";

        Endpoint.publish(userServiceUrl, new Soap.UserSoapService());
        Endpoint.publish(accountServiceUrl, new AccountSoapService());

        System.out.println("SOAP server działa.");
        System.out.println("UserService: " + userServiceUrl + "?wsdl");
        System.out.println("AccountService: " + accountServiceUrl + "?wsdl");
    }
}