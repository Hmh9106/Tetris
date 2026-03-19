/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tetris;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;
import mino.Block;

import mino.Mino;
import mino.Mino_I;
import mino.Mino_L1;
import mino.Mino_L2;
import mino.Mino_Square;
import mino.Mino_T;
import mino.Mino_Z1;
import mino.Mino_Z2;

import Database.JDBCUtil;
import java.sql.*;
import java.time.LocalTime;

/**
 *
 * @author Dell
 */
public class PlayManager {
    final int WIDTH = 360;
    final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;
    
    //Mino
    Mino currentMino;
    final int MINO_START_X;
    final int MINO_START_Y;
    Mino nexMino;
    final int NEXTMINO_X;
    final int NEXTMINO_Y;
    public static ArrayList<Block> staticBlocks = new ArrayList<>();
    
    //Others
    public static int dropInterval = 60; //mino drop in every 60frame
    
    //Game over
    boolean gameOver;
    
    
    //Effect
    boolean effectCounterOn;
    int effectCounter;
    ArrayList<Integer> effectY = new ArrayList<>();
    
    // Effect variables for spinning squares
    private ArrayList<SpinningSquare> spinningSquares = new ArrayList<>();
    private int lineCountEffect = 0; // Số dòng đã xóa trong hiệu ứng hiện tại
    
    //Score & level
    int level = 1;
    int lines;
    int score; 
    
    private String playerName;
    private long gameStartTime;
    
    // Inner class for spinning square effect
    class SpinningSquare {
        int x, y;
        float angle;
        float size;
        float alpha;
        Color color;
        float speed;
        
        public SpinningSquare(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.angle = 0;
            this.size = Block.SIZE;
            this.alpha = 1.0f;
            this.color = color;
            this.speed = 0.1f + new Random().nextFloat() * 0.2f;
        }
        
        public void update() {
            angle += speed;
            size *= 0.98f; // Thu nhỏ dần
            alpha *= 0.97f; // Mờ dần
            
            // Di chuyển lên trên một chút
            y -= 1;
        }
        
        public void draw(Graphics2D g2) {
            AffineTransform old = g2.getTransform();
            Color oldColor = g2.getColor();
            var oldComposite = g2.getComposite();
            
            // Di chuyển và xoay
            g2.translate(x + size/2, y + size/2);
            g2.rotate(angle);
            
            // Vẽ hình vuông với độ trong suốt
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(color);
            g2.fillRect((int)(-size/2), (int)(-size/2), (int)size, (int)size);
            
            // Vẽ viền trắng
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect((int)(-size/2), (int)(-size/2), (int)size, (int)size);
            
            // Khôi phục
            g2.setComposite(oldComposite);
            g2.setColor(oldColor);
            g2.setTransform(old);
        }
        
        public boolean isAlive() {
            return size > 5 && alpha > 0.05f;
        }
    }
    
    public PlayManager(String playerName) {
    this.playerName = playerName;
    this.gameStartTime = System.currentTimeMillis();
    
    //main area
    left_x = (GamePanel.WIDTH/2) - (WIDTH/2);
    right_x = left_x+WIDTH;
    top_y = 50;
    bottom_y = top_y + HEIGHT;
    MINO_START_X = left_x + (WIDTH/2) - Block.SIZE;
    MINO_START_Y = top_y + Block.SIZE;
    
    NEXTMINO_X = right_x + 175;
    NEXTMINO_Y = top_y + 500;
    
    //starting mino
    currentMino = pickMino();
    currentMino.setXY(MINO_START_X, MINO_START_Y);
    nexMino = pickMino();
    nexMino.setXY(NEXTMINO_X, NEXTMINO_Y);
}
    
    private Mino pickMino(){
        // random mino
        Mino mino = null;
        int i = new Random().nextInt(7);
        
        switch(i){
            case 0: mino = new Mino_I();break;
            case 1: mino = new Mino_L2();break;
            case 2: mino = new Mino_L1();break;
            case 3: mino = new Mino_T();break;
            case 4: mino = new Mino_Z1();break;
            case 5: mino = new Mino_Z2();break;
            case 6: mino = new Mino_Square();break;
        }
        return mino;
    }
    
