package tetris;

import Database.JDBCUtil;
import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private String playerName;
    
    public Main() {
        setTitle("Tetris Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Khởi tạo kết nối database
        JDBCUtil.getConnection();
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Menu panel
        menuPanel = new MenuPanel();
        menuPanel.getStartButton().addActionListener(e -> startGame());
        menuPanel.getExitButton().addActionListener(e -> {
            JDBCUtil.closeConnection();
            System.exit(0);
        });
        mainPanel.add(menuPanel, "MENU");
        
        // Game panel (khởi tạo sau)
        gamePanel = new GamePanel();
        mainPanel.add(gamePanel, "GAME");
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void startGame() {
        playerName = menuPanel.getPlayerName();
        
        // KHÔNG xóa gamePanel cũ, chỉ cập nhật tên và restart
        gamePanel.setPlayerName(playerName);
        gamePanel.restartGame();
        
        // Chuyển sang màn hình game
        cardLayout.show(mainPanel, "GAME");
        
        // QUAN TRỌNG: Yêu cầu focus cho GamePanel để nhận sự kiện bàn phím
        gamePanel.requestFocusInWindow();
        
        gamePanel.launchGame();
    }
    
    public void showMenu() {
        // Refresh bảng xếp hạng
        menuPanel.refreshRanking();
        
        // KHÔNG xóa gamePanel, chỉ dừng game và chuyển về menu
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        
        // Chuyển về menu
        cardLayout.show(mainPanel, "MENU");
        
        // Dừng nhạc game
        GamePanel.music.stop();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Main().setVisible(true);
        });
    }
}