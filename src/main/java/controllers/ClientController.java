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
    // Références TableView
    @FXML private TableView<Client> clientTable;
    @FXML private TableColumn<Client, String> colCIN;
    @FXML private TableColumn<Client, String> colNom;
    @FXML private TableColumn<Client, String> colPrenom;
    @FXML private TableColumn<Client, String> colTele;
    @FXML private TableColumn<Client, String> colAdresse;
    @FXML private TableColumn<Client, String> colDate;

    // Références Formulaire
    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField teleField;
    @FXML private TextArea adresseField;
    @FXML private DatePicker dateField;
    @FXML private TextField searchField;


    // Références Boutons
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

    @FXML
    public void initialize() {
        configureTableColumns();
        initializeForm();
        loadClients();
        setEditMode(false);
        // Ajout du listener pour la recherche dynamique
        setupSearchListener();
    }

    private void configureTableColumns() {
        colCIN.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTele.setCellValueFactory(new PropertyValueFactory<>("tele"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colDate.setCellValueFactory(cellData -> {
            SimpleStringProperty property = new SimpleStringProperty();
            Date date = cellData.getValue().getDateInscription();
            property.setValue(new SimpleDateFormat("dd/MM/yyyy").format(date));
            return property;
        });
        clientTable.getSortOrder().add(colNom);
    }

    private void initializeForm() {
        dateField.setValue(LocalDate.now());
        clearForm();
    }



    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                clientTable.setItems(clientsData);
                return;
            }
            performSearch(newValue.trim().toLowerCase());
        });
    }

    @FXML
    public void handleSearch(ActionEvent actionEvent) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            clientTable.setItems(clientsData);
            return;
        }
        performSearch(searchText.trim().toLowerCase());
    }

    private void performSearch(String searchText) {
        try {
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

    @FXML
    private void handleSaveClient() {
        try {
            if (!validateForm()) return;

            Client client = prepareClientFromForm();
            String cin = client.getCIN();

            if (isEditMode && !cinField.isDisable()) { // Mode ajout
                if (pharmacieService.clientExists(cin)) {
                    showAlert("Erreur", "Un client avec ce CIN existe déjà", Alert.AlertType.ERROR);
                    return;
                }
                pharmacieService.addClient(client);
                showAlert("Succès", "Client ajouté avec succès", Alert.AlertType.INFORMATION);
            } else { // Mode modification
                pharmacieService.updateClient(client);
                showAlert("Succès", "Client modifié avec succès", Alert.AlertType.INFORMATION);
            }

            refreshData();
            setEditMode(false);
            clearForm();

        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de l'opération: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

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

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

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

    @FXML
    private void handleNewClient() {
        clearForm();
        setEditMode(true);
        cinField.setDisable(false);
    }

    @FXML
    private void handleEditClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            populateForm(selected);
            setEditMode(true);
            cinField.setDisable(true); // Empêche la modification du CIN
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un client", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        setEditMode(false);
    }


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

    @FXML
    private void handleDeleteClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
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

    private void loadClients() {
        try {
            List<Client> clients = pharmacieService.getAllClients();
            clientsData.setAll(clients);
            clientTable.setItems(clientsData);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des clients: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshData() {
        loadClients();
    }

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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    //=============================================================================== add facture
    @FXML
    public void handleAddFacture(ActionEvent actionEvent) {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Avertissement", "Veuillez sélectionner un client", Alert.AlertType.WARNING);
            return;
        }

        loadFactureInMainContent(selectedClient);
    }

    private void loadFactureInMainContent(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/facture-view.fxml"));
            Parent factureContent = loader.load();

            // Initialisation du contrôleur Facture
            FactureController factureController = loader.getController();
            factureController.initWithClient(client);

            // Récupération du MainController
            Stage mainStage = (Stage) clientTable.getScene().getWindow();
            Scene mainScene = mainStage.getScene();

            // Recherche du contentPane dans la hiérarchie
            StackPane contentPane = (StackPane) mainScene.lookup("#contentPane"); // ID doit correspondre à votre FXML

            if (contentPane != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(factureContent);

                // Mise à jour du titre de la fenêtre principale
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