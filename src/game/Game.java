package game;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import GUI.BattlefieldController;
import Main.*;
import Database.*;
import audio.SFXPlayer;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;

public class Game {
    private ArrayList<Creature> creatures = new ArrayList<>();
    public game.Character playerCharacter;
    private Database db = Main.db;
    private int turn = 0;
    private int amountOfLevels = Main.db.fetchAmountOfLevels();
    public Level level;

    public Game(){
        level = new Level(1, 16, "Forest-map.png");
        level.updateLevel();

        if (Main.user.isHost()){
            this.addNewMonstersToLobby(1);
        }
        creatures = db.fetchCreaturesFromLobby();
        for (int i = 0; i < this.creatures.size(); i++){
            if (this.creatures.get(i).getPlayerId() == Main.user.getPlayerId()){
                playerCharacter = (game.Character) this.creatures.get(i);
            }
        }
    }

    public void update(){
        if (this.isPlayerTurn()){
            pushCreatureData();
        }
        else {
            updateCreatureData();
        }
        handleCreatureData();
        if(playerCharacter.getHp() <= 0 && !playerCharacter.isDead()){
            db.addChatMessage(Main.user.getUsername() + " died", true);
        }
        if (Main.user.isHost()) {
            monsterAction();
        }
    }

    public void newLevel(){
        if (Main.user.isHost()){
            pushNewLevel();
        }
        changeToNewLevel();

        if (this.level.getLevelId() == this.amountOfLevels){
            //Update rank for user
        }
    }

    public void resetTurn(){
        if (Main.user.isHost()){
            Main.db.incrementPlayerTurn(0);
        }
        turn = 0;
        //turn = turn%this.getCharacters().size();
    }

    public void pushNewLevel(){
        addNewMonstersToLobby(this.level.getLevelId() + 1);
        Main.db.setLevel(Main.user.getLobbyKey(), this.level.getLevelId() + 1);
    }

    public void addNewMonstersToLobby(int levelId){
        ArrayList<Integer> creatureIds = Main.db.fetchMonstersFromLevel(levelId);
        for (int i = 0; i < creatureIds.size(); i++){
            Main.db.createPlayer(creatureIds.get(i), false);
        }
    }


    public void updateCreatureData(){
        turn = db.fetchPlayerTurn();
        for (int i = 0; i < creatures.size(); i++) {
            Creature c = creatures.get(i);
            int playerId = c.getPlayerId();
            ArrayList<Integer> newPos = db.fetchPlayerPos(playerId);
            int newHp = db.fetchPlayerHp(playerId);
            if (playerId != Main.user.getPlayerId()) {
                c.setNewPos(newPos.get(0), newPos.get(1));
            }
            c.setHp(newHp);
        }
    }

    public void pushCreatureData(){
        for (Creature c : creatures) {
            int playerId = c.getPlayerId();
            int posX = c.getxPos();
            int posY = c.getyPos();
            db.setPos(posX, posY, playerId);
            int hp = c.getHp();
            db.setHp(hp, playerId);
        }
    }

    public void handleCreatureData(){
        for (Creature c : this.creatures) {
            c.updateDead();
            if (c.isDead()) {
                c.setPawnImage("gravestone.png");
            }
        }
    }

    public ArrayList<Integer> getMonstersIndex(){
        ArrayList<Integer> monstersIndex = new ArrayList<>();
        for (int i = 0; i < this.creatures.size(); i++){
            if (this.creatures.get(i) instanceof Monster && !(this.creatures.get(i).isDead())){
                monstersIndex.add(i);
            }
        }
        return monstersIndex;
    }

    public Creature getCreature(int index){
        return this.creatures.get(index);
    }

    public Creature getYourCreature(){
        for(Creature i: creatures){
            if(i.getPlayerId() == Main.user.getPlayerId()){
                return i;
            }
        }
        return null;
    }

    public int getAmountOfCreatures(){
        return this.creatures.size();
    }

    public ArrayList<Creature> getCreatures(){
        return this.creatures;
    }

    public ArrayList<Character> getCharacters(){
        ArrayList<Character> characters= new ArrayList<>();
        for(Creature i: creatures){
            if(i instanceof Character){
                characters.add((Character) i);
            }
        }
        return characters;
    }

    public ArrayList<Integer> getPos(int index){
        ArrayList<Integer> pos = new ArrayList<>();
        pos.add(this.creatures.get(index).getxPos());
        pos.add(this.creatures.get(index).getyPos());
        return pos;
    }

    public boolean isPlayerTurn(){
        if(creatures.get(turn % creatures.size()).getPlayerId() == Main.user.getPlayerId()){
            return true;
        }
        return false;
    }

