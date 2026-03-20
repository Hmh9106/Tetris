/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tetris;

import Database.JDBCUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Dell
 */
public class GamePanel extends JPanel implements Runnable, MouseListener  {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    final int FPS = 60;
    Thread gameThread;
    PlayManager pm;
    public static Sound music = new Sound();
    public static Sound se = new Sound();
    private String playerName;
    
    // Pause menu
    private boolean showPauseMenu = false;
    private Rectangle resumeButton;
    private Rectangle menuButton;
    private Rectangle quitButton;
    
    // Constructor mặc định (cho menu)
    public GamePanel() {
        this("Anonymous");
    }
    
    // Constructor có tên người chơi
    public GamePanel(String playerName) {
        this.playerName = playerName;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setLayout(null);
        this.addKeyListener(new KeyHandler());
        this.addMouseListener(this);
        this.setFocusable(true);
        this.requestFocusInWindow();
         
        pm = new PlayManager(playerName);
        
        // Khởi tạo các nút pause menu
        int buttonWidth = 300;
        int buttonHeight = 60;
        int centerX = (WIDTH - buttonWidth) / 2;
        int centerY = (HEIGHT - 200) / 2;
        
        resumeButton = new Rectangle(centerX, centerY, buttonWidth, buttonHeight);
        menuButton = new Rectangle(centerX, centerY + 80, buttonWidth, buttonHeight);
        quitButton = new Rectangle(centerX, centerY + 160, buttonWidth, buttonHeight);
    }
    
    public void launchGame() {
        gameThread = new Thread(this);
        gameThread.start();
        
        music.play(0, true);
        music.loop();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        
        while(gameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public void restartGame() {
        PlayManager.staticBlocks.clear();
        pm = new PlayManager(playerName);
        PlayManager.dropInterval = 60;

        // Reset key states
        KeyHandler.upPressed = false;
        KeyHandler.downPressed = false;
        KeyHandler.leftPressed = false;
        KeyHandler.rightPressed = false;
        KeyHandler.pausePressed = false;
        KeyHandler.skipPressesd = false;
        KeyHandler.refreshPressed = false;
        showPauseMenu = false;

        // Dừng thread cũ
        gameThread = null;

        this.requestFocusInWindow();
    }
    
    public void update() {
        if(KeyHandler.refreshPressed) {
            restartGame();
            KeyHandler.refreshPressed = false;
        }
        if (KeyHandler.pausePressed) {
            showPauseMenu = true;
        } else {
            showPauseMenu = false;
            if (pm.gameOver == false) {
                pm.update();
            }
        }
    }
    
    public void stopGame() {
        gameThread = null; // Dừng thread game
        music.stop(); // Dừng nhạc
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        pm.draw(g2);
        // Vẽ pause menu nếu đang pause
        if(showPauseMenu && !pm.gameOver) {
            drawPauseMenu(g2);
        }
        
        // Vẽ game over menu
        if(pm.gameOver) {
            drawGameOverMenu(g2);
        }
    }
    
     private void drawPauseMenu(Graphics2D g2) {
        // blur background
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Vẽ tiêu đề
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        String title = "TẠM DỪNG";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - titleWidth) / 2, 200);
        
        // draw button
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        
        // play button
        g2.setColor(new Color(50, 150, 50));
        g2.fill(resumeButton);
        g2.setColor(Color.WHITE);
        g2.drawString("CHƠI TIẾP", resumeButton.x + 60, resumeButton.y + 40);
        
        // menu button
        g2.setColor(Color.ORANGE);
        g2.fill(menuButton);
        g2.setColor(Color.WHITE);
        g2.drawString("MENU CHÍNH", menuButton.x + 60, menuButton.y + 40);
        
        // escape button
        g2.setColor(Color.RED);
        g2.fill(quitButton);
        g2.setColor(Color.WHITE);
        g2.drawString("THOÁT GAME", quitButton.x + 60, quitButton.y + 40);
    }
    
    private void drawGameOverMenu(Graphics2D g2) {
        // blur background
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // title GAME OVER
        g2.setColor(Color.RED);
        g2.setFont(new Font("Arial", Font.BOLD, 70));
        String title = "GAME OVER";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (WIDTH - titleWidth) / 2, 200);
        
        // draw score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String score = "ĐIỂM: " + pm.score;
        String lines = "DÒNG: " + pm.lines;
        String level = "CẤP ĐỘ: " + pm.level;
        
        int scoreWidth = g2.getFontMetrics().stringWidth(score);
        int linesWidth = g2.getFontMetrics().stringWidth(lines);
        int levelWidth = g2.getFontMetrics().stringWidth(level);
        
        g2.drawString(score, (WIDTH - scoreWidth) / 2, 300);
        g2.drawString(lines, (WIDTH - linesWidth) / 2, 360);
        g2.drawString(level, (WIDTH - levelWidth) / 2, 420);
        
        // draw button
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        
        // play button
        Rectangle restartButton = new Rectangle((WIDTH - 300) / 2, 500, 300, 60);
        g2.setColor(new Color(50, 150, 50));
        g2.fill(restartButton);
        g2.setColor(Color.WHITE);
        g2.drawString("CHƠI LẠI", restartButton.x + 70, restartButton.y + 40);
        
        // Menu button
        Rectangle menuButton2 = new Rectangle((WIDTH - 300) / 2, 580, 300, 60);
        g2.setColor(Color.ORANGE);
        g2.fill(menuButton2);
        g2.setColor(Color.WHITE);
        g2.drawString("MENU CHÍNH", menuButton2.x + 60, menuButton2.y + 40);
    }
    
        // Mouse events
    @Override
    public void mouseClicked(MouseEvent e) {
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        if(showPauseMenu && !pm.gameOver) {
            if(resumeButton.contains(x, y)) {
                // Tiếp tục chơi
                KeyHandler.pausePressed = false;
                showPauseMenu = false;
                if (!KeyHandler.muteMusicPressed) {
                    music.play(0, true);
                    music.loop();
                    this.requestFocusInWindow();
                }
            }
            else if(menuButton.contains(x, y)) {
                // Về menu chính
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc muốn về menu?", 
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
                
                if(confirm == JOptionPane.YES_OPTION) {
                    
                    pm.saveToDatabase();
                    
                    stopGame();
                    
                    showPauseMenu = false;
                    
                    // Quay về menu
                    Main mainFrame = (Main) SwingUtilities.getWindowAncestor(this);
                    mainFrame.showMenu();
                }
            }
            else if(quitButton.contains(x, y)) {
                // Thoát game
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Bạn có chắc muốn thoát? Điểm sẽ được lưu lại.", 
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
                
                if(confirm == JOptionPane.YES_OPTION) {
                    pm.saveToDatabase();
                    JDBCUtil.closeConnection();
                    System.exit(0);
                }
            }
        }
        
        if(pm.gameOver) {
            Rectangle restartButton = new Rectangle((WIDTH - 300) / 2, 500, 300, 60);
            Rectangle menuButton2 = new Rectangle((WIDTH - 300) / 2, 580, 300, 60);
            
            if(restartButton.contains(x, y)) {
                // Chơi lại
                restartGame();
            }
            else if(menuButton2.contains(x, y)) {
                // Về menu
                Main mainFrame = (Main) SwingUtilities.getWindowAncestor(this);
                mainFrame.showMenu();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}