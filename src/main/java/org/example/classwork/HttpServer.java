package org.example.classwork;

import java.io.*;
import java.net.*;

public class HttpServer {
    public static void main(String[] args) throws IOException {
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
            String filePath = requestLine[1].equals("/") ? "/index.html" : requestLine[1];

            if (method.equals("GET")) {
                handleGetRequest(filePath, out);
            } else if (method.equals("POST")) {
                handlePostRequest(in, out, filePath);
            }

            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleGetRequest(String filePath, OutputStream out) throws IOException {
        File requestedFile = new File("src/main/resources/web" + filePath);

        if (requestedFile.exists() && !requestedFile.isDirectory()) {
            String contentType = getMimeType(filePath);

            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + requestedFile.length() + "\r\n" +
                    "\r\n").getBytes());

            sendFileContent(requestedFile, out);
        } else {
            String errorResponse = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    "<h1>404 Not Found</h1>";
            out.write(errorResponse.getBytes());
        }
    }

    private static void handlePostRequest(BufferedReader in, OutputStream out, String filePath) throws IOException {
        StringBuilder body = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null && !inputLine.isEmpty()) {
            System.out.println("Header: " + inputLine);
        }

        while ((inputLine = in.readLine()) != null) {
            body.append(inputLine).append("\n");
        }

        System.out.println("POST request body: " + body.toString());

        String responseBody = "<h1>POST Request Received</h1>" +
                "<p>Body Content: " + body.toString() + "</p>";

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Length: " + responseBody.length() + "\r\n" +
                "\r\n" +
                responseBody;
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
