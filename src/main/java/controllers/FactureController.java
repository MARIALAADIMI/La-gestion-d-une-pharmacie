package controllers;

import entities.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import services.FacturePrinter;
import services.IPharmacieService;
import services.PharmacieServiceImpl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Desktop;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import services.FacturePrinter;


public class FactureController {
    // Références TableView
    @FXML private TableView<Medicament> medicamentTable;
    @FXML private TableColumn<Medicament, String> colCodeBarre;
    @FXML private TableColumn<Medicament, String> colNom;
    @FXML private TableColumn<Medicament, String> colForme;
    @FXML private TableColumn<Medicament, String> colDosage;
    @FXML private TableColumn<Medicament, String> colPrix;
    @FXML private TableColumn<Medicament, String> colStock;
    @FXML private TableColumn<Medicament, String> colRemboursable;
    @FXML private TableColumn<Medicament, String> colDateAjout;

    // Champs FXML
    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<String> clientSuggestions;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> medicamentSuggestions;
    @FXML private Spinner<Integer> quantiteSpinner;
    @FXML private TableView<FactureDetails> detailsTable;
    @FXML private Label totalLabel;


    // Données
    private ObservableList<Client> allClients = FXCollections.observableArrayList();
    private ObservableList<String> allCINs = FXCollections.observableArrayList();
    private ObservableList<Medicament> allMedicaments = FXCollections.observableArrayList();
    private ObservableList<FactureDetails> medicamentsList = FXCollections.observableArrayList();
    private ObservableList<Medicament> medicamentsData = FXCollections.observableArrayList();

    private IPharmacieService pharmacieService;
    private Client currentClient;
    private Facture facture;
    private Client client;

