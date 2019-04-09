package GUI;


import main.*;
import audio.MusicPlayer;
import audio.SFXPlayer;
import game.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.*;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;


import javafx.event.EventHandler;
import javafx.scene.text.Font;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;


public class BattlefieldController{

    // https://stackoverflow.com/questions/41081905/javafx-getting-the-location-of-a-click-on-a-gridpane
    @FXML
    private Button exitButton, moveButton, attackButton, endTurnButton;
    @FXML
    private GridPane mapGrid;
    @FXML
    private AnchorPane battlefieldUI;
    @FXML
    private Pane mapContainer;
    @FXML
    private ImageView weaponOne, weaponTwo;
    @FXML
    private ImageView playerImage;
    @FXML
    private Label hpLabel, acLabel, weaponOneLabel, weaponTwoLabel;

    private SceneSwitcher sceneSwitcher = new SceneSwitcher();

    private double mouseX;
    private double mouseY;
    public double cellWidth;
    public double cellHeight;
    private boolean transitioningToNewLevel;

    private Pane movementPane;
    private VBox transitionVbox;

    public static Game game;
    private Player player;
    private Lighting light = new Lighting();
    private InnerShadow shadow = new InnerShadow();


    public static Timer timer = new Timer();


    public BattlefieldController(){
        //New instance of Game

        game = new Game();

    }

