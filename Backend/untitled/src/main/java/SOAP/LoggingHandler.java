package SOAP;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import javax.xml.namespace.QName;
import java.time.LocalDateTime;
import java.util.Set;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean outbound = Boolean.TRUE.equals(
                context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)
        );

        System.out.println();
        System.out.println("========== SOAP " + (outbound ? "RESPONSE" : "REQUEST") + " ==========");
        System.out.println("Time: " + LocalDateTime.now());

        try {
            SOAPMessage message = context.getMessage();
            message.writeTo(System.out);
            System.out.println();
        } catch (Exception e) {
            System.out.println("Błąd logowania SOAP message: " + e.getMessage());
        }

        System.out.println("=====================================");
        System.out.println();

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        System.out.println("========== SOAP FAULT ==========");
        try {
            context.getMessage().writeTo(System.out);
            System.out.println();
        } catch (Exception e) {
            System.out.println("Błąd logowania SOAP fault: " + e.getMessage());
        }
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}