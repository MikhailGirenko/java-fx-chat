package ru.gb.javafxchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void run(){
        try(ServerSocket serverSocket = new ServerSocket(8189);
            AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Ожидается подключение...");
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключился");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void broadcast(String message){
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
    public void userMessage(String had, String message){
        String[] split = message.split("\\p{Blank}+");
        String nick = split[1];
        for (ClientHandler client : clients) {
            if(nick.equals(client.getNick())){
                client.sendMessage("Лично от "+had+": "+message.replace("/w " + nick,""));
            } else if(had.equals(client.getNick())){
                client.sendMessage("Личное сообщение "+had+": "+message.replace("/w "+nick, ""));
            }
        }
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if(nick.equals(client.getNick())){
                return true;
            }
        }
        return false;
    }

    public void unsubsribe(ClientHandler client) {
        clients.remove(client);
    }
}