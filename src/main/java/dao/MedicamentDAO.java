package dao;

import entities.Medicament;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentDAO {
    private static final String TABLE_NAME = "Medicament";

    public void addMedicament(Medicament medicament) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (code_barre, nom_Med, forme_pharmaceutique, dosage, prix_unitaire, stock_dispo, remboursable) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            ps.setString(1, medicament.getCode_barre());
            ps.setString(2, medicament.getNom_Med());
            ps.setString(3, medicament.getForme_pharmaceutique());
            ps.setString(4, medicament.getDosage());
            ps.setDouble(5, medicament.getPrix_unitaire());
            ps.setInt(6, medicament.getStock_dispo());
            ps.setBoolean(7, medicament.isRemboursable());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                throw new SQLException("Échec de l'ajout, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    medicament.setId_Med(generatedKeys.getInt(1));
                }
            }

            conn.commit();
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'ajout du médicament: " + e.getMessage(), e);
        }
    }

    public boolean medicamentExists(String codeBarre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE code_barre = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codeBarre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Medicament> getAllMedicaments() throws SQLException {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Medicament medicament = new Medicament(
                        rs.getString("code_barre"),
                        rs.getString("nom_Med"),
                        rs.getString("forme_pharmaceutique"),
                        rs.getString("dosage"),
                        rs.getDouble("prix_unitaire"),
                        rs.getInt("stock_dispo"),
                        rs.getBoolean("remboursable")
                );
                medicament.setId_Med(rs.getInt("id_Med"));
                medicament.setDate_ajout(rs.getTimestamp("date_ajout"));
                medicaments.add(medicament);
            }
        }
        return medicaments;
    }

    public void updateMedicament(Medicament medicament) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET nom_Med = ?, forme_pharmaceutique = ?, dosage = ?, prix_unitaire = ?, stock_dispo = ?, remboursable = ? WHERE id_Med = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, medicament.getNom_Med());
            ps.setString(2, medicament.getForme_pharmaceutique());
            ps.setString(3, medicament.getDosage());
            ps.setDouble(4, medicament.getPrix_unitaire());
            ps.setInt(5, medicament.getStock_dispo());
            ps.setBoolean(6, medicament.isRemboursable());
            ps.setInt(7, medicament.getId_Med());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucun médicament trouvé avec cet ID");
            }
        }
    }

    public void deleteMedicament(int idMed) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id_Med = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMed);
            ps.executeUpdate();
        }
    }

    public Medicament getMedicamentById(int idMed) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id_Med = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMed);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Medicament medicament = new Medicament(
                            rs.getString("code_barre"),
                            rs.getString("nom_Med"),
                            rs.getString("forme_pharmaceutique"),
                            rs.getString("dosage"),
                            rs.getDouble("prix_unitaire"),
                            rs.getInt("stock_dispo"),
                            rs.getBoolean("remboursable")
                    );
                    medicament.setId_Med(rs.getInt("id_Med"));
                    medicament.setDate_ajout(rs.getTimestamp("date_ajout"));
                    return medicament;
                }
            }
        }
        return null;
    }

    public List<Medicament> searchMedicaments(String keyword) throws SQLException {
        List<Medicament> medicaments = new ArrayList<>();
        String searchBy = new String();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + getSearchColumn(searchBy) + " LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Medicament medicament = extractMedicamentFromResultSet(rs);
                    medicaments.add(medicament);
                }
            }
        }
        return medicaments;
    }private String getSearchColumn(String searchBy) {
        switch (searchBy.toLowerCase()) {
            case "nom":
                return "nom_Med";
            case "code":
                return "code_barre";
            case "forme":
                return "forme_pharmaceutique";
            default:
                throw new IllegalArgumentException("Critère de recherche invalide: " + searchBy);
        }
    }

    private Medicament extractMedicamentFromResultSet(ResultSet rs) throws SQLException {
        Medicament medicament = new Medicament(
                rs.getString("code_barre"),
                rs.getString("nom_Med"),
                rs.getString("forme_pharmaceutique"),
                rs.getString("dosage"),
                rs.getDouble("prix_unitaire"),
                rs.getInt("stock_dispo"),
                rs.getBoolean("remboursable")
        );
        medicament.setId_Med(rs.getInt("id_Med"));
        medicament.setDate_ajout(rs.getTimestamp("date_ajout"));
        return medicament;
    }


    public void updateStock(int idMedicament, double quantite) throws SQLException {
        String sql = "UPDATE Medicament SET stock_dispo = stock_dispo + ? WHERE id_Med = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, quantite);
            ps.setInt(2, idMedicament);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la mise à jour, aucun médicament trouvé avec l'ID: " + idMedicament);
            }
        }
    }

    public void close() {
    }
}