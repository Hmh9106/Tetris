package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtil {
    private static Connection connection;
    
    public static Connection getConnection() {
        final String user = "sa";
        final String password = "123456";
        final String url = "jdbc:sqlserver://localhost:1433;"
                 + "databaseName=Tetris;"
                 + "encrypt=true;trustServerCertificate=true;"
                 + "user=" + user + ";password=" + password + ";";
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            System.out.println("Không tìm thấy driver JDBC!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Kết nối SQL thất bại!");
            e.printStackTrace();
        }
        return null;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đóng kết nối database thành công!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}