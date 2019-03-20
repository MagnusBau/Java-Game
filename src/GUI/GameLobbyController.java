package GUI;

import Main.*;
import Database.*;
import audio.MusicPlayer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Timer;

public class GameLobbyController {
    @FXML
    private Button travelButton;
    @FXML
    private Button backToMenuButton;
    @FXML
    private Label lobbyKeyLabel;

    private SceneSwitcher sceneSwitcher = new SceneSwitcher();

    private Database db = Main.db;
    private User user = Main.user;

    public void initialize(){
        lobbyKeyLabel.setText("" + user.getLobbyKey());
    }

    public void travelButtonPressed() throws Exception{
        audio.MusicPlayer.getInstance().stopSong();
        MusicPlayer.getInstance().changeSong(7);
        chatController.timer.cancel();
        chatController.timer.purge();
        this.sceneSwitcher.switchScene(travelButton, "Battlefield.fxml");

    }

    public void backToMenuButtonPressed() throws Exception{
        db.disconnectUserFromGameLobby();
        chatController.timer.cancel();
        chatController.timer.purge();
        this.sceneSwitcher.switchScene(backToMenuButton, "MainMenu.fxml");
    }
}
