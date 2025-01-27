package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class HttpServerJ {

    public static void main(String[] args) throws IOException{
        while (true){
            ServerSocket serverSocket = new ServerSocket(35000);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String[] dataPage = readInput(in).split(" ");
            String types = dataPage[0];
            String name = dataPage[1];
            System.out.println("nameeeee " + name);
            String outputLine = createOutput(types, name, clientSocket.getOutputStream());
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        }
    }

    private static String createOutput(String dataPage, String name, OutputStream output) throws IOException {
        String outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: " + dataPage + "\r\n"
                + "\r\n";

        File file = new File("src/main/resources/static" + name);

        if (!file.exists()) {
            outputLine = "HTTP/1.1 404 Not Found\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    + "<h1>404 Not Found</h1>";
            return outputLine;
        }

        if (dataPage.contains("image")) {
            output.write(outputLine.getBytes());
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
            return null;
        } else {
            outputLine += readHtmlFile("src/main/resources/static" + name);
            return outputLine;
        }
    }


    private static String readInput(BufferedReader in) throws IOException{
        String inputLine, type = null, name = null;
        boolean isFirstLine = true;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
            if(isFirstLine){
                String[] data = inputLine.split(" ");
                name = data[1];
                System.out.println("name: " + name);
                if(name.split("\\.").length > 1){
                    type = getType(name.split("\\.")[1]);
                }
                isFirstLine = false;
            }
            if (inputLine.isEmpty()) {
                break;
            }
        }
        System.out.println("-.-.-.-type: " + type);
        System.out.println(type);
        if(type == null){
            name = "/index.html";
            type = "html";
        }
        return type + " " + name;
    }

    private static String getType(String type){
        System.out.println("data type: " + type);
        HashMap<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put("png", "image/png");
        mimeTypes.put("html", "text/html");
        mimeTypes.put("js", "application/javascript");
        mimeTypes.put("css", "text/css");
        return mimeTypes.get(type);
    }

    private static String readHtmlFile(String filePath) throws IOException {
        System.out.println("Reading file from path: " + filePath);
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        }
        return contentBuilder.toString();
    }
}