    public FactureController() {
        try {
            pharmacieService = new PharmacieServiceImpl();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de connexion à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }




    @FXML
    public void initialize() {
        // Configuration initiale
        configureClientComponents();
        configureMedicamentComponents();
        configureDetailsTable();

        // Chargement des données
        loadInitialData();
    }

    private void configureClientComponents() {
        // Configuration de l'auto-complétion client
        cinField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                List<String> suggestions = allCINs.stream()
                        .filter(cin -> cin.toLowerCase().contains(newVal.toLowerCase()))
                        .collect(Collectors.toList());
                clientSuggestions.getItems().setAll(suggestions);
                if (!suggestions.isEmpty()) clientSuggestions.show();
            } else {
                clientSuggestions.hide();
            }
        });

        clientSuggestions.setOnAction(e -> {
            String selectedCIN = clientSuggestions.getSelectionModel().getSelectedItem();
            if (selectedCIN != null) {
                cinField.setText(selectedCIN);
                loadClientDetails(selectedCIN);
            }
        });

        // Gestion de Tab
        cinField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB && !clientSuggestions.getSelectionModel().isEmpty()) {
                String selectedCIN = clientSuggestions.getSelectionModel().getSelectedItem();
                cinField.setText(selectedCIN);
                loadClientDetails(selectedCIN);
                e.consume();
            }
        });
    }

    private void configureMedicamentComponents() {
        // Configuration des colonnes
        colCodeBarre.setCellValueFactory(new PropertyValueFactory<>("code_barre"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_Med"));
        colForme.setCellValueFactory(new PropertyValueFactory<>("forme_pharmaceutique"));
        colDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colPrix.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.2f", cellData.getValue().getPrix_unitaire())));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock_dispo"));
        colRemboursable.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isRemboursable() ? "Oui" : "Non"));
        colDateAjout.setCellValueFactory(cellData -> {
            SimpleStringProperty property = new SimpleStringProperty();
            Date date = cellData.getValue().getDate_ajout();
            property.setValue(new SimpleDateFormat("dd/MM/yyyy").format(date));
            return property;
        });

        // Configuration du spinner
        quantiteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Configuration de la recherche
        setupSearchListener();
        configureMedicamentAutocomplete();
    }

    private void configureDetailsTable() {
        // Colonne Médicament
        TableColumn<FactureDetails, String> medicamentCol = new TableColumn<>("Médicament");
        medicamentCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMedicament().getNom_Med()));

        // Colonne Quantité
        TableColumn<FactureDetails, Integer> quantiteCol = new TableColumn<>("Quantité");
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        // Colonne Prix Unitaire
        TableColumn<FactureDetails, String> prixCol = new TableColumn<>("Prix Unitaire");
        prixCol.setCellValueFactory(cellData -> {
            double prix = cellData.getValue().getMedicament().getPrix_unitaire();
            return new SimpleStringProperty(String.format("%.2f", prix));
        });

        // Colonne Sous-total
        TableColumn<FactureDetails, String> sousTotalCol = new TableColumn<>("Sous-Total");
        sousTotalCol.setCellValueFactory(cellData -> {
            double sousTotal = cellData.getValue().getSousTotal();
            return new SimpleStringProperty(String.format("%.2f", sousTotal));
        });

        // Colonne Action
        TableColumn<FactureDetails, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");
            {
                deleteBtn.setOnAction(event -> {
                    FactureDetails data = getTableView().getItems().get(getIndex());
                    medicamentsList.remove(data);
                    updateTotal();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        detailsTable.getColumns().setAll(medicamentCol, quantiteCol, prixCol, sousTotalCol, actionCol);
        detailsTable.setItems(medicamentsList);
    }

    private void loadInitialData() {
        try {
            // Chargement des clients
            allClients.setAll(pharmacieService.getAllClients());
            allCINs = allClients.stream()
                    .map(Client::getCIN)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            // Chargement des médicaments
            loadMedicaments();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des données initiales", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadMedicaments() throws SQLException {
        allMedicaments.setAll(pharmacieService.getAllMedicaments());
        medicamentsData.setAll(allMedicaments);
        medicamentTable.setItems(medicamentsData);
    }

    @FXML
    private void handleSearchClient(ActionEvent event) {
        loadClientDetails(cinField.getText().trim());
    }

    private void loadClientDetails(String cin) {
        allClients.stream()
                .filter(c -> c.getCIN().equalsIgnoreCase(cin))
                .findFirst()
                .ifPresentOrElse(
                        client -> {
                            currentClient = client;
                            nomField.setText(client.getNom());
                            prenomField.setText(client.getPrenom());
                            clientSuggestions.hide();
                        },
                        () -> showAlert("Non trouvé", "Aucun client avec ce CIN", Alert.AlertType.WARNING)
                );
    }

    private void configureMedicamentAutocomplete() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                List<String> suggestions = allMedicaments.stream()
                        .filter(m -> m.getNom_Med().toLowerCase().contains(newVal.toLowerCase()) ||
                                (m.getCode_barre() != null && m.getCode_barre().contains(newVal)))
                        .map(Medicament::getNom_Med)
                        .collect(Collectors.toList());

                medicamentSuggestions.getItems().setAll(suggestions);
                if (!suggestions.isEmpty()) medicamentSuggestions.show();
            } else {
                medicamentSuggestions.hide();
            }
        });

        medicamentSuggestions.setOnAction(e -> {
            String selected = medicamentSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                searchField.setText(selected);
                handleSearch(null);
            }
        });
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                medicamentTable.setItems(medicamentsData);
                return;
            }
            performSearch(newValue.trim().toLowerCase());
        });
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        performSearch(searchField.getText().trim().toLowerCase());
    }

    private void performSearch(String searchText) {
        if (searchText.isEmpty()) {
            medicamentTable.setItems(medicamentsData);
            return;
        }

        ObservableList<Medicament> filteredList = allMedicaments.stream()
                .filter(m -> m.getNom_Med().toLowerCase().contains(searchText) ||
                        (m.getCode_barre() != null && m.getCode_barre().toLowerCase().contains(searchText)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        medicamentTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            showAlert("Information", "Aucun médicament trouvé pour : " + searchText, Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleAddMedicament(ActionEvent event) {
        Medicament selected = medicamentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner un médicament", Alert.AlertType.ERROR);
            return;
        }

        int quantite = quantiteSpinner.getValue();
        if (quantite <= 0) {
            showAlert("Erreur", "La quantité doit être positive", Alert.AlertType.ERROR);
            return;
        }

        if (selected.getStock_dispo() < quantite) {
            showAlert("Stock insuffisant",
                    String.format("Stock disponible: %d, Quantité demandée: %d",
                            selected.getStock_dispo(), quantite),
                    Alert.AlertType.WARNING);
            return;
        }

        // Vérifier si le médicament existe déjà
        boolean exists = false;
        for (FactureDetails detail : medicamentsList) {
            if (detail.getMedicament().getId_Med() == selected.getId_Med()) {
                int newQty = detail.getQuantite() + quantite;
                if (selected.getStock_dispo() >= newQty) {
                    detail.setQuantite(newQty);
                    exists = true;
                    break;
                } else {
                    showAlert("Stock insuffisant",
                            "Quantité totale demandée dépasse le stock disponible",
                            Alert.AlertType.WARNING);
                    return;
                }
            }
        }

        if (!exists) {
            medicamentsList.add(new FactureDetails(selected, quantite));
        }

        // Mise à jour du stock affiché
        selected.setStock_dispo(selected.getStock_dispo() - quantite);
        medicamentTable.refresh();

        updateTotal();
        detailsTable.refresh();
    }



    public class FactureDetails {
        private int quantite;
        private Medicament medicament;

        public FactureDetails(Medicament medicament, int quantite) {
            this.medicament = medicament;
            this.quantite = quantite;
        }

        public FactureDetails() {

        }

        public double getSousTotal() {
            return medicament != null ? quantite * medicament.getPrix_unitaire() : 0;
        }

        // Getters et setters...
        public int getQuantite() {
            return quantite;
        }

        public void setQuantite(int quantite) {
            this.quantite = quantite;
        }

        public Medicament getMedicament() {
            return medicament;
        }

        public void setMedicament(Medicament medicament) {
        }
    }

    private void updateTotal() {
        double total = medicamentsList.stream()
                .mapToDouble(FactureDetails::getSousTotal)
                .sum();
        totalLabel.setText(String.format("%.2f DH", total));
    }

    // Dans votre contrôleur (FactureController.java)
    // Supprimer la classe interne FactureDetails qui entre en conflit
// et utiliser entities.FactureDetails partout

    private String generatePdf(Facture facture, List<entities.FactureDetails> details) throws IOException {
        // Créer le dossier Downloads s'il n'existe pas
        File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        String fileName = "Facture_" + facture.getId_Fac() + ".pdf";
        File outputFile = new File(downloadsDir, fileName);

        // Chemin vers le logo - ajustez selon votre structure de projet


        // Générer le PDF avec le logo
        // Chemin relatif depuis les ressources (doit commencer par /)
        String logoPath = "/images/photoPharmacie.png";

        FacturePrinter.generatePDF(facture, details, outputFile.getAbsolutePath(), logoPath);

        return outputFile.getAbsolutePath();
    }
    @FXML
    private void handleGenerateFacture(ActionEvent event) {
        if (currentClient == null || medicamentsList.isEmpty()) {
            showAlert("Erreur", currentClient == null ?
                            "Aucun client sélectionné" : "Aucun médicament ajouté",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            // Création de la facture principale
            Facture facture = new Facture();
            facture.setClient(currentClient);
            facture.setDate_Fac(new java.sql.Date(System.currentTimeMillis()));
            facture.setMontant_total(calculateTotal());


            // Conversion des détails
            List<entities.FactureDetails> detailsFacture = new ArrayList<>();
            for (FactureDetails detailInterne : medicamentsList) {
                entities.FactureDetails detail = new entities.FactureDetails();
                detail.setMedicament(detailInterne.getMedicament());
                detail.setQuantite(detailInterne.getQuantite());
                detail.setPrix_unitaire(detailInterne.getMedicament().getPrix_unitaire());
                detail.setFacture(facture); // Ceci est crucial!
                detailsFacture.add(detail);
            }

            // Enregistrement en base
            pharmacieService.createFactureWithDetails(facture, detailsFacture);

            // Génération PDF
            String filePath = generatePdf(facture, detailsFacture);

            // Ouverture du PDF
            try {
                Desktop.getDesktop().open(new File(filePath));
                showAlert("Succès", "Facture générée et enregistrée avec succès", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Information", "Facture enregistrée mais impossible d'ouvrir le PDF", Alert.AlertType.INFORMATION);
            }

            resetForm();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
        //=========================================================

    private double calculateTotal() {
        return medicamentsList.stream()
                .mapToDouble(FactureDetails::getSousTotal)
                .sum();
    }

    private void resetForm() {
        medicamentsList.clear();
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        searchField.clear();
        quantiteSpinner.getValueFactory().setValue(1);
        currentClient = null;
        try {
            loadMedicaments(); // Recharger les médicaments pour actualiser les stocks
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du rechargement des médicaments", Alert.AlertType.ERROR);
        }
        updateTotal();
    }

    @FXML
    private void refreshData(ActionEvent event) {
        try {
            loadMedicaments();
            searchField.clear();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du rafraîchissement des données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


//==================================== add facture =============================
// Nouvelle méthode pour initialiser avec un client
public void initWithClient(Client client) {
    // Remplissage automatique
    cinField.setText(client.getCIN());
    nomField.setText(client.getNom());
    prenomField.setText(client.getPrenom());

    // Blocage de l'édition
    cinField.setEditable(false);
    nomField.setEditable(false);
    prenomField.setEditable(false);

    // Désactivation des suggestions
    clientSuggestions.setDisable(true);

    // Conservation de la référence
    this.currentClient = client;

    // Focus sur la recherche de médicaments
    Platform.runLater(() -> searchField.requestFocus());
}





}