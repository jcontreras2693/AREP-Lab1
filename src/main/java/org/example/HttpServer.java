package org.example;

import java.io.*;
import java.net.*;

public class HttpServer {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            OutputStream out = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;

            boolean isFirstLine = true;
            String filePath = "";

            while ((inputLine = in.readLine()) != null) {
                if (isFirstLine) {
                    filePath = inputLine.split(" ")[1];
                    isFirstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            // Si el archivo solicitado es "/", redirigir a "index.html"
            if (filePath.equals("/")) {
                filePath = "/index.html";
            }

            // Intentar leer el archivo solicitado
            File requestedFile = new File("src/main/resources/web" + filePath);
            if (requestedFile.exists() && !requestedFile.isDirectory()) {
                String contentType = getMimeType(filePath);

                // Enviar encabezados HTTP
                out.write(("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + requestedFile.length() + "\r\n" +
                        "\r\n").getBytes());

                // Enviar contenido del archivo
                sendFileContent(requestedFile, out);
            } else {
                // Responder con un error 404 si el archivo no existe
                String errorResponse = "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "\r\n" +
                        "<h1>404 Not Found</h1>";
                out.write(errorResponse.getBytes());
            }

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static void sendFileContent(File file, OutputStream out) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fileInputStream.close();
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
