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


//Contrôleur pour la gestion des factures dans l'application pharmacie
public class FactureController {
    // TableView pour afficher les médicaments disponibles
    @FXML private TableView<Medicament> medicamentTable;

    // Colonnes de la table des médicaments
    @FXML private TableColumn<Medicament, String> colCodeBarre;
    @FXML private TableColumn<Medicament, String> colNom;
    @FXML private TableColumn<Medicament, String> colForme;
    @FXML private TableColumn<Medicament, String> colDosage;
    @FXML private TableColumn<Medicament, String> colPrix;
    @FXML private TableColumn<Medicament, String> colStock;
    @FXML private TableColumn<Medicament, String> colRemboursable;
    @FXML private TableColumn<Medicament, String> colDateAjout;

    // Champs pour les informations client
    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<String> clientSuggestions;

    // Champs pour la recherche et sélection des médicaments
    @FXML private TextField searchField;
    @FXML private ComboBox<String> medicamentSuggestions;
    @FXML private Spinner<Integer> quantiteSpinner;

    // TableView pour afficher les médicaments sélectionnés pour la facture
    @FXML private TableView<FactureDetails> detailsTable;
    @FXML private Label totalLabel;

    // Données de l'application
    private ObservableList<Client> allClients = FXCollections.observableArrayList();
    private ObservableList<String> allCINs = FXCollections.observableArrayList();
    private ObservableList<Medicament> allMedicaments = FXCollections.observableArrayList();
    private ObservableList<FactureDetails> medicamentsList = FXCollections.observableArrayList();
    private ObservableList<Medicament> medicamentsData = FXCollections.observableArrayList();

    // Services
    private IPharmacieService pharmacieService;
    private Client currentClient;
    private Facture facture;
    private Client client;

    //Constructeur initialisant le service pharmacie
    public FactureController() {
        try {
            pharmacieService = new PharmacieServiceImpl();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de connexion à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    //Méthode d'initialisation appelée après le chargement du FXML
    @FXML
    public void initialize() {
        configureClientComponents();
        configureMedicamentComponents();
        configureDetailsTable();

        // Chargement des données initiales
        loadInitialData();
    }

    //Configure les composants relatifs aux clients
    private void configureClientComponents() {
        // Ecouteur pour l'auto-complétion du CIN client
        cinField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                // Filtrage des CIN correspondant à la saisie
                List<String> suggestions = allCINs.stream()
                        .filter(cin -> cin.toLowerCase().contains(newVal.toLowerCase()))
                        .collect(Collectors.toList());
                clientSuggestions.getItems().setAll(suggestions);
                if (!suggestions.isEmpty()) clientSuggestions.show();
            } else {
                clientSuggestions.hide();
            }
        });

        // Action lors de la sélection d'une suggestion
        clientSuggestions.setOnAction(e -> {
            String selectedCIN = clientSuggestions.getSelectionModel().getSelectedItem();
            if (selectedCIN != null) {
                cinField.setText(selectedCIN);
                loadClientDetails(selectedCIN);
            }
        });

