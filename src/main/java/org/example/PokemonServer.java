package org.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Servidor web simple que maneja un equipo de Pokémon, permitiendo agregar nuevos
 * Pokémon mediante solicitudes POST y recuperar el equipo actual con GET.
 * También sirve archivos estáticos para la interfaz web.
 */
public class PokemonServer {
    private static List<Pokemon> pokemonTeam = new ArrayList<>();

    /**
     * Método principal que inicia el servidor y maneja las conexiones entrantes.
     *
     * @param args Argumentos de línea de comandos (no se utilizan)
     * @throws IOException Si ocurre un error al iniciar el servidor
     */
    public static void main(String[] args) throws IOException {
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

    /**
     * Acepta una conexión entrante de cliente.
     *
     * @param serverSocket Socket del servidor en modo de escucha
     * @return Socket del cliente conectado o null sí hay error
     */
    private static Socket acceptConnection(ServerSocket serverSocket) {
        try {
            System.out.println("Listo para recibir ...");
            return serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            return null;
        }
    }

    /**
     * Maneja una solicitud HTTP entrante y dirige a los "handlers" correspondientes.
     *
     * @param clientSocket Socket del cliente para la comunicación
     */
    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String inputLine = in.readLine();
            if (inputLine == null) return;

            String[] requestLine = inputLine.split(" ");
            String method = requestLine[0];
            String path = requestLine[1];

            if (path.startsWith("/api/pokemon")) {
                handleApiRequest(method, in, out);
            } else {
                handleStaticRequest(method, path, out);
            }

            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Maneja solicitudes a la API de Pokémon (/api/pokemon).
     *
     * @param method Método HTTP (GET/POST)
     * @param in BufferedReader para leer la solicitud
     * @param out OutputStream para enviar la respuesta
     * @throws IOException Si ocurre un error de E/S
     */
    private static void handleApiRequest(String method, BufferedReader in, OutputStream out) throws IOException {
        if ("GET".equals(method)) {
            handleGetTeam(out);
        } else if ("POST".equals(method)) {
            handleAddPokemon(in, out);
        } else {
            sendError(out, 405, "Method Not Allowed");
        }
    }

    /**
     * Maneja solicitudes de archivos estáticos (HTML, CSS, JS, imágenes).
     *
     * @param method Método HTTP (solo GET permitido)
     * @param path Ruta del archivo solicitado
     * @param out OutputStream para enviar la respuesta
     * @throws IOException Si ocurre un error de E/S
     */
    private static void handleStaticRequest(String method, String path, OutputStream out) throws IOException {
        if (!"GET".equals(method)) {
            sendError(out, 405, "Method Not Allowed");
            return;
        }

        String basePath = "src/main/resources/web";
        String filePath = path.equals("/") ? "/index.html" : path;

        File requestedFile = new File(basePath + filePath);

        if (requestedFile.exists() && !requestedFile.isDirectory()) {
            String contentType = getMimeType(filePath);

            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + requestedFile.length() + "\r\n" +
                    "\r\n").getBytes());

            sendFileContent(requestedFile, out);
        } else {
            sendError(out, 404, "Not Found");
        }
    }

    /**
     * Devuelve el equipo Pokémon actual en formato JSON.
     *
     * @param out OutputStream donde se escribe la respuesta
     * @throws IOException Si ocurre un error de E/S
     */
    private static void handleGetTeam(OutputStream out) throws IOException {
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

    /**
     * Añade un nuevo Pokémon al equipo desde una solicitud POST.
     *
     * @param in BufferedReader para leer el cuerpo de la solicitud
     * @param out OutputStream para enviar la respuesta
     * @throws IOException Si ocurre un error de E/S
     */
    private static void handleAddPokemon(BufferedReader in, OutputStream out) throws IOException {
        StringBuilder body = new StringBuilder();
        int contentLength = 0;

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.substring(15).trim());
            }
        }

        // Leer cuerpo JSON
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);
            body.append(buffer);
        }

        String jsonBody = body.toString();
        System.out.println("Received JSON: " + jsonBody);

        try {
            if (pokemonTeam.size() >= 6) {
                sendError(out, 400, "Bad Request - Equipo completo");
                return;
            }

            String name = jsonBody.split("\"name\":\"")[1].split("\"")[0];
            String levelStr = jsonBody.split("\"level\":")[1].split("[^0-9]")[0];
            int level = Integer.parseInt(levelStr);

            Pokemon pokemon = new Pokemon();
            pokemon.setName(name);
            pokemon.setLevel(level);
            pokemonTeam.add(pokemon);

            String response = """
                    HTTP/1.1 201 Created\r
                    Content-Type: application/json\r
                    Access-Control-Allow-Origin: *\r
                    \r
                    {"status":"success"}""";
            out.write(response.getBytes());
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            sendError(out, 400, "Bad Request - Invalid JSON");
        }
    }

    /**
     * Envía una respuesta de error HTTP.
     *
     * @param out OutputStream donde escribir la respuesta
     * @param code Código de estado HTTP
     * @param message Mensaje de error descriptivo
     * @throws IOException Si ocurre un error de E/S
     */
    private static void sendError(OutputStream out, int code, String message) throws IOException {
        String response = "HTTP/1.1 " + code + " " + message + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "Error: " + message;
        out.write(response.getBytes());
    }

    /**
     * Envía el contenido de un archivo a través del OutputStream.
     *
     * @param file Archivo a enviar
     * @param out OutputStream destino
     * @throws IOException Si ocurre un error de E/S
     */
    private static void sendFileContent(File file, OutputStream out) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Determina el tipo MIME de un archivo basado en su extensión.
     *
     * @param filePath Ruta del archivo solicitado
     * @return String con el tipo MIME correspondiente
     */
    private static String getMimeType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}