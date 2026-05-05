package SOAP;

import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.handler.Handler;

import java.util.List;

public class SOAPServer {

    public static void main(String[] args) {
        String userServiceUrl = "http://0.0.0.0:8080/UserService";
        String accountServiceUrl = "http://0.0.0.0:8081/AccountService";

        Endpoint userEndpoint = Endpoint.create(new UserSoapService());
        userEndpoint.getBinding().setHandlerChain(
                List.<Handler>of(new LoggingHandler())
        );
        userEndpoint.publish(userServiceUrl);

        Endpoint accountEndpoint = Endpoint.create(new AccountSoapService());
        accountEndpoint.getBinding().setHandlerChain(
                List.<Handler>of(new LoggingHandler())
        );
        accountEndpoint.publish(accountServiceUrl);

        System.out.println("STARTUJE SOAP.SOAPServer Z HANDLEREM");
        System.out.println("SOAP server działa.");
        System.out.println("UserService: " + userServiceUrl + "?wsdl");
        System.out.println("AccountService: " + accountServiceUrl + "?wsdl");
    }
}