        // Gestion de la touche Tab pour la complétion
        cinField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB && !clientSuggestions.getSelectionModel().isEmpty()) {
                String selectedCIN = clientSuggestions.getSelectionModel().getSelectedItem();
                cinField.setText(selectedCIN);
                loadClientDetails(selectedCIN);
                e.consume();
            }
        });
    }

    //Configure les composants relatifs aux médicaments
    private void configureMedicamentComponents() {
        // Configuration des colonnes de la table des médicaments
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

        // Configuration du spinner pour la quantité (1-100)
        quantiteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Configuration de la recherche
        setupSearchListener();
        configureMedicamentAutocomplete();
    }

    //Configure la table des détails de la facture
    private void configureDetailsTable() {

        TableColumn<FactureDetails, String> medicamentCol = new TableColumn<>("Médicament");
        medicamentCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMedicament().getNom_Med()));

        TableColumn<FactureDetails, Integer> quantiteCol = new TableColumn<>("Quantité");
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        TableColumn<FactureDetails, String> prixCol = new TableColumn<>("Prix Unitaire");
        prixCol.setCellValueFactory(cellData -> {
            double prix = cellData.getValue().getMedicament().getPrix_unitaire();
            return new SimpleStringProperty(String.format("%.2f", prix));
        });

        TableColumn<FactureDetails, String> sousTotalCol = new TableColumn<>("Sous-Total");
        sousTotalCol.setCellValueFactory(cellData -> {
            double sousTotal = cellData.getValue().getSousTotal();
            return new SimpleStringProperty(String.format("%.2f", sousTotal));
        });

        // Colonne Action avec bouton de suppression
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

        // Ajout des colonnes à la table
        detailsTable.getColumns().setAll(medicamentCol, quantiteCol, prixCol, sousTotalCol, actionCol);
        detailsTable.setItems(medicamentsList);
    }

    //Charge les données initiales (clients et médicaments)
    private void loadInitialData() {
        try {
            // Chargement de tous les clients
            allClients.setAll(pharmacieService.getAllClients());
            // Extraction des CIN pour l'auto-complétion
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

    //Charge les médicaments depuis la base de données
    private void loadMedicaments() throws SQLException {
        allMedicaments.setAll(pharmacieService.getAllMedicaments());
        medicamentsData.setAll(allMedicaments);
        medicamentTable.setItems(medicamentsData);
    }

    //Gère la recherche de client par CIN
    @FXML
    private void handleSearchClient(ActionEvent event) {
        loadClientDetails(cinField.getText().trim());
    }

    //Charge les détails d'un client à partir de son CIN
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

    //Configure l'auto-complétion pour les médicaments
    private void configureMedicamentAutocomplete() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                // Filtre les médicaments par nom ou code barre
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

        // Action lors de la sélection d'une suggestion
        medicamentSuggestions.setOnAction(e -> {
            String selected = medicamentSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                searchField.setText(selected);
                handleSearch(null);
            }
        });
    }

    //Configure l'écouteur pour la recherche de médicaments
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                medicamentTable.setItems(medicamentsData);
                return;
            }
            performSearch(newValue.trim().toLowerCase());
        });
    }

    //Gère la recherche de médicaments
    @FXML
    private void handleSearch(ActionEvent event) {
        performSearch(searchField.getText().trim().toLowerCase());
    }

    //Effectue la recherche de médicaments
    private void performSearch(String searchText) {
        if (searchText.isEmpty()) {
            medicamentTable.setItems(medicamentsData);
            return;
        }

        // Filtre les médicaments selon le texte de recherche
        ObservableList<Medicament> filteredList = allMedicaments.stream()
                .filter(m -> m.getNom_Med().toLowerCase().contains(searchText) ||
                        (m.getCode_barre() != null && m.getCode_barre().toLowerCase().contains(searchText)))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        medicamentTable.setItems(filteredList);

        if (filteredList.isEmpty()) {
            showAlert("Information", "Aucun médicament trouvé pour : " + searchText, Alert.AlertType.INFORMATION);
        }
    }

    //Gère l'ajout d'un médicament à la facture
    @FXML
    private void handleAddMedicament(ActionEvent event) {
        // Vérification de la sélection
        Medicament selected = medicamentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner un médicament", Alert.AlertType.ERROR);
            return;
        }

        // Vérification de la quantité
        int quantite = quantiteSpinner.getValue();
        if (quantite <= 0) {
            showAlert("Erreur", "La quantité doit être positive", Alert.AlertType.ERROR);
            return;
        }

        // Vérification du stock
        if (selected.getStock_dispo() < quantite) {
            showAlert("Stock insuffisant",
                    String.format("Stock disponible: %d, Quantité demandée: %d",
                            selected.getStock_dispo(), quantite),
                    Alert.AlertType.WARNING);
            return;
        }

        // Vérifie si le médicament est déjà dans la facture
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

        // Ajout du médicament s'il n'existe pas déjà
        if (!exists) {
            medicamentsList.add(new FactureDetails(selected, quantite));
        }

        // Mise à jour du stock affiché
        selected.setStock_dispo(selected.getStock_dispo() - quantite);
        medicamentTable.refresh();

        // Mise à jour de l'interface
        updateTotal();
        detailsTable.refresh();
    }

    //Classe interne pour représenter les détails d'une facture
    public class FactureDetails {
        private int quantite;
        private Medicament medicament;

        //Constructeur avec paramètres
        public FactureDetails(Medicament medicament, int quantite) {
            this.medicament = medicament;
            this.quantite = quantite;
        }

        //Constructeur par défaut
        public FactureDetails() {
        }

        //Calcule le sous-total pour ce médicament
        public double getSousTotal() {
            return medicament != null ? quantite * medicament.getPrix_unitaire() : 0;
        }

        // Getters et setters
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

    //Met à jour le total de la facture
    private void updateTotal() {
        double total = medicamentsList.stream()
                .mapToDouble(FactureDetails::getSousTotal)
                .sum();
        totalLabel.setText(String.format("%.2f DH", total));
    }

    //Génère le PDF de la facture
    private String generatePdf(Facture facture, List<entities.FactureDetails> details) throws IOException {
        // Crée le dossier Downloads s'il n'existe pas
        File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        // Nom du fichier PDF
        String fileName = "Facture_" + facture.getId_Fac() + ".pdf";
        File outputFile = new File(downloadsDir, fileName);

        // Chemin vers le logo
        String logoPath = "/images/photoPharmacie.png";

        // Génération du PDF
        FacturePrinter.generatePDF(facture, details, outputFile.getAbsolutePath(), logoPath);

        return outputFile.getAbsolutePath();
    }

    //Gère la génération de la facture
    @FXML
    private void handleGenerateFacture(ActionEvent event) {
        // Vérifications préalables
        if (currentClient == null || medicamentsList.isEmpty()) {
            showAlert("Erreur", currentClient == null ?
                            "Aucun client sélectionné" : "Aucun médicament ajouté",
                    Alert.AlertType.ERROR);
            return;
        }
        try {
            // Création de la facture
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
                detail.setFacture(facture);
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

            // Réinitialisation du formulaire
            resetForm();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    //Calcule le total de la facture
    private double calculateTotal() {
        return medicamentsList.stream()
                .mapToDouble(FactureDetails::getSousTotal)
                .sum();
    }

    //Réinitialise le formulaire
    private void resetForm() {
        medicamentsList.clear();
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        searchField.clear();
        quantiteSpinner.getValueFactory().setValue(1);
        currentClient = null;
        try {
            // Recharge les médicaments pour actualiser les stocks
            loadMedicaments();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du rechargement des médicaments", Alert.AlertType.ERROR);
        }
        updateTotal();
    }

    //Rafraîchit les données
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

    //Affiche une alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //Initialise le contrôleur avec un client spécifique
    public void initWithClient(Client client) {
        // Remplissage automatique des champs client
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