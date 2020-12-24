package com.company;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        String hostName = "127.0.0.1";
        int portNumber = 8000;

        try (
                Socket socket = new Socket(hostName, portNumber);

                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner sc = new Scanner(System.in);
        )
        {
            while (true) {
                System.out.print("Input your expression : ");
                String req = sc.nextLine();
                pw.println(req);

                System.out.println("Result = " + in.readLine());
            }
        }
    }
}