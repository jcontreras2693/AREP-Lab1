package org.example;

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
                outputLine = """
                        HTTP/1.1 200 OKc\r
                        Content-Type: text/html\r
                        \r
                        <!DOCTYPE html>
                        <html>
                            <head>
                                <title>Form Example</title>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            </head>
                            <body>
                                <h1>Form with GET</h1>
                                <form action="/app/hello">
                                    <label for="name">Name:</label><br>
                                    <input type="text" id="name" name="name" value="John"><br><br>
                                    <input type="button" value="Submit" onclick="loadGetMsg()">
                                </form>\s
                                <div id="getrespmsg"></div>

                                <script>
                                    function loadGetMsg() {
                                        let nameVar = document.getElementById("name").value;
                                        const xhttp = new XMLHttpRequest();
                                        xhttp.onload = function() {
                                            document.getElementById("getrespmsg").innerHTML =
                                            this.responseText;
                                        }
                                        xhttp.open("GET", "/app/hello?name="+nameVar);
                                        xhttp.send();
                                    }
                                </script>

                                <h1>Form with POST</h1>
                                <form action="/app/hello">
                                    <label for="postname">Name:</label><br>
                                    <input type="text" id="postname" name="name" value="John"><br><br>
                                    <input type="button" value="Submit" onclick="loadPostMsg(postname)">
                                </form>
                               \s
                                <div id="postrespmsg"></div>
                               \s
                                <script>
                                    function loadPostMsg(name){
                                        let url = "/app/hello?name=" + name.value;

                                        fetch (url, {method: 'POST'})
                                            .then(x => x.text())
                                            .then(y => document.getElementById("postrespmsg").innerHTML = y);
                                    }
                                </script>
                            </body>
                        </html>""";
                out.println(outputLine);
            }

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static String helloRestService(String path, String query) {
        String response = """
                HTTP/1.1 200 OKc\r
                Content-Type: application/json\r
                \r
                {"name":"John", "age":30, "car":null}""";
        return response;
    }
}