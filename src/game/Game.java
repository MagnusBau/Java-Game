package game;

import java.util.ArrayList;
import main.*;
import javafx.scene.layout.GridPane;

/**
 * Game.java
 *
 * This class
 */

public class Game {
    private ArrayList<Creature> creatures = new ArrayList<>();
    private Character playerCharacter;
    private int playerTurn = 0;
    private int amountOfLevels;
    private Level level;

    public Game(){
        if(Main.db != null) {
            amountOfLevels = Main.db.fetchAmountOfLevels();
            level = new Level(1);
            if (Main.user.isHost()) {
                this.addNewMonstersToLobby(1, Main.db.fetchPlayerCount());
                Main.db.setBattlefieldReady(Main.user.getLobbyKey());

            } else {
                while (!Main.db.fetchBattlefieldReady(Main.user.getLobbyKey())) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
            this.creatures = Main.db.fetchCreaturesFromLobby();

            for (int i = 0; i < this.creatures.size(); i++) {
                if (this.creatures.get(i).getPlayerId() == Main.user.getPlayerId()) {
                    playerCharacter = (game.Character) this.creatures.get(i);
                }
            }
        }
    }

    public void update(){
        if (this.isPlayerCharacterTurn()){
            pushCreatureData();
        }
        else {
            updateCreatureData();
        }
        handleCreatureData();
        if (Main.user.isHost()) {
            monsterAction();
        }
    }

    public void resetPlayerTurn(){
        if (Main.user.isHost()){
            Main.db.incrementPlayerTurn(0);
        }
        this.setPlayerTurn(0);
    }

    public void pushNewLevel(){
        int playerAmount = this.getCharacters().size();
        addNewMonstersToLobby(this.level.getLevelId() + 1, playerAmount);
        Main.db.setLevelId(Main.user.getLobbyKey(), this.level.getLevelId() + 1);
        if (this.level.getLevelId() + 1 <= this.getAmountOfLevels()) {
            if (this.level.getLevelId() + 1 == this.getAmountOfLevels()){
                for (int i = 0; i < playerAmount; i++){
                    Main.db.setPos(7 - (int)Math.round(playerAmount/2) + 2*i, 13, this.getCharacters().get(i).getPlayerId());
                }
            }
            else {
                assureNoOverlap();
            }
            upgradePlayerStats();
        }
        this.resetPlayerTurn();
    }

    public void assureNoOverlap(){
        ArrayList<Creature> newCreatures = Main.db.fetchCreaturesFromLobby();
        for (int i = 0; i < newCreatures.size(); i++){
            for (int j = i + 1; j < newCreatures.size(); j++){
                Creature c1 = newCreatures.get(i);
                Creature c2 = newCreatures.get(j);
                if (c1.getxPos() == c2.getxPos() && c1.getyPos() == c2.getyPos()){
                    int newX = (int)Math.floor(Math.random()*16);
                    int newY = (int)Math.floor(Math.random()*16);
                    boolean overlapsAgain = true;
                    while(overlapsAgain){
                        overlapsAgain = false;
                        newX = (int)Math.floor(Math.random()*16);
                        newY = (int)Math.floor(Math.random()*16);
                        for (int k = 0; k < newCreatures.size(); k++){
                            Creature c = newCreatures.get(k);
                            if (newX == c.getxPos() && newY == c.getyPos()){
                                overlapsAgain = true;
                            }
                        }
                    }
                    c1.setNewPos(newX, newY);
                }
            }
        }
        for (Creature c : creatures){
            Main.db.setPos(c.getxPos(), c.getyPos(), c.getPlayerId());
        }
    }

    public void upgradePlayerStats(){
        ArrayList<Character> characters = this.getCharacters();
        for (Character c : characters){
            Main.db.setHp((int)(c.getInitialHp()*1.25), c.getPlayerId());
            Main.db.setDamageBonus((int)(c.getDamageBonus()*1.25), c.getPlayerId());
        }
        Main.db.addChatMessage("You feel the energy from this battle, you are now more powerful!", true);
    }


    public void addNewMonstersToLobby(int levelId, int playerAmount){
        ArrayList<Integer> creatureIds = Main.db.fetchMonstersFromLevel(levelId, playerAmount);
        for (int i = 0; i < creatureIds.size(); i++){
            int playerId = Main.db.createPlayer(false);
            int posX = (int)Math.floor(Math.random()*16);
            int posY = (int)Math.floor(Math.random()*16);
            if (creatureIds.get(i) >= 13){
                posX = 8;
                posY = 7;
            }
            Main.db.createCreature(playerId, creatureIds.get(i), posX, posY);
        }
    }


    public void updateCreatureData(){
        this.updatePlayerTurn();
        for (int i = 0; i < creatures.size(); i++) {
            Creature c = creatures.get(i);
            int playerId = c.getPlayerId();
            ArrayList<Integer> newPos = Main.db.fetchPlayerPos(playerId);
            int newHp = Main.db.fetchPlayerHp(playerId);
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
           Main.db.setPos(posX, posY, playerId);
            int hp = c.getHp();
           Main.db.setHp(hp, playerId);
        }
    }

    public void handleCreatureData(){
        if (this.playerCharacter.getHp() <= 0 && !this.playerCharacter.isDead()){
            Main.db.addChatMessage(Main.user.getUsername() + " died", true);
        }
        for (Creature c : this.creatures) {
            c.updateDead();
        }
    }

    public void updatePlayerTurn(){
        this.setPlayerTurn(Main.db.fetchPlayerTurn());
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

    public boolean isPlayerCharacterTurn(){
        if(creatures.get(playerTurn % creatures.size()).getPlayerId() == Main.user.getPlayerId()){
            return true;
        }
        return false;
    }

    public boolean isMonsterTurn(){
        if(creatures.get(playerTurn % creatures.size()) instanceof Monster){
            return true;
        }
        return false;
    }

    public void updatePlayersReadyForNewLevel(){
        ArrayList<Boolean> playersReadyForNewLevel = Main.db.fetchPlayersReadyForLevel();
        for (int i = 0; i < playersReadyForNewLevel.size(); i++){
            if (this.getCharacters().get(i) != this.playerCharacter) {
                this.getCharacters().get(i).setReadyForNewLevel(playersReadyForNewLevel.get(i));
            }
        }
    }

    public boolean allPlayersReadyForNewLevel(){
        if(Main.db != null) {
            this.updatePlayersReadyForNewLevel();
        }
        boolean ready = true;
        ArrayList<Character> characters = this.getCharacters();
        for (Character c : characters){
            if (!c.isReadyForNewLevel()){
                ready = false;
            }
        }
        return ready;
    }

    public void setPlayerReadyForNewLevel(boolean ready){
        this.playerCharacter.setReadyForNewLevel(ready);
        Main.db.setReadyForNewLevel(this.playerCharacter.getPlayerId(), ready);
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

    public boolean isGameOver(){
        boolean gameOver = false;
        boolean allCharactersDead = true;
        ArrayList<Character> characters = this.getCharacters();
        for (Character i: characters){
            if (!i.isDead()){
                allCharactersDead = false;
            }
        }
        gameOver = allCharactersDead;
        return gameOver;
    }

    public void changeToNewLevel() {
        this.setPlayerReadyForNewLevel(false);
        int LevelId = Main.db.fetchLevelId(Main.user.getLobbyKey());
        this.level.setLevelId(LevelId);
        this.level.updateLevel();
        GridPane mapGrid = (GridPane) this.creatures.get(0).getPawn().getParent();
        for (Creature c : this.creatures) {
            mapGrid.getChildren().remove(c.getPawn());
        }
        this.creatures = Main.db.fetchCreaturesFromLobby();
        for (Creature c : this.creatures) {
            if (c.getPlayerId() == Main.user.getPlayerId()) {
                this.playerCharacter = (game.Character) c;
            }
            c.setPawnSize(mapGrid.getPrefWidth() / 16, mapGrid.getPrefHeight() / 16);
            mapGrid.add(c.getPawn(), c.getxPos(), c.getyPos());
        }
        for (Character c : this.getCharacters()){
            c.setReadyForNewLevel(false);
        }
    }

    public void monsterAction() {
        if (isMonsterTurn()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Monster monster = ((Monster) creatures.get(playerTurn % creatures.size()));
                    System.out.println(monster.getCreatureName());
                    if (monster.isDead()) {
                        if(Main.db != null) {
                            endTurn();
                        }
                    } else {
                        System.out.println("heihei");
                        monster.monsterMove(creatures);
                        monster.monsterAttack(creatures);
                        if(Main.db != null) {
                            pushCreatureData();
                            endTurn();
                        }
                    }
                }
            }).start();
        }
    }

