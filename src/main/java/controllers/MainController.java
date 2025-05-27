package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

//Contrôleur principal de l'application qui gère la navigation entre les différentes vues
public class MainController {
    // Références aux éléments FXML
    @FXML private StackPane contentPane;
    @FXML private Button clientButton;
    @FXML private Button medicamentButton;
    @FXML private Button factureButton;
    @FXML private Button factureListButton;
    @FXML private Button quitButton;

    //Affiche l'interface client dans le contentPane
    @FXML
    private void showClientInterface() {
        loadView("/views/client-view.fxml");
        setActiveButton(clientButton);
    }

    // Affiche l'interface médicament dans le contentPane
    @FXML
    private void showMedicamentInterface() {
        loadView("/views/medicament-view.fxml");
        setActiveButton(medicamentButton);
    }

    //Affiche l'interface facture dans le contentPane
    @FXML
    private void showFactureInterface() {
        loadView("/views/facture-view.fxml");
        setActiveButton(factureButton);
    }

    //Affiche la liste des factures dans le contentPane
    @FXML
    private void showFactureList() {
        loadView("/views/facture-list-view.fxml");
        setActiveButton(factureListButton);
    }

    //Quitte l'application
    @FXML
    private void quitApplication() {
        // Vérification de null pour plus de sécurité
        if (quitButton != null && quitButton.getScene() != null) {
            // Ferme la fenêtre principale
            Stage stage = (Stage) quitButton.getScene().getWindow();
            stage.close();
        } else {
            // Fallback si la méthode normale échoue (fermeture de la plateforme JavaFX)
            Platform.exit();
        }
    }

    //Met en surbrillance le bouton actif et désactive les autres
    private void setActiveButton(Button button) {
        // Réinitialiser tous les boutons (supprime la classe CSS 'active')
        clientButton.getStyleClass().remove("active");
        medicamentButton.getStyleClass().remove("active");
        factureButton.getStyleClass().remove("active");
        factureListButton.getStyleClass().remove("active");

        // Activer le bouton sélectionné (ajoute la classe CSS 'active')
        button.getStyleClass().add("active");
    }

    //Charge une vue FXML dans le contentPane
    private void loadView(String fxmlPath) {
        try {
            // Charge la vue FXML
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            // Efface le contenu actuel et ajoute la nouvelle vue
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace(); // Gestion simple des erreurs (à améliorer en production)
        }
    }
}