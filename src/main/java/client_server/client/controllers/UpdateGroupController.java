package client_server.client.controllers;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import client_server.client.GlobalContext;
import client_server.domain.Group;
import client_server.domain.Message;
import client_server.domain.Packet;
import com.google.common.primitives.UnsignedLong;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.json.JSONException;
import org.json.JSONObject;

import static client_server.domain.Message.cTypes.GET_GROUP;
import static client_server.domain.Message.cTypes.UPDATE_GROUP;

public class UpdateGroupController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label idLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField descrField;

    @FXML
    private Label statusLabel;

    @FXML
    void updateGroup(ActionEvent event) {
        if(nameField.getText().isEmpty() || descrField.getText().isEmpty()){
            statusLabel.setText("Fill out all fields before update.");
        }else{
            Group group = new Group(Integer.parseInt(idLabel.getText()), nameField.getText(), descrField.getText());
            Message msg = new Message(Message.cTypes.UPDATE_GROUP.ordinal() , 1, group.toJSON().toString().getBytes(StandardCharsets.UTF_8));

            Packet packet = new Packet((byte) 1, UnsignedLong.valueOf(GlobalContext.packetId++), msg);


            Packet receivedPacket = GlobalContext.clientTCP.sendPacket(packet.toPacket());

            int command = receivedPacket.getBMsq().getcType();
            Message.cTypes[] val = Message.cTypes.values();
            Message.cTypes command_type = val[command];


            if (command_type == UPDATE_GROUP) {
                String message = new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8);
                JSONObject information = new JSONObject(message);
                try {
                    statusLabel.setText(information.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Can't update group.");
            }
        }
    }

    @FXML
    void initialize() {

    }

    public void initData(Group group){
        idLabel.setText(group.getId().toString());
        nameField.setText(group.getName());
        descrField.setText(group.getDescription());
    }
}