    public void update(){
        if (currentMino.active == false) {
            //if mino isnt active, put it on the staticBlocks
            staticBlocks.add(currentMino.b[0]);
            staticBlocks.add(currentMino.b[1]);
            staticBlocks.add(currentMino.b[2]);
            staticBlocks.add(currentMino.b[3]);
            
            if (currentMino.b[0].x == MINO_START_X && currentMino.b[0].y == MINO_START_Y) {
                gameOver = true;
                GamePanel.music.stop();
                GamePanel.se.play(2, false);
                saveToDatabase();
            }
            
            currentMino.deactivating = false;
            
            //replace the current mino with the next mino
            currentMino = nexMino;
            currentMino.setXY(MINO_START_X, MINO_START_Y);
            nexMino = pickMino();
            nexMino.setXY(NEXTMINO_X, NEXTMINO_Y);
            //delete row + add score
            checkDelete();
        }
        else{
            currentMino.update();
        }
        
        // Update spinning squares effect
        for (int i = spinningSquares.size() - 1; i >= 0; i--) {
            spinningSquares.get(i).update();
            if (!spinningSquares.get(i).isAlive()) {
                spinningSquares.remove(i);
            }
        }
    }
    
    private void checkDelete(){
        int x = left_x;
        int y = top_y;
        int blockCount = 0;   
        int lineCount = 0;
        
        while(x < right_x && y < bottom_y){
            
            for(int i = 0; i < staticBlocks.size(); i++){
                if(staticBlocks.get(i).x == x && staticBlocks.get(i).y == y){
                    //increase the count if there is a static block
                    blockCount++;
                }
            }
            
            x += Block.SIZE;
            
            if(x == right_x){
                if (blockCount == 12) {
                    
                    // Tạo hiệu ứng hình vuông xoay cho mỗi ô trong dòng
                    Color effectColor;
                    if (lineCount == 0) {
                        effectColor = Color.RED;
                    } else if (lineCount == 1) {
                        effectColor = Color.BLUE;
                    } else if (lineCount == 2) {
                        effectColor = Color.GREEN;
                    } else {
                        effectColor = Color.MAGENTA;
                    }
                    
                    // Tạo các hình vuông xoay tại vị trí của các ô bị xóa
                    for (int col = 0; col < 12; col++) {
                        int blockX = left_x + col * Block.SIZE;
                        int blockY = y;
                        
                        // Tạo nhiều hình vuông với màu sắc khác nhau
                        spinningSquares.add(new SpinningSquare(blockX + Block.SIZE/2, 
                                                              blockY + Block.SIZE/2, 
                                                              effectColor));
                        spinningSquares.add(new SpinningSquare(blockX + Block.SIZE/4, 
                                                              blockY + Block.SIZE/4, 
                                                              Color.WHITE));
                        spinningSquares.add(new SpinningSquare(blockX + 3*Block.SIZE/4, 
                                                              blockY + 3*Block.SIZE/4, 
                                                              new Color(255, 215, 0))); // Vàng kim
                    }
                    
                    effectCounterOn = true;
                    effectY.add(y);
                    
                    for(int i = staticBlocks.size()-1; i > -1; i--){
                        if(staticBlocks.get(i).y == y){
                            staticBlocks.remove(i);
                        }
                    }
                    
                    lineCount++;
                    lines++;
                    lineCountEffect = lineCount;
                    GamePanel.se.play(1, false);
                    
                    //drop speed
                    if(lines % 10 == 0 && dropInterval > 1){
                        level++;
                        if(dropInterval > 10){
                            dropInterval-=10;
                        }
                        else{
                            dropInterval-=1;
                        }
                    }
                    
                    for(int i = 0; i < staticBlocks.size(); i++){
                        if(staticBlocks.get(i).y < y){
                            staticBlocks.get(i).y +=Block.SIZE;
                        }
                    }
                }
                blockCount = 0;
                x = left_x;
                y += Block.SIZE;
            }
        }
        //add score
        if(lineCount > 0){
            int singleLineScore = 100 * level;
            score += singleLineScore * lineCount;
        }
    }
    
