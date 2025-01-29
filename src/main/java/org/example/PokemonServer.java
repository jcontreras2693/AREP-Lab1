package org.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class PokemonServer {
    // Lista para almacenar el equipo Pokémon (en memoria)
    private static List<Pokemon> pokemonTeam = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Inicializar el equipo con Pokémon
        Pokemon pikachu = new Pokemon();
        pikachu.setName("Pikachu");
        pikachu.setLevel(25);
        pokemonTeam.add(pikachu);

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        while (true) {
            Socket clientSocket = acceptConnection(serverSocket);

            if (clientSocket != null) {
                handleRequest(clientSocket);
            }
        }
    }

    private static Socket acceptConnection(ServerSocket serverSocket) {
        try {
            System.out.println("Listo para recibir ...");
            return serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            return null;
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String inputLine = in.readLine();
            if (inputLine == null) return;

            String[] requestLine = inputLine.split(" ");
            String method = requestLine[0];
            String path = requestLine[1];

            // Manejar rutas REST
            if (path.startsWith("/api/pokemon")) {
                handleApiRequest(method, path, in, out);
            } else {
                // Manejar archivos estáticos (como antes)
                handleStaticRequest(method, path, in, out);
            }

            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleApiRequest(String method, String path, BufferedReader in, OutputStream out) throws IOException {
        if ("GET".equals(method)) {
            handleGetTeam(out);
        } else if ("POST".equals(method)) {
            handleAddPokemon(in, out);
        } else {
            sendError(out, 405, "Method Not Allowed");
        }
    }

    private static void handleStaticRequest(String method, String path, BufferedReader in, OutputStream out) throws IOException {
        if (!"GET".equals(method)) {
            sendError(out, 405, "Method Not Allowed");
            return;
        }

        // Ruta base para los archivos estáticos
        String basePath = "src/main/resources/web";
        String filePath = path.equals("/") ? "/index.html" : path;

        // Verificar si el archivo existe
        File requestedFile = new File(basePath + filePath);

        if (requestedFile.exists() && !requestedFile.isDirectory()) {
            // Obtener el tipo MIME del archivo
            String contentType = getMimeType(filePath);

            // Escribir la respuesta HTTP
            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + requestedFile.length() + "\r\n" +
                    "\r\n").getBytes());

            // Enviar el contenido del archivo
            sendFileContent(requestedFile, out);
        } else {
            // Archivo no encontrado
            sendError(out, 404, "Not Found");
        }
    }

    private static void handleGetTeam(OutputStream out) throws IOException {
        // Convertir lista a JSON manualmente (sin librerías)
        StringBuilder json = new StringBuilder("[");
        for (Pokemon p : pokemonTeam) {
            json.append(String.format("{\"name\":\"%s\",\"level\":%d},", p.getName(), p.getLevel()));
        }
        if (!pokemonTeam.isEmpty()) json.deleteCharAt(json.length() - 1);
        json.append("]");

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + json.length() + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "\r\n" +
                json;

        out.write(response.getBytes());
    }

    private static void handleAddPokemon(BufferedReader in, OutputStream out) throws IOException {
        // Leer el cuerpo de la solicitud
        StringBuilder body = new StringBuilder();
        int contentLength = 0;

        // Leer los headers para obtener Content-Length
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.substring(15).trim());
            }
        }

        // Leer el cuerpo de la solicitud
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);
            body.append(buffer);
        }

        // Parsear el cuerpo JSON
        String jsonBody = body.toString();
        System.out.println("Received JSON: " + jsonBody); // Para depuración

        try {
            // Extraer el nombre y el nivel del JSON
            String name = jsonBody.split("\"name\":\"")[1].split("\"")[0];
            String levelStr = jsonBody.split("\"level\":")[1].split("[^0-9]")[0];
            int level = Integer.parseInt(levelStr);

            // Crear y agregar Pokémon
            Pokemon pokemon = new Pokemon();
            pokemon.setName(name);
            pokemon.setLevel(level);
            pokemonTeam.add(pokemon);

            // Respuesta exitosa
            String response = "HTTP/1.1 201 Created\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +
                    "\r\n" +
                    "{\"status\":\"success\"}";
            out.write(response.getBytes());
        } catch (Exception e) {
            // Manejar errores de parsing
            System.err.println("Error parsing JSON: " + e.getMessage());
            sendError(out, 400, "Bad Request - Invalid JSON");
        }

        System.out.println("Received JSON: " + jsonBody);
    }

    private static void sendError(OutputStream out, int code, String message) throws IOException {
        String response = "HTTP/1.1 " + code + " " + message + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Error: " + message;
        out.write(response.getBytes());
    }

    private static void sendFileContent(File file, OutputStream out) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private static String getMimeType(String filePath) {
        if (filePath.endsWith(".html")) {
            return "text/html";
        } else if (filePath.endsWith(".css")) {
            return "text/css";
        } else if (filePath.endsWith(".js")) {
            return "application/javascript";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }
}