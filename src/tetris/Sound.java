/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tetris;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 *
 * @author Dell
 */
public class Sound {
    Clip musicClip;
    URL url[] = new URL[10];
    
    public Sound(){
        url[0] = getClass().getResource("/sounds/videoplayback.wav");
        url[1] = getClass().getResource("/sounds/delete_line.wav");
        url[2] = getClass().getResource("/sounds/game_over.wav");
        url[3] = getClass().getResource("/sounds/rotation.wav");
        url[4] = getClass().getResource("/sounds/touch_floor.wav");
    }
    public void play(int i, boolean music){
        try{
            AudioInputStream ais = AudioSystem.getAudioInputStream(url[i]);
            Clip clip = AudioSystem.getClip();
            
            if(music){
                musicClip = clip;
            }
            
            clip.open(ais);
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if(event.getType() == LineEvent.Type.STOP){
                        clip.close();
                    }
                }
            });
            ais.close();
            clip.start();
        } catch(Exception e){
        }
        
    }
    public void loop(){
        musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    public void stop(){
        musicClip.stop();
        musicClip.close();
    }
}
