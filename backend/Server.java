import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class Server {

    public static void main(String[] args) throws IOException {

        int port = Integer.parseInt(System.getenv("PORT", "8080"));

        HttpServer server = HttpServer.create(
            new InetSocketAddress(port),
            0
        );

        server.createContext("/", exchange -> {

            String method = exchange.getRequestMethod();

            String response = "Request method: "+method;

            exchange.sendResponseHeaders(200, response.length());

            exchange.getResponseBody().write(response.getBytes());

            exchange.getResponseBody().close();
        });
        server.createContext("/contact", exchange -> {

    // CORS
    exchange.getResponseHeaders().add(
        "Access-Control-Allow-Origin",
        "*"
    );

    exchange.getResponseHeaders().add(
        "Access-Control-Allow-Methods",
        "POST, OPTIONS"
    );

    exchange.getResponseHeaders().add(
        "Access-Control-Allow-Headers",
        "Content-Type"
    );

    String method = exchange.getRequestMethod();

    // Handle CORS preflight request
    if (method.equals("OPTIONS")) {
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
        return;
    }

    if (method.equals("POST")) {

        InputStream inputStream = exchange.getRequestBody();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream));

        String data = reader.readLine();

        String[] fields = data.split("&");

String name = "";
String email = "";
String msg = "";

for (String field : fields) {

    String[] pair = field.split("=", 2);

    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
    String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);

    if (key.equals("name")) {
        name = value;
    }

    if (key.equals("email")) {
        email = value;
    }

    if (key.equals("message")) {
        msg = value;
    }
}

System.out.println("Name: " + name);
System.out.println("Email: " + email);
System.out.println("Message: " + msg);
sendEmail(name, email, msg);

        String response = "Data received successfully!";

        exchange.sendResponseHeaders(200, response.length());

        exchange.getResponseBody().write(response.getBytes());

        exchange.getResponseBody().close();

    } else {

        String response = "Only POST requests are allowed.";

        exchange.sendResponseHeaders(405, response.length());

        exchange.getResponseBody().write(response.getBytes());

        exchange.getResponseBody().close();
    }
});

        server.start();

        System.out.println("Server started at http://localhost:8080");
    }
    public static void sendEmail(String name, String email, String msg) {

    String senderEmail = System.getenv("EMAIL");
    String appPassword = System.getenv("EMAIL_PASSWORD");
    String receiverEmail = System.getenv("RECEIVER_EMAIL");

    Properties properties = new Properties();

    properties.put("mail.smtp.host", "smtp.gmail.com");
    properties.put("mail.smtp.port", "587");
    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");

    Session session = Session.getInstance(properties, new Authenticator() {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {

            return new PasswordAuthentication(
                senderEmail,
                appPassword
            );
        }
    });

    try {

        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(senderEmail));

        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(receiverEmail)
        );

        message.setSubject("New Portfolio Contact Message");

        message.setText(
            "Name: " + name + "\n" +
            "Email: " + email + "\n\n" +
            "Message:\n" + msg
        );

        Transport.send(message);

        System.out.println("Email sent successfully!");

    } catch (MessagingException e) {

        System.out.println("Failed to send email.");
        e.printStackTrace();
    }
}
}