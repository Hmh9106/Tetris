/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tetris;

/**
 *
 * @author Dell
 */
import Database.JDBCUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MenuPanel extends JPanel {
    private JTextField txtTen;
    private JButton btnStart;
    private JButton btnExit;
    private JTable rankingTable;
    private DefaultTableModel tableModel;
    
    public MenuPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1280, 720));
        
        // Tiêu đề
        JLabel title = new JLabel("TETRIS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 80));
        title.setForeground(Color.CYAN);
        add(title, BorderLayout.NORTH);
        
        // Panel trung tâm
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.BLACK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        // Panel nhập tên
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setBackground(Color.BLACK);
        inputPanel.setMaximumSize(new Dimension(800, 60));
        
        JLabel lblTen = new JLabel("NHẬP TÊN CỦA BẠN:");
        lblTen.setFont(new Font("Arial", Font.BOLD, 24));
        lblTen.setForeground(Color.WHITE);
        
        txtTen = new JTextField(20);
        txtTen.setFont(new Font("Arial", Font.PLAIN, 20));
        txtTen.setPreferredSize(new Dimension(250, 40));
        txtTen.setBackground(Color.DARK_GRAY);
        txtTen.setForeground(Color.WHITE);
        txtTen.setCaretColor(Color.WHITE);
        
        inputPanel.add(lblTen);
        inputPanel.add(txtTen);
        
        centerPanel.add(inputPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Label bảng xếp hạng
        JLabel lblRanking = new JLabel("BẢNG XẾP HẠNG", SwingConstants.CENTER);
        lblRanking.setFont(new Font("Arial", Font.BOLD, 36));
        lblRanking.setForeground(Color.YELLOW);
        lblRanking.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblRanking);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Tạo bảng xếp hạng
        String[] columns = {"HẠNG", "TÊN", "ĐIỂM", "DÒNG", "CẤP ĐỘ", "THỜI GIAN"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        rankingTable = new JTable(tableModel);
        rankingTable.setFont(new Font("Arial", Font.PLAIN, 18));
        rankingTable.setRowHeight(35);
        rankingTable.setBackground(Color.DARK_GRAY);
        rankingTable.setForeground(Color.WHITE);
        rankingTable.setGridColor(Color.GRAY);
        rankingTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));
        rankingTable.getTableHeader().setBackground(Color.GRAY);
        rankingTable.getTableHeader().setForeground(Color.BLACK);
        rankingTable.getTableHeader().setReorderingAllowed(false);
        
        ((javax.swing.table.DefaultTableCellRenderer) rankingTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        // Căn giữa các cột
        for (int i = 0; i < columns.length; i++) {
            rankingTable.getColumnModel().getColumn(i).setCellRenderer(new CenterTableCellRenderer());
        }
        
        JScrollPane scrollPane = new JScrollPane(rankingTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.DARK_GRAY);
        
        centerPanel.add(scrollPane);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel nút
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 40, 0));
        
        btnStart = new JButton("BẮT ĐẦU CHƠI");
        btnStart.setFont(new Font("Arial", Font.BOLD, 28));
        btnStart.setBackground(new Color(50, 150, 50));
        btnStart.setForeground(Color.BLACK);
        btnStart.setFocusPainted(false);
        btnStart.setPreferredSize(new Dimension(250, 60));
        
        btnExit = new JButton("THOÁT");
        btnExit.setFont(new Font("Arial", Font.BOLD, 28));
        btnExit.setBackground(Color.RED);
        btnExit.setForeground(Color.BLACK);
        btnExit.setFocusPainted(false);
        btnExit.setPreferredSize(new Dimension(250, 60));
        
        buttonPanel.add(btnStart);
        buttonPanel.add(btnExit);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load dữ liệu bảng xếp hạng
        loadRankingData();
    }
    
    private void loadRankingData() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        
        try {
            Connection conn = JDBCUtil.getConnection();

            String sql = """
                SELECT TOP 10 
                    ROW_NUMBER() OVER (ORDER BY Diem DESC, Dong DESC) AS ThuTu,
                    Ten, Diem, Dong, CapDo, 
                    CONVERT(VARCHAR(8), ThoiGian, 108) as ThoiGianStr
                FROM Player
                ORDER BY Diem DESC, Dong DESC
            """;
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("ThuTu"),
                    rs.getString("Ten"),
                    rs.getInt("Diem"),
                    rs.getInt("Dong"),
                    rs.getInt("CapDo"),
                    rs.getString("ThoiGianStr")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    // Renderer để căn giữa các ô trong bảng
    class CenterTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        public CenterTableCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(new Font("Arial", Font.PLAIN, 18));
            c.setForeground(Color.WHITE);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.DARK_GRAY : new Color(70, 70, 70));
            }
            return c;
        }
    }
    
    public JButton getStartButton() {
        return btnStart;
    }
    
    public JButton getExitButton() {
        return btnExit;
    }
    
    public String getPlayerName() {
        String name = txtTen.getText().trim();
        if (name.isEmpty()) {
            name = "Anonymous";
        }
        return name;
    }
    
    public void refreshRanking() {
        loadRankingData();
    }
}