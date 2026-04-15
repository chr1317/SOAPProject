package SOAP;

import jakarta.xml.ws.Endpoint;

public class SOAPServer {

    public static void main(String[] args) {
        String url = "http://localhost:8080/UserService";

        Endpoint.publish(url, new UserSoapService());

        System.out.println("SOAP server działa pod: " + url);
        System.out.println("WSDL: " + url + "?wsdl");
    }
}