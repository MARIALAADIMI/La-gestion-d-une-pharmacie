package dao;

import entities.Client;
import entities.Facture;
import entities.FactureDetails;
import entities.Medicament;


import javax.swing.text.Document;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FactureDAO {
    private Connection connection;


    public FactureDAO() throws SQLException {
        this.connection = DBConnection.getConnection();
    }








    public void generateFacture(int idFacture) throws SQLException {
        // Cette méthode peut être supprimée ou modifiée pour faire autre chose
        // Exemple alternative : mettre à jour la date
        String query = "UPDATE Facture SET date_Fac = CURRENT_DATE WHERE id_Fac = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idFacture);
            stmt.executeUpdate();
        }
    }

    public List<Facture> getFacturesByClient(String cin) throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT * FROM Facture WHERE CIN = ? ORDER BY date_Fac DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Facture facture = new Facture();
                    facture.setId_Fac(rs.getInt("id_Fac"));
                    facture.setDate_Fac(rs.getDate("date_Fac"));
                    facture.setMontant_total(rs.getDouble("montant_total"));



                    factures.add(facture);
                }
            }
        }
        return factures;
    }


    public List<FactureDetails> getFactureDetails(int idFacture) throws SQLException {
        List<FactureDetails> details = new ArrayList<>();
        String query = "SELECT fd.*, m.nom_Med, m.prix_unitaire FROM Facture_Details fd " +
                "JOIN Medicament m ON fd.id_Med = m.id_Med " +
                "WHERE fd.id_Fac = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idFacture);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FactureDetails detail = new FactureDetails();
                    detail.setQuantite(rs.getInt("quantite"));

                    Medicament medicament = new Medicament();
                    medicament.setId_Med(rs.getInt("id_Med"));
                    medicament.setNom_Med(rs.getString("nom_Med"));
                    medicament.setPrix_unitaire(rs.getDouble("prix_unitaire"));

                    detail.setMedicament(medicament);
                    details.add(detail);
                }
            }
        }
        return details;
    }

    /**
     * Ferme la connexion à la base de données
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }



    public void createFacture(Facture facture, Connection conn) throws SQLException {
        if (facture == null) {
            throw new IllegalArgumentException("Facture cannot be null");
        }
        if (conn == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        if (facture.getClient() == null || facture.getClient().getCIN() == null) {
            throw new IllegalArgumentException("Client CIN cannot be null");
        }

        String sql = "INSERT INTO facture (CIN, date_Fac, montant_total) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, facture.getClient().getCIN());
            stmt.setDate(2, facture.getDate_Fac());
            stmt.setDouble(3, facture.getMontant_total());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating facture failed, no rows affected.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    facture.setId_Fac(rs.getInt(1));
                } else {
                    throw new SQLException("Creating facture failed, no ID obtained.");
                }
            }
        }
    }

    private void updateMedicamentStock(List<FactureDetails> details, Connection conn) throws SQLException {
        String updateQuery = "UPDATE Medicament SET stock_dispo = stock_dispo - ? WHERE id_Med = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            for (FactureDetails detail : details) {
                stmt.setInt(1, detail.getQuantite());
                stmt.setInt(2, detail.getMedicament().getId_Med());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    public void createFactureWithDetails(Facture facture, List<FactureDetails> details) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Désactive l'auto-commit

            // 1. Insère la facture et récupère l'ID généré
            createFacture(facture, conn);

            // Vérifie que l'ID a bien été généré
            if (facture.getId_Fac() == 0) {
                throw new SQLException("Échec de la génération de l'ID de facture");
            }

            // 2. Insère chaque détail avec l'ID de la facture
            for (FactureDetails detail : details) {
                // S'assure que la relation est bien établie
                detail.setFacture(facture);

                String sql = "INSERT INTO Facture_Details (id_Fac, id_Med, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, facture.getId_Fac());
                    stmt.setInt(2, detail.getMedicament().getId_Med());
                    stmt.setInt(3, detail.getQuantite());
                    stmt.setDouble(4, detail.getPrix_unitaire());
                    stmt.executeUpdate();
                }
            }

            // 3. Met à jour les stocks
            updateMedicamentStock(details, conn);

            conn.commit(); // Valide la transaction
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Annule en cas d'erreur
            }
            throw new SQLException("Erreur lors de la création de la facture avec détails: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Erreur fermeture connexion: " + e.getMessage());
                }
            }
        }
    }


    public Facture findById(int idFacture) throws SQLException {
        String query = "SELECT f.*, c.CIN, c.nom, c.prenom FROM Facture f " +
                "JOIN Client c ON f.CIN = c.CIN WHERE f.id_Fac = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idFacture);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Facture facture = new Facture();
                    facture.setId_Fac(rs.getInt("id_Fac"));
                    // ... initialisez tous les champs
                    return facture;
                }
            }
        }
        return null;
    }





    //=========================================


    public List<Facture> getAllFactures() throws SQLException {
        List<Facture> factures = new ArrayList<>();
        String query = "SELECT f.*, c.CIN, c.nom, c.prenom FROM Facture f " +
                "JOIN Client c ON f.CIN = c.CIN ORDER BY f.id_Fac DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Facture facture = new Facture();
                facture.setId_Fac(rs.getInt("id_Fac"));
                facture.setDate_Fac(rs.getDate("date_Fac"));
                facture.setMontant_total(rs.getDouble("montant_total"));

                Client client = new Client();
                client.setCIN(rs.getString("CIN"));
                client.setNom(rs.getString("nom"));
                client.setPrenom(rs.getString("prenom"));

                facture.setClient(client);
                factures.add(facture);
            }
        }
        return factures;
    }



}
