import java.net.*;
import java.io.*;

public class HttpServer {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        }
        catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while (running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean isFirstLine = true;
            String file = "";

            while ((inputLine = in.readLine()) != null) {
                if (isFirstLine){
                    file = inputLine.split(" ")[1];
                    isFirstLine = false;
                }
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }

            URI requestedFile = new URI(file);
            System.out.println("file: " + requestedFile);

            if(requestedFile.getPath().startsWith("/app/hello")){
                outputLine = helloRestService(requestedFile.getPath(), requestedFile.getQuery());
                out.println(outputLine);
            } else{
                outputLine = "HTTP/1.1 200 OKc\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head>"
                        + "<meta charset=\"UTF-8\">"
                        + "<title>Title of the document</title>\n"
                        + "</head>"
                        + "<body>"
                        + "My Web Site"
                        + "</body>"
                        + "</html>";
                out.println(outputLine);
            }

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String helloRestService(String path, String query) {
        String response = "HTTP/1.1 200 OKc\r\n"
                + "Content-Type: application/json\r\n"
                + "\r\n"
                + "'{\"name\":\"John\", \"age\":30, \"car\":null}'";
        return response;
    }
}