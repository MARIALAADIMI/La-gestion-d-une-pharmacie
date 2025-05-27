package presentation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// Classe principale JavaFX qui lance l'application
public class ApplicationJavaFX extends Application {

    // Point d'entrée principal de l'application Java
    public static void main(String[] args) {
        launch(args);
    }

    // Méthode appelée automatiquement après launch() pour initialiser la fenêtre principale
    @Override
    public void start(Stage primaryStage) {
        try {
            // Chargement du fichier FXML (interface graphique) depuis le dossier resources/views
            URL fxmlUrl = getClass().getResource("/views/login-view.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable");
            }

            configureStage(primaryStage);
            // Chargement du contenu graphique défini dans le FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Création d'une scène avec le contenu chargé
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            // Affiche une alerte en cas de problème lors du chargement du fichier FXML
            showErrorAlert("Erreur de chargement", e.getMessage());
        } catch (Exception e) {
            // Affiche une alerte pour toute autre erreur système imprévue
            showErrorAlert("Erreur système", e.getMessage());
        }
    }

    // Méthode pour configurer les propriétés de la fenêtre Stage (titre, icône)
    public static void configureStage(Stage stage) {
        try {
            // Charge l'icône de l'application depuis le dossier resources/images
            InputStream iconStream = ApplicationJavaFX.class.getResourceAsStream("/images/icons8-pharmacie-48.png");
            if (iconStream != null) {
                Image appIcon = new Image(iconStream);
                stage.getIcons().clear();
                stage.getIcons().add(appIcon);
            }
            stage.setTitle("PharmaSoft Pro"); // Définit le titre de la fenêtre
        } catch (Exception e) {
            // En cas d'erreur de chargement d'icône, affiche un message dans la console
            System.err.println("Erreur de chargement de l'icône: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour afficher une fenêtre d'alerte d'erreur à l'utilisateur
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
