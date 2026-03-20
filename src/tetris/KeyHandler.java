/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tetris;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author Dell
 */
public class KeyHandler implements KeyListener{

    public static boolean upPressed, downPressed, leftPressed, rightPressed, pausePressed, skipPressesd, muteMusicPressed, refreshPressed;
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPressed = true;
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = true;
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
        if(code == KeyEvent.VK_ESCAPE){
            if(pausePressed){
                pausePressed = false;
                GamePanel.music.play(0, true);
                GamePanel.music.loop();
            }
            else{
                pausePressed = true;
                GamePanel.music.stop();
            }
        }
        if(code == KeyEvent.VK_SPACE){
            skipPressesd = true;
        }
        if (code == KeyEvent.VK_M){
            if(muteMusicPressed){
                muteMusicPressed = false;
                GamePanel.music.play(0, true);
                GamePanel.music.loop();
            }
            else{
                muteMusicPressed = true;
                GamePanel.music.stop();
            }
        }    
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    
}
