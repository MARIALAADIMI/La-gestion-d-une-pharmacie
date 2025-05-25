package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3307/pharmacie_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Ne pas stocker la connexion en static, créer une nouvelle connexion à chaque fois
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Ajout de paramètres de connexion importants
            return DriverManager.getConnection(
                    URL + "?useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=10",
                    USER,
                    PASSWORD
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC non trouvé", e);
        }
    }
}