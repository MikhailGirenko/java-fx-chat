package ru.gb.javafxchat.server;

import ru.gb.javafxchat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private AuthService authService;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.authService=authService;
            this.server = server;
            this.socket = socket;
            this.in=new DataInputStream(socket.getInputStream());
            this.out=new DataOutputStream(socket.getOutputStream());
            new Thread(()->{
                try {
                    authenticate();
                    readMessages();
                }finally {
                    closeConnection();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        while (true){
            try {
                String message = in.readUTF();
                if(Command.isCommand(message)){
                    Command command = Command.getCommand(message);
                    if(command==Command.AUTH){
                        String[] params = command.parse(message);
                        String login = params[0];
                        String password=params[1];
                        String nick = authService.getNickByLoginAndPassword(login, password);
                    if(nick!=null){
                        if(server.isNickBusy(nick)){
                            sendMessage(Command.ERROR,"Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage(Command.AUTHOK, nick);
                        this.nick = nick;
                        server.broadcast("Пользователь "+nick+" зашел в чат.");
                        server.subscribe(this);
                        break;
                    }else {
                        sendMessage(Command.ERROR, "Неверный логин и пароль");
                    }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    private void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    private void closeConnection() {
        sendMessage(Command.END);

        if(in!=null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(out!=null){
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(socket!=null){
            server.unsubsribe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        while (true){
            try {
                String message = in.readUTF();
                if(message.startsWith("/w")){
                    server.userMessage(nick, message);
                }else {
                    server.broadcast(nick + ": " + message);
                }

                if ("/end".equals(message)) {
                        break;
                    }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getNick() {
        return nick;
    }
}