    public void initialize() {
        player = new Player(playerImage);
        cellWidth = mapGrid.getPrefWidth()/(16.0);
        cellHeight = mapGrid.getPrefHeight()/(16.0);

        for (Creature c : game.getCreatures()){
            c.setPawnSize(cellWidth, cellHeight);
            mapGrid.add(c.getPawn(), c.getxPos(), c.getyPos());
            if (c instanceof Monster){
                ((Monster) c).initAttackPane(cellWidth, cellHeight);
                mapGrid.add(((Monster) c).getAttackPane(), c.getxPos(), c.getyPos());
            }
        }

        weaponOne.setImage(new Image("GUI/images/" + game.getPlayerCharacter().getWeapons().get(0).getImageUrl()));
        weaponTwo.setImage(new Image("GUI/images/" + game.getPlayerCharacter().getWeapons().get(1).getImageUrl()));
        weaponOne.setEffect(light);
        weaponTwo.setEffect(shadow);
        player.setEquippedWeapon(0);
        Weapon weapon1 = game.getPlayerCharacter().getWeapons().get(0);
        Weapon weapon2 = game.getPlayerCharacter().getWeapons().get(1);
        weaponOneLabel.setText(weapon1.getName() + "\n" + "Avg. damage: " + (((((double)weapon1.getDamageDice()/2)+0.5)*weapon1.getDiceAmount()) + game.getPlayerCharacter().getDamageBonus()) + "\n" + (weapon1.isRanged() ? "Ranged" : "Melee"));
        weaponTwoLabel.setText(weapon2.getName() + "\n" + "Avg. damage: " + (((((double)weapon2.getDamageDice()/2)+0.5)*weapon2.getDiceAmount()) + game.getPlayerCharacter().getDamageBonus()) + "\n" + (weapon2.isRanged() ? "Ranged" : "Melee"));

        mapContainer.getChildren().add(game.getLevel().backgroundImage);
        game.getLevel().backgroundImage.toBack();

        acLabel.setText("AC: " + game.getPlayerCharacter().getAc());

        initLevelTransitionVbox();
        initMovementPane();

        refreshViewFromGame();
        updateGame();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    update();
                    TeamMatesController.updateListView();
                });
            }
        },0 ,1200);
    }

    public void attackButtonPressed(){
        player.setAttackPressed(!player.isAttackPressed());
        if (player.isAttackPressed()) {
            moveButton.setDisable(true);
            endTurnButton.setDisable(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (Monster m : game.getMonsters()) {
                        if (!m.isDead()) {
                            if (m.getAttackPane() == null){
                                m.initAttackPane(cellWidth, cellHeight);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        mapGrid.add(m.getAttackPane(), m.getxPos(), m.getyPos());
                                    }
                                });
                            }
                            if(game.getPlayerCharacter().getWeapons().get(player.getEquippedWeapon()).isRanged()){
                                if(game.attackRange(m, false)) {
                                    m.showAttackPane();
                                    m.getAttackPane().addEventFilter(MouseEvent.MOUSE_CLICKED, attackEventHandler);
                                }
                            }else if(!game.getPlayerCharacter().getWeapons().get(player.getEquippedWeapon()).isRanged()){
                                if(game.attackRange(m, true)) {
                                    m.showAttackPane();
                                    m.getAttackPane().addEventFilter(MouseEvent.MOUSE_CLICKED, attackEventHandler);
                                }
                            }
                        }
                    }
                }
            }).start();
            attackButton.getStyleClass().add("attack-button-selected");
        }
        else{
            attackFinished();
        }
    }

    private EventHandler<MouseEvent> attackEventHandler = new EventHandler<MouseEvent>(){
        @Override
        public void handle(MouseEvent e){
            Monster clickedMonster = null;
            for (Monster m : game.getMonsters()){
                if (e.getSource() == m.getAttackPane()){
                    clickedMonster = m;
                }
            }
            game.getPlayerCharacter().attackCreature(clickedMonster, player.getEquippedWeapon());
            player.setAttackUsed(true);
            attackFinished();
        }
    };

    public void attackFinished(){
        updateGame();
        player.setAttackPressed(false);
        moveButton.setDisable(false);
        endTurnButton.setDisable(false);
        attackButton.getStyleClass().clear();
        attackButton.getStyleClass().add("button");

        for (Monster m : game.getMonsters()){
            m.hideAttackPane();
            m.getAttackPane().removeEventFilter(MouseEvent.MOUSE_CLICKED, attackEventHandler);
        }
        refreshViewFromGame();
        checkForPlayerTurn();
    }

    public void moveButtonPressed(){
        for(Creature i: game.getCreatures()){
            System.out.println("\n" + i.getCreatureName() + "\nxpos: " + i.getxPos() + "\nypos: " + i.getyPos() + "\n");
        }
        player.setMovePressed(!player.isMovePressed());
        if (player.isMovePressed()) {
            attackButton.setDisable(true);
            endTurnButton.setDisable(true);
            moveButton.getStyleClass().add("button-selected");
            showMovementPane();
            openMapMoveEventHandler();
        }
        else{
            moveFinished();
        }
    }

    private EventHandler<MouseEvent> mapMoveEventHandler = new EventHandler<MouseEvent>(){
        @Override
        public void handle(MouseEvent e){
            mouseX = e.getX();
            mouseY = e.getY();
            moveFinished();
        }
    };

    public void moveFinished(){
        for(Creature i: game.getCreatures()){
            System.out.println("\n" + i.getCreatureName() + "\nxpos: " + i.getxPos() + "\nypos: " + i.getyPos() + "\n");
        }
        if (game.getPlayerCharacter().moveCreature(toGrid(mapGrid.getWidth(), mouseX), toGrid(mapGrid.getHeight(), mouseY), game.getCreatures())){
            player.setMoveUsed(true);
        }
        updateGame();
        player.setMovePressed(false);
        hideMovementPane();
        closeMapMoveEventHandler();
        attackButton.setDisable(false);
        endTurnButton.setDisable(false);
        moveButton.getStyleClass().clear();
        moveButton.getStyleClass().add("button");
        refreshViewFromGame();
        checkForPlayerTurn();
    }

    public void endTurnButtonPressed(){
        player.resetUsedActions();
        game.endTurn();
    }

    public void helpButtonPressed(){
        if (Desktop.isDesktopSupported()){
            try {
                Desktop.getDesktop().browse(new URI("https://gitlab.stud.iie.ntnu.no/heleneyj/game-development-project/wikis/System%20Documentation"));
            }
            catch (IOException ioe){
                System.out.println("Error with IO");
                ioe.printStackTrace();
            }
            catch (URISyntaxException e){
                System.out.println("Error in URL");
                e.printStackTrace();
            }
        }
    }

    public void exitButtonPressed() throws Exception {
        System.out.println(Main.db.addChatMessage(Main.user.getUsername() + " has left the lobby", true));
        Main.db.disconnectUserFromGameLobby();
        chatController.timer.cancel();
        chatController.timer.purge();
        timer.cancel();
        timer.purge();
        MusicPlayer.getInstance().stopSong();
        this.sceneSwitcher.switchScene(exitButton, "MainMenu.fxml");
    }

    private int toGrid(double pixels, double pos){
        double dPos = pos / (pixels / 16);
        int iPos = (int) (dPos);
        return iPos;
    }

    public void openMapMoveEventHandler(){
        mapGrid.setCursor(Cursor.HAND);
        mapGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, mapMoveEventHandler);
    }

    public void closeMapMoveEventHandler(){
        mapGrid.setCursor(Cursor.DEFAULT);
        mapGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, mapMoveEventHandler);
    }

    public void updateGame(){
        game.update();
    }

    public void refreshViewFromGame(){
        for(int i = 0; i < game.getCreatures().size(); i++){
            Creature c = game.getCreatures().get(i);
            if (GridPane.getColumnIndex(c.getPawn()) != c.getxPos() || GridPane.getRowIndex(c.getPawn()) != c.getyPos()) {
                mapGrid.getChildren().remove(c.getPawn());
                mapGrid.add(c.getPawn(), c.getxPos(), c.getyPos());
                if (c instanceof Monster){
                    ((Monster) c).updateAttackPane();
                }
            }
        }
        Weapon weapon1 = game.getPlayerCharacter().getWeapons().get(0);
        Weapon weapon2 = game.getPlayerCharacter().getWeapons().get(1);
        weaponOneLabel.setText(weapon1.getName() + "\n" + "Avg. damage: " + (((((double)weapon1.getDamageDice()/2)+0.5)*weapon1.getDiceAmount()) + game.getPlayerCharacter().getDamageBonus()) + "\n" + (weapon1.isRanged() ? "Ranged" : "Melee"));
        weaponTwoLabel.setText(weapon2.getName() + "\n" + "Avg. damage: " + (((((double)weapon2.getDamageDice()/2)+0.5)*weapon2.getDiceAmount()) + game.getPlayerCharacter().getDamageBonus()) + "\n" + (weapon2.isRanged() ? "Ranged" : "Melee"));

        hpLabel.setText("HP: " + Math.max(0, game.getPlayerCharacter().getHp()) + "/" + game.getPlayerCharacter().getInitialHp());
        updateImage();
    }


    public boolean update(){
        if (game.isLevelCleared()) {
            if (!game.getPlayerCharacter().isReadyForNewLevel()){
                if (Main.user.isHost()){
                    game.pushNewLevel();
                }
                game.setPlayerReadyForNewLevel(true);
            }
            while(!game.allPlayersReadyForNewLevel()){
                System.out.println("Hello");
                try{
                    Thread.sleep(200);
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            }
            nextLevelTransition();
            return false;
        }
        else if(game.isGameOver()){
            gameOverTransition();
            return false;
        }
        updateGame();
        refreshViewFromGame();
        checkForPlayerTurn();
        System.out.println(game.toString());
        return true;
    }

    public void newLevel(){
        //game.getPlayerCharacter().setHp(game.getPlayerCharacter().getInitialHp());
        game.changeToNewLevel();
        player.resetUsedActions();
    }

    public boolean checkForPlayerTurn(){
        if(!game.isPlayerCharacterTurn()){
            moveButton.setDisable(true);
            attackButton.setDisable(true);
            endTurnButton.setDisable(true);
            return false;
        }
        else{
            if (game.getPlayerCharacter().isDead() || player.isAllActionsUsed()){
                game.endTurn();
                player.resetUsedActions();
                return false;
            }
            if (!player.isMovePressed()){
                attackButton.setDisable(false);
            }
            if (!player.isAttackPressed()){
                    moveButton.setDisable(false);
            }
            if (!player.isMovePressed() && !player.isAttackPressed()){
                endTurnButton.setDisable(false);
            }
            if (player.isAttackUsed()){
                attackButton.setDisable(true);
            }
            if (player.isMoveUsed()){
                moveButton.setDisable(true);
            }
            return true;
        }
    }

    public void initMovementPane(){
        movementPane = new Pane();
        double moveDistanceX = cellWidth*(2*game.getPlayerCharacter().getMovement() + 1);
        double moveDistanceY = cellHeight*(2*game.getPlayerCharacter().getMovement() + 1);
        movementPane.setPrefWidth(moveDistanceX);
        movementPane.setPrefHeight(moveDistanceY);
        movementPane.setMouseTransparent(true);
        movementPane.setStyle("-fx-background-color: rgb(26, 188, 156, 0.3)");
        movementPane.setVisible(false);
    }


    public void showMovementPane(){
        int xpos = game.getPlayerCharacter().getxPos();
        int ypos = game.getPlayerCharacter().getyPos();
        int movement = game.getPlayerCharacter().getMovement();
        mapGrid.getChildren().remove(movementPane);
        mapGrid.add(movementPane, xpos - movement + ((xpos < movement) ? movement-xpos : 0), ypos - movement + ((ypos < movement) ? movement-ypos : 0));
        movementPane.toBack();
        movementPane.setVisible(true);
        GridPane.setColumnSpan(movementPane, 2*(game.getPlayerCharacter().getMovement()) + 1 - ((xpos < movement) ? movement-xpos : 0));
        GridPane.setRowSpan(movementPane, 2*(game.getPlayerCharacter().getMovement()) + 1 - ((ypos < movement) ? movement-ypos : 0));
    }

    public void hideMovementPane(){
        movementPane.setVisible(false);
    }

    public void weaponOneSelected(){
        weaponOne.setEffect(light);
        weaponTwo.setEffect(shadow);
        player.setEquippedWeapon(0);
    }

    public void weaponTwoSelected(){
        weaponOne.setEffect(shadow);
        weaponTwo.setEffect(light);
        player.setEquippedWeapon(1);
    }

    public void updateImage() {
        player.imageUpdate();
    }

    public void initLevelTransitionVbox(){
        transitionVbox = new VBox();
        VBox vbox = transitionVbox;
        vbox.setPrefWidth(mapGrid.getPrefWidth());
        vbox.setPrefHeight(mapGrid.getPrefHeight());
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(40);
        vbox.setStyle("-fx-background-color:  #1c1c1c80");

        Label label1 = new Label("Level cleared!");
        label1.setTextFill(Color.web("f8d256"));
        label1.setFont(new Font(60));
        label1.setStyle("-fx-font-weight: bold");

        Label label2 = new Label();
        label2.setFont(new Font(30));
        label2.setTextFill(Color.WHITE);
        vbox.getChildren().addAll(label1, label2);
        vbox.setVisible(false);
        battlefieldUI.getChildren().add(vbox);
    }

    public void showLevelTransitionVbox(){
        String nextLevelName = Main.db.getLevelName(game.getLevel().getLevelId() + 1);
        if (nextLevelName != null && !game.isGameOver()){
            SFXPlayer.getInstance().setSFX(13);
            ((Label)transitionVbox.getChildren().get(1)).setText("Travelling to " + nextLevelName + "world");
        }
        else if(game.isGameOver()){
            SFXPlayer.getInstance().setSFX(14);
            ((Label)transitionVbox.getChildren().get(0)).setText("Defeat");
            ((Label)transitionVbox.getChildren().get(1)).setText("Better luck next time");
        }
        else{
            SFXPlayer.getInstance().setSFX(16);
            ((Label)transitionVbox.getChildren().get(0)).setText("Victory!");
            ((Label)transitionVbox.getChildren().get(1)).setAlignment(Pos.CENTER);
            ((Label)transitionVbox.getChildren().get(1)).setText("Made by:\nTeam 3");
            Main.db.setRank(Main.db.fetchRank(Main.user.getUser_id()) + 1);
        }
        transitionVbox.setVisible(true);
        mapGrid.setGridLinesVisible(false);
        this.disableAllButtons();
    }

    public void hideLevelTransitionVbox(){
        transitionVbox.setVisible(false);
        mapGrid.setGridLinesVisible(true);
        this.enableAllButtons();
    }

    public void enableAllButtons(){
        attackButton.setDisable(false);
        moveButton.setDisable(false);
        endTurnButton.setDisable(false);
        exitButton.setDisable(false);
    }

    public void disableAllButtons(){
        attackButton.setDisable(true);
        moveButton.setDisable(true);
        endTurnButton.setDisable(true);
        exitButton.setDisable(true);
    }

    public void nextLevelTransition() {
            if (!transitioningToNewLevel) {
                transitioningToNewLevel = true;
                MusicPlayer.getInstance().stopSong();
                MusicPlayer.getInstance().changeSong(1);
                showLevelTransitionVbox();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                newLevel();
                                game.updatePlayerTurn();
                                checkForPlayerTurn();
                                if (game.getLevel().getLevelId() <= game.getAmountOfLevels()) {
                                    hideLevelTransitionVbox();
                                    transitioningToNewLevel = false;
                                }
                                else{
                                    try {
                                        Thread.sleep(7000);
                                        MusicPlayer.getInstance().stopSong();
                                        MusicPlayer.getInstance().changeSong(2);
                                        sceneSwitcher.switchScene(exitButton, "MainMenu.fxml");
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    chatController.timer.cancel();
                                    chatController.timer.purge();
                                    timer.cancel();
                                    timer.purge();
                                    Main.user.setHost(false);
                                }
                            }
                        });
                    }
                }).start();
            }
    }

    public void gameOverTransition(){
        if (!transitioningToNewLevel) {
            transitioningToNewLevel = true;
            SFXPlayer.getInstance().setSFX(3);
            MusicPlayer.getInstance().stopSong();
            MusicPlayer.getInstance().changeSong(1);
            showLevelTransitionVbox();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    chatController.timer.cancel();
                    chatController.timer.purge();
                    timer.cancel();
                    timer.purge();
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MusicPlayer.getInstance().changeSong(2);
                                sceneSwitcher.switchScene(exitButton, "MainMenu.fxml");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                                hideLevelTransitionVbox();
                                transitioningToNewLevel = false;
                        }
                    });
                }
            }).start();
        }
    }
}
