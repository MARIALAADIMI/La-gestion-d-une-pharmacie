package controllers;

import entities.Medicament;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.IPharmacieService;
import services.PharmacieServiceImpl;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TableCell;


public class MedicamentController {

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

    // Références Formulaire
    @FXML private TextField codeBarreField;
    @FXML private TextField nomField;
    @FXML private TextField formeField;
    @FXML private TextField dosageField;
    @FXML private TextField prixField;
    @FXML private TextField stockField;
    @FXML private CheckBox remboursableCheck;
    @FXML private TextField searchField;

    // Références Boutons
    @FXML private Button btnNouveau;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnAnnuler;








    private final IPharmacieService pharmacieService = new PharmacieServiceImpl();
    private final ObservableList<Medicament> medicamentsData = FXCollections.observableArrayList();
    private boolean isEditMode = false;
    private int currentMedId;
    // Références pour la modification de stock
    @FXML private VBox stockModificationPane;
    @FXML private Label currentStockLabel;
    @FXML private Label newStockLabel;
    @FXML private TextField stockAdditionField;
    @FXML private Button btnModifierStock;
    @FXML private Button btnStockFaible;
    @FXML private Button btnResetView;

    private Medicament selectedMedForStock;

    public MedicamentController() throws SQLException {
    }

    @FXML
    public void initialize() {
        configureTableColumns();
        initializeForm();
        loadMedicaments();
        setEditMode(false);
        setupSearchListener();

        // Validation numérique pour la quantité
        stockAdditionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                stockAdditionField.setText(oldValue);
            }
            updateStockCalculation();
        });



       // Ajoutez cet écouteur pour mettre à jour le médicament sélectionné
       medicamentTable.getSelectionModel().selectedItemProperty().addListener(
               (obs, oldSelection, newSelection) -> {
                   if (newSelection != null) {
                       selectedMedForStock = newSelection;
                   }
               });



       //===================================================


    }



    // Affiche le panneau de modification de stock

    @FXML
    private void showStockModificationPane() {
        selectedMedForStock = medicamentTable.getSelectionModel().getSelectedItem();
        if (selectedMedForStock != null) {
            // Affiche le stock actuel du médicament sélectionné
            currentStockLabel.setText(String.valueOf(selectedMedForStock.getStock_dispo()));
            stockAdditionField.clear();
            // Initialise le nouveau stock avec la valeur actuelle
            newStockLabel.setText(String.valueOf(selectedMedForStock.getStock_dispo()));

        } else {
            showAlert("Avertissement", "Veuillez sélectionner un médicament", Alert.AlertType.WARNING);
        }
    }
    // Calcule automatiquement le nouveau stock

    private void updateStockCalculation() {
        if (selectedMedForStock == null) return;

        try {
            int currentStock = selectedMedForStock.getStock_dispo();
            int toAdd = stockAdditionField.getText().isEmpty() ? 0
                    : Integer.parseInt(stockAdditionField.getText());

            int newStock = currentStock + toAdd;
            newStockLabel.setText(String.valueOf(newStock));

            // Change la couleur si stock négatif
            newStockLabel.setStyle(newStock < 0 ? "-fx-text-fill: #f44336;" : "-fx-text-fill: #4CAF50;");
        } catch (NumberFormatException e) {
            newStockLabel.setText("Erreur");
        }
    }
    private void updateSelectedMedicament(Medicament selectedMed) {
        if (selectedMed != null) {
            this.selectedMedForStock = selectedMed;
            currentStockLabel.setText(String.format("%.2f", selectedMed.getStock_dispo()));
            stockAdditionField.clear();
            newStockLabel.setText(String.format("%.2f", selectedMed.getStock_dispo()));
        }
    }

    // Valide la modification du stock

    @FXML
    private void handleStockUpdate() {
        try {
            // Vérifications
            if (selectedMedForStock == null) {
                showAlert("Erreur", "Aucun médicament sélectionné", Alert.AlertType.ERROR);
                return;
            }

            if (stockAdditionField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez entrer une quantité", Alert.AlertType.ERROR);
                return;
            }

            double currentStock = selectedMedForStock.getStock_dispo();
            double quantityToAdd = Double.parseDouble(stockAdditionField.getText());
            double newStock = currentStock + quantityToAdd;

            if (newStock < 0) {
                showAlert("Erreur", "Le stock ne peut pas être négatif", Alert.AlertType.ERROR);
                return;
            }

            // Mise à jour
            selectedMedForStock.setStock_dispo((int) newStock);
            pharmacieService.updateMedicament(selectedMedForStock);


            // Après la mise à jour réussie
            medicamentTable.refresh();
            currentStockLabel.setText(String.valueOf(newStock));
            newStockLabel.setText(String.valueOf(newStock));

            showAlert("Succès",
                    String.format("Stock mis à jour: %.2f (+%.2f)",
                            newStock, quantityToAdd),
                    Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un nombre valide", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur BD", "Échec de la mise à jour: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }

    }
    // Annule la modification
    @FXML
    private void handleCancelStockUpdate() {
        stockModificationPane.setVisible(false);
    }










    private void configureTableColumns() {
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
        medicamentTable.getSortOrder().add(colNom);
    }

    private void initializeForm() {
        clearForm();
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
    public void handleSearch(ActionEvent actionEvent) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) {
            medicamentTable.setItems(medicamentsData);
            return;
        }
        performSearch(searchText.trim().toLowerCase());
    }

    private void performSearch(String searchText) {
        try {
            ObservableList<Medicament> filteredList = medicamentsData.filtered(medicament ->
                    (medicament.getCode_barre() != null && medicament.getCode_barre().toLowerCase().contains(searchText)) ||
                            (medicament.getNom_Med() != null && medicament.getNom_Med().toLowerCase().contains(searchText)) ||
                            (medicament.getForme_pharmaceutique() != null && medicament.getForme_pharmaceutique().toLowerCase().contains(searchText))
            );

            medicamentTable.setItems(filteredList);

            if (filteredList.isEmpty()) {
                showAlert("Information", "Aucun médicament trouvé pour : " + searchText, Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la recherche : " + e.getMessage(), Alert.AlertType.ERROR);
            medicamentTable.setItems(medicamentsData);
        }
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;

        btnNouveau.setDisable(editMode);
        btnModifier.setDisable(editMode);
        btnSupprimer.setDisable(editMode);

        btnEnregistrer.setDisable(!editMode);
        btnAnnuler.setDisable(!editMode);

        codeBarreField.setDisable(editMode && !codeBarreField.getText().isEmpty());
    }

    @FXML
    private void handleSaveMedicament() {
        try {
            if (!validateForm()) return;

            Medicament medicament = prepareMedicamentFromForm();
            String codeBarre = medicament.getCode_barre();

            if (isEditMode && !codeBarreField.isDisable()) {
                if (pharmacieService.medicamentExists(codeBarre)) {
                    showAlert("Erreur", "Un médicament avec ce code barre existe déjà", Alert.AlertType.ERROR);
                    return;
                }
                pharmacieService.addMedicament(medicament);
                showAlert("Succès", "Médicament ajouté avec succès", Alert.AlertType.INFORMATION);
            } else {
                pharmacieService.updateMedicament(medicament);
                showAlert("Succès", "Médicament modifié avec succès", Alert.AlertType.INFORMATION);
            }

            refreshData();
            setEditMode(false);
            clearForm();

        } catch (SQLException e) {
            showAlert("Erreur BD", "Erreur lors de l'opération: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private Medicament prepareMedicamentFromForm() {
        Medicament medicament = new Medicament();
        if (isEditMode) {
            medicament.setId_Med(currentMedId);
        }
        Medicament selected = medicamentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            medicament.setId_Med(selected.getId_Med());
        }
        medicament.setCode_barre(codeBarreField.getText().trim());
        medicament.setNom_Med(nomField.getText().trim());
        medicament.setForme_pharmaceutique(formeField.getText().trim());
        medicament.setDosage(dosageField.getText().trim());
        medicament.setPrix_unitaire(Double.parseDouble(prixField.getText().trim()));
        medicament.setStock_dispo(Integer.parseInt(stockField.getText().trim()));
        medicament.setRemboursable(remboursableCheck.isSelected());
        return medicament;
    }


    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (codeBarreField.getText().trim().isEmpty()) {
            errors.append("- Le code barre est obligatoire\n");
        }
        if (nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }
        if (prixField.getText().trim().isEmpty()) {
            errors.append("- Le prix est obligatoire\n");
        } else {
            try {
                Double.parseDouble(prixField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre valide\n");
            }
        }
        if (stockField.getText().trim().isEmpty()) {
            errors.append("- Le stock est obligatoire\n");
        } else {
            try {
                Integer.parseInt(stockField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("- Le stock doit être un nombre entier\n");
            }
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n" + errors, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    private void handleNewMedicament() {
        clearForm();
        setEditMode(true);
        codeBarreField.setDisable(false);
    }


    @FXML
    private void handleEditMedicament() {
        Medicament selected = medicamentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Stockez l'ID dans un champ caché ou conservez-le dans l'objet
            currentMedId = selected.getId_Med(); // Ajoutez ce champ à votre classe
            populateForm(selected);
            setEditMode(true);
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un médicament", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        setEditMode(false);
    }

    private void populateForm(Medicament medicament) {
        codeBarreField.setText(medicament.getCode_barre());
        nomField.setText(medicament.getNom_Med());
        formeField.setText(medicament.getForme_pharmaceutique());
        dosageField.setText(medicament.getDosage());
        prixField.setText(String.valueOf(medicament.getPrix_unitaire()));
        stockField.setText(String.valueOf(medicament.getStock_dispo()));
        remboursableCheck.setSelected(medicament.isRemboursable());
    }

    @FXML
    private void handleDeleteMedicament() {
        Medicament selected = medicamentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer le médicament");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + selected.getNom_Med() + "?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    pharmacieService.deleteMedicament(selected.getId_Med());
                    refreshData();
                    clearForm();
                } catch (SQLException e) {
                    showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un médicament", Alert.AlertType.WARNING);
        }
    }

    private void loadMedicaments() {
        try {
            List<Medicament> medicaments = pharmacieService.getAllMedicaments();
            medicamentsData.setAll(medicaments);
            medicamentTable.setItems(medicamentsData);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des médicaments: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshData() {
        loadMedicaments();
    }

    @FXML
    private void clearForm() {
        codeBarreField.clear();
        nomField.clear();
        formeField.clear();
        dosageField.clear();
        prixField.clear();
        stockField.clear();
        remboursableCheck.setSelected(false);
        medicamentTable.getSelectionModel().clearSelection();
        codeBarreField.setDisable(false);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



//====================================================================


    @FXML
    private void afficherStockFaible() {
        try {
            if (medicamentsData == null) {
                showAlert("Erreur", "Aucune donnée de médicament chargée", Alert.AlertType.ERROR);
                return;
            }

            ObservableList<Medicament> medicamentsFaibleStock = medicamentsData.filtered(
                    med -> med != null && med.getStock_dispo() < 10
            );

            if (medicamentsFaibleStock.isEmpty()) {
                showAlert("Information", "Aucun médicament en stock faible (<10)", Alert.AlertType.INFORMATION);
                return;
            }

            medicamentTable.setItems(medicamentsFaibleStock);

            // Style des lignes
            medicamentTable.setRowFactory(tv -> new TableRow<Medicament>() {
                @Override
                protected void updateItem(Medicament med, boolean empty) {
                    super.updateItem(med, empty);
                    if (empty || med == null) {
                        setStyle("");
                    } else {
                        setStyle(med.getStock_dispo() < 5 ? "-fx-background-color: #ffdddd;"
                                : "-fx-background-color: #fff3e0;");
                    }
                }
            });

        } catch (Exception e) {
            showAlert("Erreur", "Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

}