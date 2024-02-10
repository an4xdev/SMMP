import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ApiGatewayHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        {
            // Wykonaj odpowiednią akcję na podstawie żądania.
            // if (exchange.getRequestMethod().equals("POST")
            //         && exchange.getRequestURI().getPath().equals("/agents/register")) {
            //     System.out.println("Agent...");
            // } else if (exchange.getRequestMethod().equals("POST")
            //         && exchange.getRequestURI().getPath().equals("/microservices/open")) {
            //     System.out.println("Open Microservice");
            // } else {
            //     System.out.println("Unknown request");
            // }

            // testing request
            System.out.println("Address: " + exchange.getRemoteAddress().getAddress() + ", port: "
                    + exchange.getRemoteAddress().getPort() + ", hostname: " + exchange.getRemoteAddress().getHostName()
                    + ", hostString: " + exchange.getRemoteAddress().getHostString());
            // System.out.println("Headers: ");
            // exchange.getRequestHeaders().forEach((k, v) -> System.out.println("key: " + k + ", value: " + v));
            System.out.println("Request method: " + exchange.getRequestMethod());
            System.out.println("Request URI: " + exchange.getRequestURI());
            System.out.println("Protocole" + exchange.getProtocol());

            // got from: https://stackoverflow.com/a/43044139
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
                // exchange.sendResponseHeaders(204, -1);
                // HTTP status codes got from: https://stackoverflow.com/a/730307
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_NO_CONTENT, -1);
                return;
            }
            String responseBody = Integer.toString(exchange.getRemoteAddress().getPort());
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseBody.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBody.getBytes());
                os.close();
            }
        }
    }
}
