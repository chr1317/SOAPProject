package SOAP;

import jakarta.xml.ws.Endpoint;

import java.util.List;

public class SOAPServer {

    public static void main(String[] args) {
        String userServiceUrl = "http://localhost:8080/UserService";
        String accountServiceUrl = "http://localhost:8081/AccountService";

        Endpoint userEndpoint = Endpoint.publish(userServiceUrl, new UserSoapService());
        Endpoint accountEndpoint = Endpoint.publish(accountServiceUrl, new AccountSoapService());

        userEndpoint.getBinding().setHandlerChain(
                List.of(new LoggingHandler())
        );

        accountEndpoint.getBinding().setHandlerChain(
                List.of(new LoggingHandler())
        );

        System.out.println("SOAP server działa.");
        System.out.println("UserService: " + userServiceUrl + "?wsdl");
        System.out.println("AccountService: " + accountServiceUrl + "?wsdl");
    }
}