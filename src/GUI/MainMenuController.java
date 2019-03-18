package GUI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MainMenuController {


    @FXML
    private Button startNewGameButton;

<<<<<<< HEAD
    public void startNewGameButtonPressed() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("createCharacter.fxml"));
=======
    public void buttonPressed1() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("createcharacter.fxml"));
>>>>>>> 926a1fe07c84b9a04d67526f856572fa47582d79
        Scene scene = new Scene(root);
        Stage stage = (Stage)startNewGameButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private Button joinLobbyButton;

    public void joinLobbyButtonPressed() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("FindLobby.fxml"));
        Scene scene = new Scene(root, 800, 500);
        Stage stage = (Stage)joinLobbyButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private Button viewAccountButton;

    public void viewAccountButtonPressed() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("AccountDetails.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)viewAccountButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private Button settingsButton;

    public void settingsButtonPressed() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("settings.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)settingsButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private Button helpButton;

    public void helpButtonPressed() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("https://gitlab.stud.iie.ntnu.no/heleneyj/game-development-project/wikis/System/User-manual"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)helpButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private Button signOutButton;

    public void signOutButtonPressed() throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("start.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage)signOutButton.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}
