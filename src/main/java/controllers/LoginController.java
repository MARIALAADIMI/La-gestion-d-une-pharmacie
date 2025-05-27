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
    // Déclaration des éléments FXML (liés au fichier login.fxml)
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheck;
    @FXML private Button loginButton;

    // Méthode appelée lors du clic sur le bouton de connexion
    @FXML
    private void handleLogin() {
        // Récupération des valeurs des champs
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation des champs obligatoires
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur de connexion",
                    "Veuillez saisir un nom d'utilisateur et un mot de passe",
                    Alert.AlertType.ERROR);
            return;
        }

        // Tentative d'authentification
        if (authenticate(username, password)) {
            try {
                // Chargement de l'interface principale
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
                Parent root = loader.load();

                // Configuration de la nouvelle fenêtre principale
                Stage mainStage = new Stage();
                ApplicationJavaFX.configureStage(mainStage); // Configuration du style
                mainStage.setScene(new Scene(root));
                mainStage.setMaximized(true); // Mode plein écran

                // Fermeture de la fenêtre de login actuelle
                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

                // Affichage de la fenêtre principale
                mainStage.show();

            } catch (IOException e) {
                showAlert("Erreur",
                        "Impossible de charger l'interface principale",
                        Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } else {
            // Message en cas d'échec d'authentification
            showAlert("Échec de connexion",
                    "Identifiants incorrects",
                    Alert.AlertType.ERROR);
        }
    }

    // Méthode d'authentification (simulée dans cet exemple)
    private boolean authenticate(String username, String password) {
        // TODO: Remplacer par une vraie logique d'authentification
        // Actuellement, seuls admin/admin123 sont acceptés
        return "admin".equals(username) && "admin123".equals(password);
    }

    // Méthode appelée lors du clic sur "Mot de passe oublié"
    @FXML
    private void handleForgotPassword() {
        showAlert("Mot de passe oublié",
                "Contactez l'administrateur",
                Alert.AlertType.INFORMATION);
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Pas de texte d'en-tête
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY); // Style minimal
        alert.showAndWait(); // Affichage modal
    }
}