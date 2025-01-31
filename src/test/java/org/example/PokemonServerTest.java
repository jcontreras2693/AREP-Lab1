package org.example;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

class PokemonServerTest {
    private static final String BASE_URL = "http://localhost:35000";
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void startServer() throws InterruptedException {
        // Iniciar el servidor en un hilo separado
        new Thread(() -> {
            try {
                PokemonServer.main(new String[]{});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Esperar que el servidor inicie
        Thread.sleep(1000);
    }

    @Test
    @Order(1)
    void testGetTeam() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/pokemon"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        // Verificar código de estado
        assertEquals(200, response.statusCode());

        // Verificar estructura JSON
        String jsonResponse = response.body();
        assertTrue(jsonResponse.startsWith("["));
        assertTrue(jsonResponse.contains("Pikachu"));
        assertTrue(jsonResponse.contains("25"));
    }

    @Test
    @Order(2)
    void testAddPokemon() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"name\":\"Charizard\",\"level\":50}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/pokemon"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        // Verificar código de estado
        assertEquals(201, response.statusCode());

        // Verificar respuesta
        String responseBody = response.body();
        assertTrue(responseBody.contains("success"));

        // Verificar que se agregó el Pokémon
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/pokemon"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());
        assertTrue(getResponse.body().contains("Charizard"));
        assertTrue(getResponse.body().contains("50"));
    }
}