    public boolean isMonsterTurn(){
        if(creatures.get(turn % creatures.size()) instanceof Monster){
            return true;
        }
        return false;
    }

    public void updatePlayersReadyForNewLevel(){
        ArrayList<Boolean> playersReadyForNewLevel = Main.db.fetchPlayersReadyForLevel();
        for (int i = 0; i < playersReadyForNewLevel.size(); i++){
            this.getCharacters().get(i).setReadyForNewLevel(playersReadyForNewLevel.get(i));
        }
    }

    public boolean playersReadyForNewLevel(){
        boolean ready = true;
        ArrayList<Character> characters = this.getCharacters();
        for (Character c : characters){
            if (!c.isReadyForNewLevel()){
                ready = false;
            }
        }
        return ready;
    }

    public void setAllPlayersReadyForNewLevel(boolean ready){
        ArrayList<Character> characters = this.getCharacters();
        for (Character c : characters){
            Main.db.setReadyForNewLevel(c.getPlayerId(), ready);
        }
    }

    public boolean isPositionAvailable(int x, int y){
        boolean available = true;
        for (Creature c : this.creatures){
            if (c.getxPos() == x && c.getyPos() == y){
                available = false;
            }
        }
        return available;
    }

    public Creature getCreatureFromPosition(int x, int y){
        for (Creature c : this.creatures){
            if (c.getxPos() == x && c.getyPos() == y){
                return c;
            }
        }
        return null;
    }

    public ArrayList<Monster> getMonsters(){
        ArrayList<Monster> monsters = new ArrayList<>();
        for (Creature c : this.creatures){
            if (c instanceof Monster){
                monsters.add((Monster)c);
            }
        }
        return monsters;
    }

    public boolean isLevelCleared(){
        boolean cleared = false;
        boolean allMonstersDead = true;
        ArrayList<Monster> monsters = this.getMonsters();
        for (Monster m : monsters){
            if (!m.isDead()){
                allMonstersDead = false;
            }
        }
        cleared = allMonstersDead;
        return cleared;
    }

    public void changeToNewLevel(){
        this.level.setLevelId(this.level.getLevelId() + 1);
        this.level.updateLevel();

        GridPane mapGrid = (GridPane)this.creatures.get(0).getPawn().getParent();
        for (Creature c : this.creatures){
            mapGrid.getChildren().remove(c.getPawn());
        }
        this.creatures = Main.db.fetchCreaturesFromLobby();
        for (Creature c : this.creatures){
            if (c.getPlayerId() == Main.user.getPlayerId()){
                this.playerCharacter = (game.Character)c;
            }
            c.setPawnSize(mapGrid.getPrefWidth()/16, mapGrid.getPrefHeight()/16);
            mapGrid.add(c.getPawn(), c.getxPos(), c.getyPos());
        }
    }

    public void monsterAction() {
        if (isMonsterTurn()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Monster monster = ((Monster) creatures.get(turn % creatures.size()));
                    if (monster.isDead()) {
                        endTurn();
                    } else {
                        monster.monsterMove(creatures);
                        monster.monsterAttack(creatures);
                        pushCreatureData();
                        endTurn();
                    }
                }
            }).start();
        }
    }

    public void endTurn(){
        turn++;
        db.incrementPlayerTurn(turn);
    }

    public Level getLevel(){
        return this.level;
    }

    public int getAmountOfLevels() {
        return this.amountOfLevels;
    }

    public boolean attackRange(Monster monster, boolean melee){
        /*if(melee && Math.abs(playerCharacter.getxPos() - monster.getxPos()) == 1 && Math.abs(playerCharacter.getyPos() - monster.getyPos()) == 1){
            return true;
        }else if(!melee && Math.abs(playerCharacter.getxPos() - monster.getxPos()) > 1 || Math.abs(playerCharacter.getyPos() - monster.getyPos()) > 1){
            return true;
        }else{
            return false;
        }*/

        if(melee && (Math.abs(playerCharacter.getxPos() - monster.getxPos()) <= 1) && (Math.abs(playerCharacter.getyPos() - monster.getyPos()) <= 1)){
            System.out.println("\nx distance: " + (Math.abs(playerCharacter.getxPos() - monster.getxPos()) <= 1) + "\n");
            System.out.println("\ny distance: " + (Math.abs(playerCharacter.getyPos() - monster.getyPos()) <= 1) + "\n");
            return true;
        }
        else if(!melee && (Math.abs(playerCharacter.getxPos() - monster.getxPos()) > 1) || (Math.abs(playerCharacter.getyPos() - monster.getyPos())) > 1){
            return true;
        }
        else{
            return false;
        }
    }
}
