package services;

import dao.*;
import entities.*;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.nio.file.Paths;

//Implémentation du service métier principal de la pharmacie.

public class PharmacieServiceImpl implements IPharmacieService {

    // Dépendances DAO pour accéder aux entités en base de données
    private final ClientDAO clientDAO;
    private final MedicamentDAO medicamentDAO;
    private final FactureDAO factureDAO;

    // Constructeur : initialisation des DAOs
    public PharmacieServiceImpl() throws SQLException {
        this.clientDAO = new ClientDAO();
        this.medicamentDAO = new MedicamentDAO();
        this.factureDAO = new FactureDAO();
    }

    // ============ Méthodes Client ============

    // Ajoute un nouveau client dans la base de données
    @Override
    public void addClient(Client client) throws SQLException {
        try {
            clientDAO.addClient(client);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'ajout du client: " + e.getMessage(), e);
        }
    }

    // Récupère tous les clients
    @Override
    public List<Client> getAllClients() throws SQLException {
        try {
            return clientDAO.getAllClients();
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des clients: " + e.getMessage(), e);
        }
    }

    // Récupère un client par son CIN
    @Override
    public Client getClientByCIN(String cin) throws SQLException {
        try {
            return clientDAO.getClientByCIN(cin);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la recherche du client: " + e.getMessage(), e);
        }
    }

    // Met à jour un client existant
    @Override
    public void updateClient(Client client) throws SQLException {
        try {
            clientDAO.updateClient(client);
        } catch (SQLException e) {
            throw new SQLException("Échec de la mise à jour du client: " + e.getMessage(), e);
        }
    }

    // Supprime un client à partir de son CIN
    @Override
    public void deleteClient(String CIN) throws SQLException {
        try {
            clientDAO.deleteClient(CIN);
        } catch (SQLException e) {
            throw new SQLException("Échec de la suppression du client: " + e.getMessage(), e);
        }
    }

    // Vérifie si un client existe (via son CIN)
    @Override
    public boolean clientExists(String CIN) throws SQLException {
        try {
            return clientDAO.clientExists(CIN);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la vérification du client: " + e.getMessage(), e);
        }
    }

    // ============ Méthodes Medicament ============

    // Ajoute un médicament
    @Override
    public void addMedicament(Medicament medicament) throws SQLException {
        try {
            medicamentDAO.addMedicament(medicament);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de l'ajout du médicament: " + e.getMessage(), e);
        }
    }

    // Récupère tous les médicaments
    @Override
    public List<Medicament> getAllMedicaments() throws SQLException {
        try {
            return medicamentDAO.getAllMedicaments();
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des médicaments: " + e.getMessage(), e);
        }
    }

    // Recherche des médicaments à partir d’un mot-clé
    @Override
    public List<Medicament> searchMedicaments(String searchTerm) throws SQLException {
        try {
            return medicamentDAO.searchMedicaments(searchTerm);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la recherche des médicaments: " + e.getMessage(), e);
        }
    }

    // Met à jour les informations d’un médicament
    @Override
    public void updateMedicament(Medicament medicament) throws SQLException {
        try {
            medicamentDAO.updateMedicament(medicament);
        } catch (SQLException e) {
            throw new SQLException("Échec de la mise à jour du médicament: " + e.getMessage(), e);
        }
    }

    // Supprime un médicament à partir de son ID
    @Override
    public void deleteMedicament(int idMed) throws SQLException {
        try {
            medicamentDAO.deleteMedicament(idMed);
        } catch (SQLException e) {
            throw new SQLException("Échec de la suppression du médicament: " + e.getMessage(), e);
        }
    }

    // Met à jour le stock d’un médicament
    @Override
    public void updateStock(int idMedicament, int quantite) throws SQLException {
        try {
            medicamentDAO.updateStock(idMedicament, quantite);
        } catch (SQLException e) {
            throw new SQLException("Échec de la mise à jour du stock: " + e.getMessage(), e);
        }
    }

