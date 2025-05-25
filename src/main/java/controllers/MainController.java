package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    @FXML private StackPane contentPane;
    @FXML private Button clientButton;
    @FXML private Button medicamentButton;
    @FXML private Button factureButton;
    @FXML private Button factureListButton;
    @FXML private Button quitButton;

    @FXML
    private void showClientInterface() {
        loadView("/views/client-view.fxml");
        setActiveButton(clientButton);
    }

    @FXML
    private void showMedicamentInterface() {
        loadView("/views/medicament-view.fxml");
        setActiveButton(medicamentButton);
    }

    @FXML
    private void showFactureInterface() {
        loadView("/views/facture-view.fxml");
        setActiveButton(factureButton);
    }

    @FXML
    private void showFactureList() {
        loadView("/views/facture-list-view.fxml");
        setActiveButton(factureListButton);
    }


    @FXML
    private void quitApplication() {
        // Vérification de null pour plus de sécurité
        if (quitButton != null && quitButton.getScene() != null) {
            Stage stage = (Stage) quitButton.getScene().getWindow();
            stage.close();
        } else {
            // Fallback si la méthode normale échoue
            Platform.exit();
        }
    }


    private void setActiveButton(Button button) {
        // Réinitialiser tous les boutons
        clientButton.getStyleClass().remove("active");
        medicamentButton.getStyleClass().remove("active");
        factureButton.getStyleClass().remove("active");
        factureListButton.getStyleClass().remove("active");

        // Activer le bouton sélectionné
        button.getStyleClass().add("active");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}