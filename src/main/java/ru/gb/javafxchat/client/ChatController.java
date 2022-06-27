package ru.gb.javafxchat.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ru.gb.javafxchat.Command;

import javax.security.auth.login.CredentialNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ChatController {
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextField loginField;
    @FXML
    private HBox authBox;
    @FXML
    private PasswordField passField;
    @FXML
    private HBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;

    private ChatClient client;

    private String selectedNick;

    public ChatController() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                showNotification();
            }
        }
    }

    private void showNotification() {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Не могу подключиться к серверу.\n",
                new ButtonType("Попробовать сново", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Ошибка подключения!");
        Optional<ButtonType> answer = alert.showAndWait();
        Boolean isExit = answer.map(select -> select.getButtonData().isCancelButton()).orElse(false);
        if(isExit){
            System.exit(0);
        }
    }

    public void clickSendButton() {
        String message  = messageField.getText();
        if (message.isBlank()) {
            return;
        }
        if(selectedNick != null){
            client.sendMessage(Command.PRIVATE_MESSAGE,selectedNick, message);
            selectedNick = null;
        }else {
            client.sendMessage(Command.MESSAGE,message);
        }
        messageField.clear();
        messageField.requestFocus();
    }

    public void addMessage(String message) {
        messageArea.appendText(message+"\n");
    }
    public void setAuth(boolean success){
        authBox.setVisible(!success);
        messageBox.setVisible(success);
    }

    public void signinBtnClick() {
        client.sendMessage(Command.AUTH, loginField.getText(), passField.getText());
    }

    public void showError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage, new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Error");
        alert.showAndWait();
    }

    public void selectClient(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            String selectedNick = clientList.getSelectionModel().getSelectedItem();
            if(selectedNick!= null && !selectedNick.isEmpty()){
                this.selectedNick = selectedNick;
            }
        }
    }

    public void updateClientsList(String [] clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);
    }

    public void signOutClick() {
        client.sendMessage(Command.END);
    }

    public ChatClient getClient() {
        return client;
    }
}

