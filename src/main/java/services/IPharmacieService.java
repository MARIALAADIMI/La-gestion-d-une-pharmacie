package services;

import entities.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface définissant les opérations principales du service de gestion d'une pharmacie.
 */
public interface IPharmacieService {

    // ============ Méthodes Client ============

    //Ajoute un nouveau client dans la base de données.
    void addClient(Client client) throws SQLException;

    //Récupère la liste de tous les clients enregistrés.
    List<Client> getAllClients() throws SQLException;

    //Récupère les informations d’un client via son CIN.
    Client getClientByCIN(String cin) throws SQLException;

    //Met à jour les informations d’un client.
    void updateClient(Client client) throws SQLException;

    //Supprime un client en fonction de son CIN.
    void deleteClient(String CIN) throws SQLException;

    //Vérifie si un client existe déjà via son CIN.
    boolean clientExists(String CIN) throws SQLException;

    // ============ Méthodes Medicament ============

    //Ajoute un nouveau médicament dans la base de données.
    void addMedicament(Medicament medicament) throws SQLException;

    //Récupère la liste de tous les médicaments.
    List<Medicament> getAllMedicaments() throws SQLException;

    //Recherche des médicaments par nom ou code-barres.
    List<Medicament> searchMedicaments(String searchTerm) throws SQLException;

    //Met à jour les informations d’un médicament.
    void updateMedicament(Medicament medicament) throws SQLException;

    //Supprime un médicament via son ID.
    void deleteMedicament(int idMed) throws SQLException;

    //Met à jour le stock d’un médicament (ajout ou retrait de quantités).

    void updateStock(int idMedicament, int quantite) throws SQLException;

    // Vérifie si un médicament existe via son code-barres.
    boolean medicamentExists(String codeBarre) throws SQLException;

    // ============ Méthodes Facture ============

    //Crée une nouvelle facture dans la base de données.
    void createFacture(Facture facture) throws SQLException;

    //Crée une facture et enregistre également ses détails (produits achetés).
    void createFactureWithDetails(Facture facture, List<FactureDetails> detailsList) throws SQLException;

    //Récupère toutes les factures associées à un client.
    List<Facture> getFacturesByClient(String CIN) throws SQLException;

    //Récupère les détails d'une facture spécifique.
    List<FactureDetails> getFactureDetails(int idFacture) throws SQLException;

    //Génère une facture (par exemple mise à jour du total ou des produits).
    void generateFacture(int idFacture) throws SQLException;

    //Génère un fichier PDF à partir d’une facture existante.

    void generateFacturePDF(Facture facture, String outputPath) throws SQLException, IOException;

    // ============ Autres ============

    //Récupère toutes les factures de la base de données.

    List<Facture> getAllFactures() throws SQLException;
}
