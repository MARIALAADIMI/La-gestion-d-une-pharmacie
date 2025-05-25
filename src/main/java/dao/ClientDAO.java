package dao;

import entities.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {
    private static final String TABLE_NAME = "client";

    public void addClient(Client client) throws SQLException {
        String sql = "INSERT INTO client (CIN, nom, prenom, tele, adresse, date_inscription) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false); // Désactive l'autocommit

            ps.setString(1, client.getCIN());
            ps.setString(2, client.getNom());
            ps.setString(3, client.getPrenom());
            ps.setString(4, client.getTele());
            ps.setString(5, client.getAdresse());
            ps.setTimestamp(6, new Timestamp(client.getDateInscription().getTime()));

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                throw new SQLException("Échec de l'ajout, aucune ligne affectée.");
            }

            conn.commit(); // Valide explicitement la transaction

        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'ajout du client: " + e.getMessage(), e);
        }
    }

    public boolean clientExists(String CIN) throws SQLException {
        String sql = "SELECT COUNT(*) FROM client WHERE CIN = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, CIN);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client client = new Client(
                        rs.getString("CIN"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("tele"),
                        rs.getString("adresse")
                );
                client.setDateInscription(rs.getTimestamp("date_inscription"));
                clients.add(client);
            }
        }
        return clients;
    }

    public void updateClient(Client client) throws SQLException {
        String sql = "UPDATE client SET nom = ?, prenom = ?, tele = ?, adresse = ?, date_inscription = ? WHERE CIN = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, client.getNom());
            ps.setString(2, client.getPrenom());
            ps.setString(3, client.getTele());
            ps.setString(4, client.getAdresse());
            ps.setTimestamp(5, new Timestamp(client.getDateInscription().getTime()));
            ps.setString(6, client.getCIN());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucun client trouvé avec ce CIN");
            }
        }
    }

    public void deleteClient(String CIN) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE CIN = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, CIN);
            ps.executeUpdate();
        }
    }

    public Client getClientByCIN(String CIN) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE CIN = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, CIN);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Client client = new Client(
                            rs.getString("CIN"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("tele"),
                            rs.getString("adresse")
                    );
                    client.setDateInscription(rs.getTimestamp("date_inscription"));
                    return client;
                }
            }
        }
        return null;
    }

    public List<Client> searchClients(String keyword, String searchType) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + searchType + " LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Client client = new Client(
                            rs.getString("CIN"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("tele"),
                            rs.getString("adresse")
                    );
                    client.setDateInscription(rs.getTimestamp("date_inscription"));
                    clients.add(client);
                }
            }
        }
        return clients;
    }

    public void close() {
    }
}