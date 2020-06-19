package client_server.client.controllers;

import client_server.client.GlobalContext;
import client_server.domain.Message;
import client_server.domain.Packet;
import client_server.domain.UserCredentials;
import com.google.common.primitives.UnsignedLong;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class LoginWindowController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    public void processLogin() throws IOException {
        System.out.println("Process login");

//        try {

        UserCredentials user = new UserCredentials(loginField.getText(), DigestUtils.md5Hex(passwordField.getText()));
        Message msg = new Message(Message.cTypes.LOGIN.ordinal(), 1, user.toJSON().toString().getBytes(StandardCharsets.UTF_8));

        Packet packet = new Packet((byte)1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);

        Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "Response from server: " + new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8)
                + "\t for user with ID: " + receivedPacket.getSrcId()
                + "\t for packet with ID: " + receivedPacket.getbPktId());

//        FXMLLoader loader = new FXMLLoader();
//        Stage stage = (Stage) loginField.getScene().getWindow();
//        VBox root = loader.load(getClass().getResourceAsStream("/ui/products/list.fxml"));
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//        } catch (final LoginException ex) {
//            messageField.setText(ex.getErrorResponse().getMessage());
//        }
    }

    @FXML
    void initialize() {

    }

}
