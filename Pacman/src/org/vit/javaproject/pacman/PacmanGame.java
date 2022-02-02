package org.vit.javaproject.pacman;

import javax.swing.JFrame;

public class PacmanGame extends JFrame {
    
    public PacmanGame() {
        add(new GameModel());
    }
    public static void main(String[] args) {
        PacmanGame pacObj = new PacmanGame();
        pacObj.setVisible(true);
        pacObj.setTitle("Pacman Game");
        pacObj.setSize(380, 420);
        pacObj.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pacObj.setLocationRelativeTo(null);
    }   
}
