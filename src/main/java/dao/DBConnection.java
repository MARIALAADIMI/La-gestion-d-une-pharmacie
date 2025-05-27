package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // URL de connexion à la base de données MySQL
    // Le port 3307 est utilisé ici (vérifiez qu'il correspond à votre configuration MySQL)
    private static final String URL = "jdbc:mysql://localhost:3307/pharmacie_db";

    // Identifiants de connexion à la base de données
    private static final String USER = "root";
    private static final String PASSWORD = "";

    //Méthode pour obtenir une nouvelle connexion à la base de données.
    public static Connection getConnection() throws SQLException {
        try {
            // Chargement explicite du driver JDBC MySQL (facultatif avec JDBC 4.0+ mais utile pour compatibilité)
            Class.forName("com.mysql.cj.jdbc.Driver");


            return DriverManager.getConnection(
                    URL + "?useSSL=false&autoReconnect=true&failOverReadOnly=false&maxReconnects=10",
                    USER,
                    PASSWORD
            );
        } catch (ClassNotFoundException e) {
            // Si le driver JDBC n’est pas trouvé dans le classpath
            throw new SQLException("Driver JDBC non trouvé", e);
        }
    }
}
