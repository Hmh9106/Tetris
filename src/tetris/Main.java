package tetris;

import Database.JDBCUtil;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

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

        setIconForProgram();
        
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

    private void setIconForProgram() {
            URL iconURL = getClass().getResource("/imgs/icon128.png");
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
    }
    
    private void startGame() {
        playerName = menuPanel.getPlayerName();
        
        // Xóa game panel cũ nếu có
        if (gamePanel != null) {
            mainPanel.remove(gamePanel);
        }
        
        // Tạo game panel mới với tên người chơi
        gamePanel = new GamePanel(playerName);
        mainPanel.add(gamePanel, "GAME");
        
        // Chuyển sang màn hình game
        cardLayout.show(mainPanel, "GAME");
        
        // Yêu cầu focus cho GamePanel
        gamePanel.requestFocusInWindow();
        
        gamePanel.launchGame();
    }
    
    public void showMenu() {
        menuPanel.refreshRanking();
        
        if (gamePanel != null) {
            mainPanel.remove(gamePanel);
            gamePanel = null;
        }
        
        gamePanel = new GamePanel();
        mainPanel.add(gamePanel, "GAME");
        
        cardLayout.show(mainPanel, "MENU");
        GamePanel.music.stop();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Main().setVisible(true);
        });
    }
}