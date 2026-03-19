/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mino;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 *
 * @author Dell
 */
public class Block extends Rectangle{
    public int x,y;
    public static final int SIZE = 30;
    public Color c;
    
    public Block(Color c){
        this.c = c;
    }
    public void draw(Graphics2D g2){
        int margin = 2;
        g2.setColor(c);
        g2.fillRect(x + margin, y + margin, SIZE - (margin*2), SIZE- (margin*2));
    }
}
