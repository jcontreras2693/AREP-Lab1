package org.example.classwork;

import java.net.MalformedURLException;
import java.net.URL;

public class URLParser {
    public static void main(String[] args) throws MalformedURLException {
        URL myUrl = new URL("https://www.escuelaing.edu.co/es/programas/ingenieria-de-sistemas/#scrolling-content-plan_de_estudios-6");

        System.out.println("Protocol: " + myUrl.getProtocol());
        System.out.println("Authority: " + myUrl.getAuthority());
        System.out.println("Host: " + myUrl.getHost());
        System.out.println("Port: " + myUrl.getPort());
        System.out.println("Path: " + myUrl.getPath());
        System.out.println("Query: " + myUrl.getQuery());
        System.out.println("File: " + myUrl.getFile());
        System.out.println("Ref: " + myUrl.getRef());

    }
}