package game;

import main.*;
import audio.MusicPlayer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Level {
    private int levelId;
    private int music;
    private String backgroundUrl;
    private boolean readyForNewLevel = false;
    public ImageView backgroundImage;
    private String nextLevelName;

    public Level(int levelId, int music, String backgroundUrl){
        this.levelId = levelId;
        this.music = music;
        this.backgroundUrl = backgroundUrl;
        this.backgroundImage = new ImageView(new Image("GUI/images/" + backgroundUrl));
    }

    public Level(int levelId){
        Level firstLevel = Main.db.fetchLevelObject(levelId);
        this.setLevelId(firstLevel.getLevelId());
        this.setMusic(firstLevel.getMusic());
        this.setBackgroundUrl(firstLevel.getBackgroundUrl());
        this.backgroundImage = new ImageView(new Image("GUI/images/" + firstLevel.getBackgroundUrl()));
        MusicPlayer.getInstance().stopSong();
        MusicPlayer.getInstance().changeSong(this.music);
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getMusic() {
        return music;
    }

    public void setMusic(int music) {
        this.music = music;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public boolean isReadyForNewLevel() {
        return readyForNewLevel;
    }

    public void setReadyForNewLevel(boolean readyForNewLevel) {
        this.readyForNewLevel = readyForNewLevel;
    }

    public void updateLevel(){
        Level newLevel = Main.db.fetchLevelObject(Main.db.fetchLevelId(Main.user.getLobbyKey()));
        if (newLevel != null) {
            this.setMusic(newLevel.getMusic());
            this.setBackgroundUrl(newLevel.getBackgroundUrl());
            this.backgroundImage.setImage(new Image("GUI/images/" + this.getBackgroundUrl()));
            MusicPlayer.getInstance().stopSong();
            MusicPlayer.getInstance().changeSong(this.music);
        }
        else {
            this.setLevelId(this.getLevelId() + 1);
        }
    }

    public String getLevelName(){
        String[] urlParts = this.getBackgroundUrl().split("-");
        return urlParts[0];
    }
}
