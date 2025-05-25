package services;

import entities.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface IPharmacieService {
    // ============ Méthodes Client ============
    void addClient(Client client) throws SQLException;
    List<Client> getAllClients() throws SQLException;
    Client getClientByCIN(String cin) throws SQLException;
    void updateClient(Client client) throws SQLException;
    void deleteClient(String CIN) throws SQLException;
    boolean clientExists(String CIN) throws SQLException;

    // ============ Méthodes Medicament ============
    void addMedicament(Medicament medicament) throws SQLException;
    List<Medicament> getAllMedicaments() throws SQLException;
    List<Medicament> searchMedicaments(String searchTerm) throws SQLException;
    void updateMedicament(Medicament medicament) throws SQLException;
    void deleteMedicament(int idMed) throws SQLException;
    void updateStock(int idMedicament, int quantite) throws SQLException;
    boolean medicamentExists(String codeBarre) throws SQLException;

    // ============ Méthodes Facture ============
    void createFacture(Facture facture) throws SQLException;
    void createFactureWithDetails(Facture facture, List<FactureDetails> detailsList) throws SQLException;
    List<Facture> getFacturesByClient(String CIN) throws SQLException;
    List<FactureDetails> getFactureDetails(int idFacture) throws SQLException;
    void generateFacture(int idFacture) throws SQLException;


    void generateFacturePDF(Facture facture, String outputPath) throws SQLException, IOException;



    //=============================================
    List<Facture> getAllFactures() throws SQLException;
}