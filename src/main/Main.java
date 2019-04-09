package main;

import GUI.InterfaceMain;
import audio.MusicPlayer;
import audio.ThreadPool;
import database.*;
import user.User;

public class Main {

    public static User user;
    public static Database db;
    public static void main(String[] args){
        db = new Database();

        MusicPlayer.getInstance().changeSong(11);
        ThreadPool.getInstance().runTask(MusicPlayer.getInstance());
        ThreadPool.getInstance().runTask(new InterfaceMain());

        ThreadPool.getInstance().join();
    }
}