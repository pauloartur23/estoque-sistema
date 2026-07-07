package com.estoque.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller da tela principal (menu). Abre as telas de Produtos e Vendas
 * em janelas (Stage) separadas.
 */
public class TelaPrincipalController {

    @FXML
    private void abrirTelaProdutos() {
        abrirJanela("/fxml/TelaProduto.fxml", "Gerenciamento de Produtos", 950, 600);
    }

    @FXML
    private void abrirTelaVendas() {
        abrirJanela("/fxml/TelaVenda.fxml", "Registrar Venda", 900, 600);
    }

    private void abrirJanela(String caminhoFxml, String titulo, int largura, int altura) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(caminhoFxml)));
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root, largura, altura);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