    public void endTurn(){
        this.incrementPlayerTurn();
       Main.db.incrementPlayerTurn(playerTurn);
    }

    public Level getLevel(){
        return this.level;
    }

    public int getAmountOfLevels() {
        return this.amountOfLevels;
    }

    public boolean attackRange(Monster monster, boolean melee){
        if(melee) {
            if ((Math.abs(playerCharacter.getxPos() - monster.getxPos()) <= 1) && (Math.abs(playerCharacter.getyPos() - monster.getyPos()) <= 1)) {
                return true;
            }
        }
        else if(!melee) {
            if ((Math.abs(playerCharacter.getxPos() - monster.getxPos()) > 1) || (Math.abs(playerCharacter.getyPos() - monster.getyPos())) > 1) {
                return true;
            }
        }
        return false;
    }

    public void setCreatures(ArrayList<Creature> creatureList){
        creatures = creatureList;
    }

    public void setLevel(Level level){
        this.level = level;
    }

    public void incrementPlayerTurn(){
        this.setPlayerTurn(this.getPlayerTurn() + 1);
    }

    public int getPlayerTurn(){
        return this.playerTurn;
    }

    public void setPlayerTurn(int playerTurn){
        this.playerTurn = playerTurn;
    }

    public Character getPlayerCharacter(){
        return this.playerCharacter;
    }

    public void setPlayerCharacter(Character i){
        playerCharacter = i;
    }

    public String toString(){
        StringBuilder string = new StringBuilder("");
        string.append("User host: " + Main.user.isHost() + "\n");
        string.append("Is playerCharacterTurn: " + this.isPlayerCharacterTurn() + "\n");
        string.append("Amount of creatures: " + this.creatures.size() + "\n");
        for (int i = 0; i < this.creatures.size(); i++){
            Creature c = this.creatures.get(i);
            string.append("Creature " + (i+1) + ": " + c.getCreatureName() + " HP: " + c.getHp() + "\n");
        }
        string.append("\n");
        return string.toString();
    }

}
