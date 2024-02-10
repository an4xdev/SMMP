import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.Test;

public class TestApi {

    private static final String API_URL = "http://localhost:8000/api";

    @Test
    public void testConnection() {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(API_URL);
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new AssertionError("Error with connetion to Server. Status code: " + response.statusCode());
            }

            String responseBody = response.body();

            Integer.parseInt(responseBody);

        } catch (Exception e) {
            throw new AssertionError("Error: " + e.getMessage());
        }

    }
}
