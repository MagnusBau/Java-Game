package GUI;

import Database.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AccountDetailsController {
    @FXML
    private Text username, email, rank;

    private Database db = InterfaceMain.db;

    public void initialize() throws Exception{
        getInfo();
    }

    public void getInfo()throws Exception{
        username.setText(db.user.getUsername());
        email.setText(db.user.getEmail());
        rank.setText(Integer.toString(db.user.getRank()));
    }

    @FXML
    private Button changeUsername, changePassword, back;

    public void changeUsername() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("changeUsername.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)changeUsername.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void changePassword() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("changePassword.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)changePassword.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void menu() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)back.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}