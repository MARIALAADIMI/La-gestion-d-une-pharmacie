package controllers;

import entities.Client;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import services.IPharmacieService;
import services.PharmacieServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ClientController {
    // Références aux éléments TableView dans le FXML
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, String> colCIN;
    @FXML private TableColumn<Client, String> colNom;
    @FXML private TableColumn<Client, String> colPrenom;
    @FXML private TableColumn<Client, String> colTele;
    @FXML private TableColumn<Client, String> colAdresse;
    @FXML private TableColumn<Client, String> colDate;

    // Références aux champs du formulaire dans le FXML
    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField teleField;
    @FXML private TextArea adresseField;
    @FXML private DatePicker dateField;
    @FXML private TextField searchField;

    // Références aux boutons dans le FXML
    @FXML private Button btnNouveau;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnnuler;

    private final IPharmacieService pharmacieService = new PharmacieServiceImpl();
    private final ObservableList<Client> clientsData = FXCollections.observableArrayList();
    private boolean isEditMode = false;

    public ClientController() throws SQLException {
    }

     //Méthode d'initialisation appelée automatiquement après le chargement du FXML
    @FXML
    public void initialize() {
        configureTableColumns(); // Configure les colonnes du tableau
        initializeForm();        // Initialise le formulaire
        loadClients();           // Charge les clients depuis la base de données
        setEditMode(false);       // Désactive le mode édition
        setupSearchListener();   // Configure l'écouteur pour la recherche
    }

     //Configure les colonnes de la TableView et leurs liaisons avec les propriétés des objets Client
    private void configureTableColumns() {
        // Lie chaque colonne à une propriété de l'objet Client
        colCIN.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTele.setCellValueFactory(new PropertyValueFactory<>("tele"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        // Formatage spécial pour la date
        colDate.setCellValueFactory(cellData -> {
            SimpleStringProperty property = new SimpleStringProperty();
            Date date = cellData.getValue().getDateInscription();
            property.setValue(new SimpleDateFormat("dd/MM/yyyy").format(date));
            return property;
        });

        // Trie par défaut sur la colonne nom
        clientTable.getSortOrder().add(colNom);
    }

    // Initialise le formulaire avec des valeurs par défaut
    private void initializeForm() {
        dateField.setValue(LocalDate.now()); // Date du jour par défaut
        clearForm(); // Vide les autres champs
    }

    // Configure l'écouteur pour la recherche dynamique
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                // Si le champ est vide, affiche tous les clients
                clientTable.setItems(clientsData);
                return;
            }
            // Sinon effectue la recherche
            performSearch(newValue.trim().toLowerCase());
        });
    }

     //Gère l'action de recherche déclenchée par le bouton ou la touche Entrée
    @FXML
    public void handleSearch(ActionEvent actionEvent) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            clientTable.setItems(clientsData);
            return;
        }
        performSearch(searchText.trim().toLowerCase());
    }

    // Effectue la recherche dans la liste des clients
    private void performSearch(String searchText) {
        try {
            // Filtre les clients dont le CIN, nom ou prénom contient le texte recherché
            ObservableList<Client> filteredList = clientsData.filtered(client ->
                    (client.getCIN() != null && client.getCIN().toLowerCase().contains(searchText)) ||
                            (client.getNom() != null && client.getNom().toLowerCase().contains(searchText)) ||
                            (client.getPrenom() != null && client.getPrenom().toLowerCase().contains(searchText))
            );

            clientTable.setItems(filteredList);

            if (filteredList.isEmpty()) {
                showAlert("Information", "Aucun client trouvé pour : " + searchText, Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la recherche : " + e.getMessage(), Alert.AlertType.ERROR);
            clientTable.setItems(clientsData); // Restaure la liste complète en cas d'erreur
        }
    }

   //Active ou désactive le mode édition du formulaire
    private void setEditMode(boolean editMode) {
        isEditMode = editMode;

        // Boutons désactivés en mode édition
        btnNouveau.setDisable(editMode);
        btnModifier.setDisable(editMode);
        btnSupprimer.setDisable(editMode);

        // Boutons activés seulement en mode édition
        btnEnregistrer.setDisable(!editMode);
        btnAnnuler.setDisable(!editMode);

        // Le CIN est modifiable seulement en création (mode édition + champ vide)
        cinField.setDisable(editMode && !cinField.getText().isEmpty());
    }

     //Gère l'enregistrement d'un client (ajout ou modification)
    @FXML
    private void handleSaveClient() {
        try {
            if (!validateForm()) return; // Valide le formulaire avant enregistrement

            Client client = prepareClientFromForm(); // Crée un client à partir du formulaire
            String cin = client.getCIN();

            if (isEditMode && !cinField.isDisable()) { // Mode ajout
                if (pharmacieService.clientExists(cin)) {
                    showAlert("Erreur", "Un client avec ce CIN existe déjà", Alert.AlertType.ERROR);
                    return;
                }
                pharmacieService.addClient(client); // Ajoute le client
                showAlert("Succès", "Client ajouté avec succès", Alert.AlertType.INFORMATION);
            } else { // Mode modification
                pharmacieService.updateClient(client); // Met à jour le client
                showAlert("Succès", "Client modifié avec succès", Alert.AlertType.INFORMATION);
            }

            refreshData(); // Rafraîchit les données
            setEditMode(false); // Quitte le mode édition
            clearForm(); // Vide le formulaire

        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de l'opération: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    //Crée un objet Client à partir des valeurs du formulaire
    private Client prepareClientFromForm() {
        Client client = new Client();
        client.setCIN(cinField.getText().trim());
        client.setNom(nomField.getText().trim());
        client.setPrenom(prenomField.getText().trim());
        client.setTele(teleField.getText().trim());
        client.setAdresse(adresseField.getText().trim());
        client.setDateInscription(
                Date.from(dateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant())
        );
        return client;
    }

    //Valide les données du formulaire
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Vérification des champs obligatoires
        if (cinField.getText().trim().isEmpty()) {
            errors.append("- Le CIN est obligatoire\n");
        }
        if (nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }
        if (prenomField.getText().trim().isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n" + errors, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    //Gère la création d'un nouveau client (vide le formulaire et active le mode édition)
    @FXML
    private void handleNewClient() {
        clearForm();
        setEditMode(true);
        cinField.setDisable(false); // Active le champ CIN pour la création
    }

     //Gère la modification d'un client existant
    @FXML
    private void handleEditClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            populateForm(selected);
            setEditMode(true);
            cinField.setDisable(true);
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un client", Alert.AlertType.WARNING);
        }
    }

    //Annule les modifications en cours et quitte le mode édition
    @FXML
    private void handleCancel() {
        clearForm();
        setEditMode(false);
    }

    //Remplit le formulaire avec les données d'un client
    private void populateForm(Client client) {
        cinField.setText(client.getCIN());
        nomField.setText(client.getNom());
        prenomField.setText(client.getPrenom());
        teleField.setText(client.getTele());
        adresseField.setText(client.getAdresse());
        dateField.setValue(
                client.getDateInscription().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate()
        );
    }

    //Gère la suppression d'un client
    @FXML
    private void handleDeleteClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Demande confirmation avant suppression
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer le client");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + selected.getNom() + " " + selected.getPrenom() + "?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    pharmacieService.deleteClient(selected.getCIN());
                    refreshData();
                    clearForm();
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un client", Alert.AlertType.WARNING);
        }
    }

    //Charge tous les clients depuis la base de données
    private void loadClients() {
        try {
            List<Client> clients = pharmacieService.getAllClients();
            clientsData.setAll(clients); // Met à jour la liste observable
            clientTable.setItems(clientsData); // Lie à la TableView
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des clients: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    //Rafraîchit les données de la TableView
    @FXML
    private void refreshData() {
        loadClients();
    }

    // Vide tous les champs du formulaire
    @FXML
    private void clearForm() {
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        teleField.clear();
        adresseField.clear();
        dateField.setValue(LocalDate.now());
        clientTable.getSelectionModel().clearSelection();

        // Réactive le champ CIN après annulation
        cinField.setDisable(false);
    }

    //Affiche une boîte de dialogue d'alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //Gère l'ajout d'une facture pour le client sélectionné
    @FXML
    public void handleAddFacture(ActionEvent actionEvent) {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Avertissement", "Veuillez sélectionner un client", Alert.AlertType.WARNING);
            return;
        }

        loadFactureInMainContent(selectedClient); // Charge l'interface de facturation
    }

    //Charge l'interface de facturation dans la zone de contenu principale
    private void loadFactureInMainContent(Client client) {
        try {
            // Charge la vue de facturation
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/facture-view.fxml"));
            Parent factureContent = loader.load();

            // Initialise le contrôleur de facture avec le client
            FactureController factureController = loader.getController();
            factureController.initWithClient(client);

            // Récupère la fenêtre principale
            Stage mainStage = (Stage) clientTable.getScene().getWindow();
            Scene mainScene = mainStage.getScene();

            // Trouve la zone de contenu dans l'interface principale
            StackPane contentPane = (StackPane) mainScene.lookup("#contentPane");

            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(factureContent); // Affiche la facturation

                // Met à jour le titre de la fenêtre
                mainStage.setTitle("PharmaSoft Pro - Facturation pour " + client.getNom());
            } else {
                throw new RuntimeException("Zone de contenu principale non trouvée");
            }

        } catch (IOException e) {
            showAlert("Erreur", "Erreur de chargement de l'interface: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}