package com.estoque;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Classe principal — inicializa a aplicação JavaFX carregando a tela principal.
 */
public class Main extends Application {

    @Override
    public void start(Stage stagePrincipal) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxml/TelaPrincipal.fxml")));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        stagePrincipal.setTitle("Sistema de Gerenciamento de Estoque");
        stagePrincipal.setScene(scene);
        stagePrincipal.setMinWidth(800);
        stagePrincipal.setMinHeight(550);
        stagePrincipal.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
