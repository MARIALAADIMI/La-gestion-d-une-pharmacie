package controllers;

import entities.Facture;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import services.IPharmacieService;
import services.PharmacieServiceImpl;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class FactureListController {
    // Déclaration des composants FXML
    @FXML private TableView<Facture> factureTable;
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private TextField searchField;
    @FXML private TableColumn<Facture, String> cinColumn;
    @FXML private TableColumn<Facture, String> nomColumn;
    @FXML private TableColumn<Facture, String> prenomColumn;
    @FXML private Button pdfButton;

    // Service pour interagir avec la base de données
    private final IPharmacieService pharmacieService = new PharmacieServiceImpl();
    // Liste observable pour stocker les données des factures
    private final ObservableList<Facture> facturesData = FXCollections.observableArrayList();

    // Constructeur
    public FactureListController() throws SQLException {
    }

    // Méthode d'initialisation appelée automatiquement après le chargement du FXML
    @FXML
    public void initialize() {
        // Initialiser les options de recherche dans la ComboBox
        searchTypeCombo.setItems(FXCollections.observableArrayList(
                "Numéro de facture", "CIN", "Nom", "Prénom"
        ));
        searchTypeCombo.getSelectionModel().selectFirst(); // Sélectionner la première option par défaut

        // Configurer les colonnes du tableau
        configureColumns();

        // Désactiver le bouton PDF si aucune facture n'est sélectionnée
        pdfButton.disableProperty().bind(
                factureTable.getSelectionModel().selectedItemProperty().isNull()
        );

        // Charger les factures depuis la base de données
        loadFactures();
    }

    // Méthode pour configurer les colonnes du tableau
    private void configureColumns() {
        // Colonne CIN - affiche le CIN du client associé à la facture
        cinColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getClient() != null ?
                                cellData.getValue().getClient().getCIN() : ""
                )
        );

        // Colonne Nom - affiche le nom du client
        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getClient() != null ?
                                cellData.getValue().getClient().getNom() : ""
                )
        );

        // Colonne Prénom - affiche le prénom du client
        prenomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getClient() != null ?
                                cellData.getValue().getClient().getPrenom() : ""
                )
        );
    }

    // Méthode pour charger les factures depuis la base de données
    private void loadFactures() {
        try {
            // Récupérer toutes les factures via le service
            List<Facture> factures = pharmacieService.getAllFactures();
            // Mettre à jour la liste observable
            facturesData.setAll(factures);
            // Lier la liste observable au tableau
            factureTable.setItems(facturesData);
        } catch (SQLException e) {
            // Afficher une alerte en cas d'erreur
            showAlert("Erreur", "Erreur lors du chargement des factures: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Gestionnaire d'événement pour la recherche
    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim();
        // Si le champ de recherche est vide, afficher toutes les factures
        if (searchText.isEmpty()) {
            factureTable.setItems(facturesData);
            return;
        }

        String searchType = searchTypeCombo.getValue();
        try {
            // Filtrer la liste des factures selon le critère de recherche
            List<Facture> filteredList = facturesData.stream()
                    .filter(f -> {
                        if (f.getClient() == null) return false;

                        switch (searchType) {
                            case "Numéro de facture":
                                return String.valueOf(f.getId_Fac()).contains(searchText);
                            case "CIN":
                                return f.getClient().getCIN().toLowerCase().contains(searchText.toLowerCase());
                            case "Nom":
                                return f.getClient().getNom().toLowerCase().contains(searchText.toLowerCase());
                            case "Prénom":
                                return f.getClient().getPrenom().toLowerCase().contains(searchText.toLowerCase());
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toList());

            // Mettre à jour le tableau avec les résultats filtrés
            factureTable.setItems(FXCollections.observableArrayList(filteredList));

            // Afficher un message si aucun résultat n'est trouvé
            if (filteredList.isEmpty()) {
                showAlert("Information", "Aucune facture trouvée", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Méthode pour rafraîchir les données
    @FXML
    private void refreshData(ActionEvent event) {
        loadFactures(); // Recharger les factures
        searchField.clear(); // Vider le champ de recherche
    }

    // Méthode pour ouvrir le PDF de la facture sélectionnée
    @FXML
    private void handleShowPdf(ActionEvent event) {
        // Récupérer la facture sélectionnée
        Facture selected = factureTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner une facture", Alert.AlertType.WARNING);
            return;
        }

        // Construire le chemin du fichier PDF dans le dossier Téléchargements
        String fileName = "Facture_" + selected.getId_Fac() + ".pdf";
        String downloadsPath = System.getProperty("user.home") + "/Downloads/" + fileName;

        try {
            File pdfFile = new File(downloadsPath);
            // Vérifier si le fichier existe et l'ouvrir
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                showAlert("Information", "Le fichier PDF n'existe pas: " + downloadsPath, Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}