    public void saveToDatabase() {
        long gameDuration = System.currentTimeMillis() - gameStartTime;
        LocalTime timePlayed = LocalTime.ofSecondOfDay(gameDuration / 1000);

        try {
            Connection conn = JDBCUtil.getConnection();

            // Kiểm tra xem tên đã tồn tại chưa
            String checkSql = "SELECT COUNT(*) FROM Player WHERE Ten = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, playerName);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            boolean exists = rs.getInt(1) > 0;
            rs.close();
            checkStmt.close();

            if (exists) {
                // Cập nhật nếu điểm mới cao hơn
                String updateSql = """
                    UPDATE Player 
                    SET Diem = ?, Dong = ?, CapDo = ?, ThoiGian = ? 
                    WHERE Ten = ? AND Diem < ?
                """;
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, score);
                updateStmt.setInt(2, lines);
                updateStmt.setInt(3, level);
                updateStmt.setTime(4, Time.valueOf(timePlayed));
                updateStmt.setString(5, playerName);
                updateStmt.setInt(6, score);
                updateStmt.executeUpdate();
                updateStmt.close();
            } else {
                // Thêm mới
                String insertSql = "INSERT INTO Player (Ten, Diem, Dong, CapDo, ThoiGian) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, playerName);
                insertStmt.setInt(2, score);
                insertStmt.setInt(3, lines);
                insertStmt.setInt(4, level);
                insertStmt.setTime(5, Time.valueOf(timePlayed));
                insertStmt.executeUpdate();
                insertStmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
}
    
    public void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(50, 50, 50)); // Màu xám đậm cho lưới
        g2.setStroke(new BasicStroke(1f));
        
        // Vẽ các đường dọc
        for (int i = 0; i <= 12; i++) { // 12 cột (WIDTH/Block.SIZE = 360/30 = 12)
            int x = left_x + i * Block.SIZE;
            g2.drawLine(x, top_y, x, bottom_y);
        }
        
        // Vẽ các đường ngang
        for (int i = 0; i <= 20; i++) { // 20 hàng (HEIGHT/Block.SIZE = 600/30 = 20)
            int y = top_y + i * Block.SIZE;
            g2.drawLine(left_x, y, right_x, y);
        }
    }
    
    public void draw(Graphics2D g2){
        // Vẽ lưới caro
        drawGrid(g2);
        
        //draw area border
        g2.setColor(Color.white);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left_x-4, top_y-4, WIDTH+8, HEIGHT+8);
        
        //draw next brick
        int x = right_x + 100;
        int y = bottom_y - 200;
        g2.drawRect(x, y, 200, 200);
        g2.setFont(new Font("Arial", Font.PLAIN, 30));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawString("NEXT", x+60, y+50);
        
        //draw score
        g2.drawRect(x, top_y, 250, 300);
        x += 40;
        y = top_y +90;
        g2.drawString("LEVEL: " +level, x, y); y +=70;
        g2.drawString("LINES: " +lines, x, y); y +=70;
        g2.drawString("SCORE: " +score, x, y);
        
        //draw current mino
        if(currentMino != null){
            currentMino.draw(g2);
        }
        
        //draw next
        nexMino.draw(g2);
        
        //draw static blocks
        for(int i = 0; i < staticBlocks.size(); i++){
            staticBlocks.get(i).draw(g2);
        }
        
        // Vẽ hiệu ứng hình vuông xoay tròn
        for (SpinningSquare square : spinningSquares) {
            square.draw(g2);
        }
        
        //draw pause
        g2.setColor(Color.gray);
        g2.setFont(g2.getFont().deriveFont(80f));
        if(KeyHandler.pausePressed){
            x = left_x + 70;
            y = top_y + 320;
            g2.drawString("PAUSE️D", x-50, y);
        }
        
        //draw game over
        if(gameOver){
            x = left_x + 25;
            y = top_y + 320;
            g2.setFont(new Font("Arial",Font.BOLD,60));
            g2.setColor(Color.white);
            g2.drawString("GAME OVER", x, y);
        }
        
        //draw mute
        if(KeyHandler.muteMusicPressed){
            x = left_x - 440;
            y = top_y + 30;
            g2.drawString("M", x, y);
        }
    }
}