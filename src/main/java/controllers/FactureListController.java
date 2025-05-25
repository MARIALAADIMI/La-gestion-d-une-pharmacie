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
    @FXML private TableView<Facture> factureTable;
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private TextField searchField;
    @FXML private TableColumn<Facture, String> cinColumn;
    @FXML private TableColumn<Facture, String> nomColumn;
    @FXML private TableColumn<Facture, String> prenomColumn;
    @FXML private Button pdfButton;

    private final IPharmacieService pharmacieService = new PharmacieServiceImpl();
    private final ObservableList<Facture> facturesData = FXCollections.observableArrayList();

    public FactureListController() throws SQLException {
    }

    @FXML
    public void initialize() {
        // Initialiser les options de recherche
        searchTypeCombo.setItems(FXCollections.observableArrayList(
                "Numéro de facture", "CIN", "Nom", "Prénom"
        ));
        searchTypeCombo.getSelectionModel().selectFirst();

        // Configurer les colonnes
        configureColumns();

        // Activer/désactiver le bouton PDF selon la sélection
        pdfButton.disableProperty().bind(
                factureTable.getSelectionModel().selectedItemProperty().isNull()
        );

        // Charger les données
        loadFactures();
    }

    private void configureColumns() {
        // Colonne CIN
        cinColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getClient() != null ?
                                cellData.getValue().getClient().getCIN() : ""
                )
        );

        // Colonne Nom
        nomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getClient() != null ?
                                cellData.getValue().getClient().getNom() : ""
                )
        );

        // Colonne Prénom
        prenomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getClient() != null ?
                                cellData.getValue().getClient().getPrenom() : ""
                )
        );
    }

    private void loadFactures() {
        try {
            List<Facture> factures = pharmacieService.getAllFactures();
            facturesData.setAll(factures);
            factureTable.setItems(facturesData);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des factures: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            factureTable.setItems(facturesData);
            return;
        }

        String searchType = searchTypeCombo.getValue();
        try {
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

            factureTable.setItems(FXCollections.observableArrayList(filteredList));

            if (filteredList.isEmpty()) {
                showAlert("Information", "Aucune facture trouvée", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void refreshData(ActionEvent event) {
        loadFactures();
        searchField.clear();
    }

    @FXML
    private void handleShowPdf(ActionEvent event) {
        Facture selected = factureTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Avertissement", "Veuillez sélectionner une facture", Alert.AlertType.WARNING);
            return;
        }

        String fileName = "Facture_" + selected.getId_Fac() + ".pdf";
        String downloadsPath = System.getProperty("user.home") + "/Downloads/" + fileName;

        try {
            File pdfFile = new File(downloadsPath);
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                showAlert("Information", "Le fichier PDF n'existe pas: " + downloadsPath, Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}