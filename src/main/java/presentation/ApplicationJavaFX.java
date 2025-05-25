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

public class ApplicationJavaFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            URL fxmlUrl = getClass().getResource("/views/login-view.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable");
            }

            configureStage(primaryStage);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true); // Plein écran
            primaryStage.show();

        } catch (IOException e) {
            showErrorAlert("Erreur de chargement", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur système", e.getMessage());
        }
    }

    public static void configureStage(Stage stage) {
        try {
            InputStream iconStream = ApplicationJavaFX.class.getResourceAsStream("/images/icons8-pharmacie-48.png");
            if (iconStream != null) {
                Image appIcon = new Image(iconStream);
                stage.getIcons().clear();
                stage.getIcons().add(appIcon);
            }
            stage.setTitle("PharmaSoft Pro");
        } catch (Exception e) {
            System.err.println("Erreur de chargement de l'icône: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}