    // Vérifie si un médicament existe (via son code barre)
    @Override
    public boolean medicamentExists(String codeBarre) throws SQLException {
        try {
            return medicamentDAO.medicamentExists(codeBarre);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la vérification du médicament: " + e.getMessage(), e);
        }
    }

    // ============ Méthodes Facture ============

    // Crée une facture seule (sans détails)
    @Override
    public void createFacture(Facture facture) throws SQLException {
        if (facture == null) {
            throw new IllegalArgumentException("Facture cannot be null");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Démarre une transaction

            try {
                factureDAO.createFacture(facture, conn);
                conn.commit(); // Confirme la transaction
            } catch (SQLException e) {
                conn.rollback(); // Annule en cas d'erreur
                throw new SQLException("Failed to create facture: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new SQLException("Database connection error: " + e.getMessage(), e);
        }
    }

    // Crée une facture avec ses lignes de détail
    @Override
    public void createFactureWithDetails(Facture facture, List<FactureDetails> detailsList) throws SQLException {
        try {
            factureDAO.createFactureWithDetails(facture, detailsList);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la création de la facture avec détails: " + e.getMessage(), e);
        }
    }

    // Récupère toutes les factures d’un client
    @Override
    public List<Facture> getFacturesByClient(String CIN) throws SQLException {
        try {
            return factureDAO.getFacturesByClient(CIN);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des factures: " + e.getMessage(), e);
        }
    }

    // Récupère les détails d’une facture
    @Override
    public List<FactureDetails> getFactureDetails(int idFacture) throws SQLException {
        try {
            return factureDAO.getFactureDetails(idFacture);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la récupération des détails de facture: " + e.getMessage(), e);
        }
    }

    // Met à jour une facture (optionnel, selon votre logique)
    @Override
    public void generateFacture(int idFacture) throws SQLException {
        try {
            factureDAO.generateFacture(idFacture);
        } catch (SQLException e) {
            throw new SQLException("Erreur lors de la mise à jour de la facture: " + e.getMessage(), e);
        }
    }

    // Génère un fichier PDF d’une facture
    @Override
    public void generateFacturePDF(Facture facture, String outputPath) throws SQLException, IOException {
        if (facture == null) {
            throw new IllegalArgumentException("La facture ne peut pas être null");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin de sortie ne peut pas être vide");
        }

        try {
            List<FactureDetails> details = factureDAO.getFactureDetails(facture.getId_Fac());

            if (details == null || details.isEmpty()) {
                throw new SQLException("Aucun détail de facture trouvé pour la facture ID: " + facture.getId_Fac());
            }

            String logoPath = "src/main/resources/images/logo_pharmacie.png";
            FacturePrinter.generatePDF(facture, details, outputPath, logoPath);

        } catch (SQLException | IOException e) {
            throw e;
        }
    }

    // Variante de la méthode PDF en passant seulement l’ID
    public void generateFacturePDF(int idFacture, String outputPath) throws SQLException, IOException {
        Facture facture = factureDAO.findById(idFacture);
        if (facture == null) {
            throw new SQLException("Facture non trouvée avec l'ID: " + idFacture);
        }

        List<FactureDetails> details = factureDAO.getFactureDetails(idFacture);
        if (details == null || details.isEmpty()) {
            throw new SQLException("Aucun détail trouvé pour la facture: " + idFacture);
        }

        String logoPath = "src/main/resources/images/logo_pharmacie.png";
        FacturePrinter.generatePDF(facture, details, outputPath, logoPath);
    }

    // Ferme proprement les ressources DAO
    public void close() {
        try {
            if (clientDAO != null) clientDAO.close();
            if (medicamentDAO != null) medicamentDAO.close();
            if (factureDAO != null) factureDAO.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture des DAOs: " + e.getMessage());
        }
    }

    // Récupère toutes les factures enregistrées
    @Override
    public List<Facture> getAllFactures() throws SQLException {
        return factureDAO.getAllFactures();
    }
}
