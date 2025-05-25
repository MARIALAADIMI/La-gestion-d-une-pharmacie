package services;

import dao.*;
import entities.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.nio.file.Paths;



public class PharmacieServiceImpl implements IPharmacieService {
    private final ClientDAO clientDAO;
    private final MedicamentDAO medicamentDAO;
    private final FactureDAO factureDAO;

    public PharmacieServiceImpl() throws SQLException {
        this.clientDAO = new ClientDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.factureDAO = new FactureDAO();
    }

    // ============ Méthodes Client ============
    @Override
    public void addClient(Client client) throws SQLException {
        try {
            clientDAO.addClient(client);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'ajout du client: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Client> getAllClients() throws SQLException {
        try {
            return clientDAO.getAllClients();
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des clients: " + e.getMessage(), e);
        }
    }

    @Override
    public Client getClientByCIN(String cin) throws SQLException {
        try {
            return clientDAO.getClientByCIN(cin);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la recherche du client: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateClient(Client client) throws SQLException {
        try {
            clientDAO.updateClient(client);
        } catch (SQLException e) {
            throw new SQLException("Échec de la mise à jour du client: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteClient(String CIN) throws SQLException {
        try {
            clientDAO.deleteClient(CIN);
        } catch (SQLException e) {
            throw new SQLException("Échec de la suppression du client: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean clientExists(String CIN) throws SQLException {
        try {
            return clientDAO.clientExists(CIN);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la vérification du client: " + e.getMessage(), e);
        }
    }

    // ============ Méthodes Medicament ============
    @Override
    public void addMedicament(Medicament medicament) throws SQLException {
        try {
            medicamentDAO.addMedicament(medicament);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'ajout du médicament: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Medicament> getAllMedicaments() throws SQLException {
        try {
            return medicamentDAO.getAllMedicaments();
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des médicaments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Medicament> searchMedicaments(String searchTerm) throws SQLException {
        try {
            return medicamentDAO.searchMedicaments(searchTerm);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la recherche des médicaments: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMedicament(Medicament medicament) throws SQLException {
        try {
            medicamentDAO.updateMedicament(medicament);
        } catch (SQLException e) {
            throw new SQLException("Échec de la mise à jour du médicament: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMedicament(int idMed) throws SQLException {
        try {
            medicamentDAO.deleteMedicament(idMed);
        } catch (SQLException e) {
            throw new SQLException("Échec de la suppression du médicament: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStock(int idMedicament, int quantite) throws SQLException {
        try {
            medicamentDAO.updateStock(idMedicament, quantite);
        } catch (SQLException e) {
            throw new SQLException("Échec de la mise à jour du stock: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean medicamentExists(String codeBarre) throws SQLException {
        try {
            return medicamentDAO.medicamentExists(codeBarre);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la vérification du médicament: " + e.getMessage(), e);
        }
    }

    // ============ Méthodes Facture ============
    @Override
    public void createFacture(Facture facture) throws SQLException {
        if (facture == null) {
            throw new IllegalArgumentException("Facture cannot be null");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                factureDAO.createFacture(facture, conn);
                conn.commit(); // Commit transaction
            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw new SQLException("Failed to create facture: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new SQLException("Database connection error: " + e.getMessage(), e);
        }
    }

    @Override
    public void createFactureWithDetails(Facture facture, List<FactureDetails> detailsList) throws SQLException {
        try {
            factureDAO.createFactureWithDetails(facture, detailsList);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la création de la facture avec détails: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Facture> getFacturesByClient(String CIN) throws SQLException {
        try {
            return factureDAO.getFacturesByClient(CIN);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des factures: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FactureDetails> getFactureDetails(int idFacture) throws SQLException {
        try {
            return factureDAO.getFactureDetails(idFacture);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des détails de facture: " + e.getMessage(), e);
        }
    }

    @Override
    public void generateFacture(int idFacture) throws SQLException {
        try {
            factureDAO.generateFacture(idFacture);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la mise à jour de la facture: " + e.getMessage(), e);
        }
    }


    @Override
    public void generateFacturePDF(Facture facture, String outputPath) throws SQLException, IOException {
        // Validation des paramètres
        if (facture == null) {
            throw new IllegalArgumentException("La facture ne peut pas être null");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin de sortie ne peut pas être vide");
        }

        try {
            // Récupération des détails de la facture depuis la base de données
            List<FactureDetails> details = factureDAO.getFactureDetails(facture.getId_Fac());

            // Vérification qu'il y a bien des détails
            if (details == null || details.isEmpty()) {
                throw new SQLException("Aucun détail de facture trouvé pour la facture ID: " + facture.getId_Fac());
            }

            // Chemin vers le logo - à adapter selon votre structure de projet
            String logoPath = "src/main/resources/images/logo_pharmacie.png";

            // Génération du PDF avec le logo
            FacturePrinter.generatePDF(facture, details, outputPath, logoPath);

        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des détails de facture: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    public void generateFacturePDF(int idFacture, String outputPath) throws SQLException, IOException {
        Facture facture = factureDAO.findById(idFacture);
        if (facture == null) {
            throw new SQLException("Facture non trouvée avec l'ID: " + idFacture);
        }

        List<FactureDetails> details = factureDAO.getFactureDetails(idFacture);
        if (details == null || details.isEmpty()) {
            throw new SQLException("Aucun détail trouvé pour la facture: " + idFacture);
        }

        // Chemin vers le logo - à adapter selon votre structure de projet
        String logoPath = "src/main/resources/images/logo_pharmacie.png";

        FacturePrinter.generatePDF(facture, details, outputPath, logoPath);
    }
    // ============ Méthodes de fermeture ============
    public void close() {
        try {
            if (clientDAO != null) {
                clientDAO.close();
            }
            if (medicamentDAO != null) {
                medicamentDAO.close();
            }
            if (factureDAO != null) {
                factureDAO.close();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture des DAOs: " + e.getMessage());
        }
    }



    //=======================================
    @Override
    public List<Facture> getAllFactures() throws SQLException {
        return factureDAO.getAllFactures();
    }

}