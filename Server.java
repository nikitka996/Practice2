package com.company;

import java.net.*;
import java.io.*;
import java.lang.Thread;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    int portNumber;
    int count;
    Map<Integer, Socket> clients;
    Map<Integer, BufferedReader> inputData;
    Map<Integer, PrintWriter> outputData;
    Queue<Task> tasks;
    final int tasksThreadsCount = 5;

    public Server() {
        this.portNumber = 8000;
        count = 0;
        clients = new ConcurrentHashMap<>();
        inputData = new ConcurrentHashMap<>();
        outputData = new ConcurrentHashMap<>();
        tasks = new ConcurrentLinkedQueue<>();

        Thread clientsChecker = new Thread(new ClientsChecker(inputData,
                tasks));

        clientsChecker.start();
        List<Thread> tasksThreads = new ArrayList<Thread>();
        for (int i = 0; i < tasksThreadsCount; i++) {
            tasksThreads.add(new Thread(new Request(this,
                    outputData,
                    tasks)));
        }
        for (Thread t : tasksThreads) {
            t.start();
        }

        System.out.println("Server has started.");

        try (ServerSocket serverSocket =
                     new ServerSocket(this.portNumber)) {
            while(true) {
                Socket clientSocket = serverSocket.accept();
                count++;
                System.out.println("New client has joined. Total threads : " + count);
                clients.put(count, clientSocket);
                try {
                    inputData.put(count, new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream())));
                    outputData.put(count, new PrintWriter(
                            clientSocket.getOutputStream(), true));


                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static String response(String request) {
        try {
            String firstNum = "";
            String secondNum = "";
            char sign = '+';
            String operations = "+-*/";

            for (int i = 0; i < request.length(); i++) {
                if (operations.indexOf(request.charAt(i)) == -1) {
                    if (request.charAt(i) != ' ')
                        firstNum += request.charAt(i);
                }
                else {
                    sign = request.charAt(i);
                    for (int j  = i+1; j < request.length(); j++) {
                        if (request.charAt(j) != ' ')
                            secondNum += request.charAt(j);
                    }
                    break;
                }
            }

            int a = Integer.parseInt(firstNum);
            int b = Integer.parseInt(secondNum);
            int res = 0;

            switch(sign){
                case '+':
                    res = a + b;
                    break;
                case '-':
                    res = a - b;
                    break;
                case '*':
                    res = a * b;
                    break;
                case '/':
                    res = a / b;
                    break;
            }

            return Integer.toString(res);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return "Wrong expression. Use two numbers and one sign!";
        }
    }

    static class Task {
        int id;
        String request;
        public Task(int id, String request) {
            this.id = id;
            this.request = request;
        }
        public int getId() {
            return id;
        }
        public String getRequest() {
            return request;
        }
    }

    static class ClientsChecker implements Runnable {
        Map<Integer, BufferedReader> inputData;
        Queue<Task> tasks;
        public ClientsChecker(Map<Integer, BufferedReader> clientsInputs,
                              Queue<Task> tasks) {
            this.inputData = clientsInputs;
            this.tasks = tasks;
        }

        public void run() {
            while(true) {
                for (Map.Entry<Integer, BufferedReader> c
                        : inputData.entrySet()) {
                    try {
                        if (c.getValue().ready()) {
                            tasks.add(new Task(c.getKey(),
                                    c.getValue().readLine()));
                        }
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

    static class Request implements Runnable {
        Server server;
        Map<Integer, PrintWriter> clientsOutputs;
        Queue<Task> tasks;

        public Request(Server server,
                       Map<Integer, PrintWriter> clientsOutputs,
                       Queue<Task> tasks) {
            this.server = server;
            this.clientsOutputs = clientsOutputs;
            this.tasks = tasks;
        }

        public void run() {
            while(true) {
                Task t = tasks.poll();
                if (t != null) {
                    try {
                        clientsOutputs.get(t.getId())
                                .println(server.response(t.getRequest()));
                    }
                    catch(Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
        }

    public static void main(String[] args){
        new Server();
    }
}