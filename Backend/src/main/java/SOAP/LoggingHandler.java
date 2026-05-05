package SOAP;

import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Set;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean outbound = Boolean.TRUE.equals(
                context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)
        );

        String direction = outbound ? "SOAP RESPONSE" : "SOAP REQUEST";
        Object operation = context.get(MessageContext.WSDL_OPERATION);
        Object path = context.get(MessageContext.PATH_INFO);

        System.out.println();
        System.out.println("==================================================");
        System.out.println(direction);
        System.out.println("Time: " + LocalDateTime.now());
        System.out.println("Path: " + path);
        System.out.println("Operation: " + operation);
        System.out.println("--------------------------------------------------");

        try {
            SOAPMessage message = context.getMessage();
            System.out.println(formatSoap(message));
        } catch (Exception e) {
            System.out.println("Błąd logowania SOAP message: " + e.getMessage());
        }

        System.out.println("==================================================");
        System.out.println();

        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("SOAP FAULT");
        System.out.println("Time: " + LocalDateTime.now());
        System.out.println("--------------------------------------------------");

        try {
            System.out.println(formatSoap(context.getMessage()));
        } catch (Exception e) {
            System.out.println("Błąd logowania SOAP fault: " + e.getMessage());
        }

        System.out.println("==================================================");
        System.out.println();

        return true;
    }

    private String formatSoap(SOAPMessage message) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();

        transformer.transform(
                new DOMSource(message.getSOAPPart()),
                new StreamResult(writer)
        );

        return writer.toString();
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}