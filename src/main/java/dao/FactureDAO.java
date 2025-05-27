package dao;

import entities.Client;
import entities.Facture;
import entities.FactureDetails;
import entities.Medicament;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FactureDAO {
    private Connection connection;

    // Constructeur : initialise la connexion à la base
    public FactureDAO() throws SQLException {
        this.connection = DBConnection.getConnection();
    }

    //Met à jour la date d'une facture à la date actuelle.
    public void generateFacture(int idFacture) throws SQLException {
        String query = "UPDATE Facture SET date_Fac = CURRENT_DATE WHERE id_Fac = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idFacture);
            stmt.executeUpdate();
        }
    }

    //Récupère toutes les factures d’un client à partir de son CIN.
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

    //Récupère les détails d’une facture (liste des médicaments achetés).
    public List<FactureDetails> getFactureDetails(int idFacture) throws SQLException {
        List<FactureDetails> details = new ArrayList<>();
        String query = "SELECT fd.*, m.nom_Med, m.prix_unitaire FROM Facture_Details fd " +
                "JOIN Medicament m ON fd.id_Med = m.id_Med WHERE fd.id_Fac = ?";

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

    //Ferme proprement la connexion.
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }

    // Insère une facture dans la base (sans les détails).
    public void createFacture(Facture facture, Connection conn) throws SQLException {
        if (facture == null || conn == null || facture.getClient() == null || facture.getClient().getCIN() == null) {
            throw new IllegalArgumentException("Facture, connexion ou client invalide");
        }

        String sql = "INSERT INTO facture (CIN, date_Fac, montant_total) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, facture.getClient().getCIN());
            stmt.setDate(2, facture.getDate_Fac());
            stmt.setDouble(3, facture.getMontant_total());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Échec création facture.");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    facture.setId_Fac(rs.getInt(1));
                } else {
                    throw new SQLException("ID facture non généré.");
                }
            }
        }
    }

    //Met à jour le stock des médicaments.
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

    //Crée une facture complète avec ses détails (médicaments) dans une transaction.
    public void createFactureWithDetails(Facture facture, List<FactureDetails> details) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            createFacture(facture, conn); // insère la facture

            if (facture.getId_Fac() == 0) throw new SQLException("ID facture non généré");

            // insère les détails
            for (FactureDetails detail : details) {
                detail.setFacture(facture); // lie le détail à la facture
                String sql = "INSERT INTO Facture_Details (id_Fac, id_Med, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, facture.getId_Fac());
                    stmt.setInt(2, detail.getMedicament().getId_Med());
                    stmt.setInt(3, detail.getQuantite());
                    stmt.setDouble(4, detail.getPrix_unitaire());
                    stmt.executeUpdate();
                }
            }

            updateMedicamentStock(details, conn); // met à jour le stock

            conn.commit(); // valide la transaction
        } catch (SQLException e) {
            if (conn != null) conn.rollback(); // rollback si erreur
            throw new SQLException("Erreur lors de la création complète de la facture", e);
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

    //Recherche une facture par son ID.
    public Facture findById(int idFacture) throws SQLException {
        String query = "SELECT f.*, c.CIN, c.nom, c.prenom FROM Facture f " +
                "JOIN Client c ON f.CIN = c.CIN WHERE f.id_Fac = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idFacture);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Facture facture = new Facture();
                    facture.setId_Fac(rs.getInt("id_Fac"));
                    // À compléter si vous souhaitez remplir d’autres champs
                    return facture;
                }
            }
        }
        return null;
    }

    //Récupère toutes les factures de tous les clients.
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
