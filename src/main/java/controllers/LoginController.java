package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import presentation.ApplicationJavaFX;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;
    @FXML private Button loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur de connexion",
                    "Veuillez saisir un nom d'utilisateur et un mot de passe",
                    Alert.AlertType.ERROR);
            return;
        }

        if (authenticate(username, password)) {
            try {
                // Charger l'interface principale
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
                Parent root = loader.load();

                // Configurer la nouvelle fenêtre
                Stage mainStage = new Stage();
                ApplicationJavaFX.configureStage(mainStage);
                mainStage.setScene(new Scene(root));
                mainStage.setMaximized(true); // Plein écran

                // Fermer la fenêtre de login
                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

                // Afficher la fenêtre principale
                mainStage.show();

            } catch (IOException e) {
                showAlert("Erreur",
                        "Impossible de charger l'interface principale",
                        Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            showAlert("Échec de connexion",
                    "Identifiants incorrects",
                    Alert.AlertType.ERROR);
        }
    }

    private boolean authenticate(String username, String password) {
        // Simulation d'authentification
        return "admin".equals(username) && "admin123".equals(password);
    }

    @FXML
    private void handleForgotPassword() {
        showAlert("Mot de passe oublié",
                "Contactez l'administrateur",
                Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }
}