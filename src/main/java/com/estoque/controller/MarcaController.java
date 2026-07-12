package com.estoque.controller;

import com.estoque.model.Marca;
import com.estoque.service.MarcaService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class MarcaController {

    @FXML private TextField campoNome;

    @FXML private TableView<Marca> tabelaMarcas;
    @FXML private TableColumn<Marca, String> colNome;

    private final MarcaService marcaService = new MarcaService();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNome()));
        carregarTabela();

        tabelaMarcas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) campoNome.setText(novo.getNome());
        });
    }

    @FXML
    private void onSalvar() {
        try {
            Marca selecionada = tabelaMarcas.getSelectionModel().getSelectedItem();
            if (selecionada != null) {
                selecionada.setNome(campoNome.getText().trim());
                marcaService.atualizar(selecionada);
                AlertUtil.info("Sucesso", "Marca atualizada com sucesso!");
            } else {
                marcaService.cadastrar(campoNome.getText().trim());
                AlertUtil.info("Sucesso", "Marca cadastrada com sucesso!");
            }
            campoNome.clear();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            AlertUtil.aviso("Campo inválido", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onRemover() {
        Marca selecionada = tabelaMarcas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            AlertUtil.aviso("Nenhuma marca selecionada", "Selecione uma marca na tabela para remover.");
            return;
        }
        boolean confirmado = AlertUtil.confirmar("Confirmar remoção",
                "Deseja realmente remover a marca '" + selecionada.getNome() + "'?");
        if (!confirmado) return;

        try {
            marcaService.remover(selecionada.getId());
            campoNome.clear();
            carregarTabela();
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onNovo() {
        campoNome.clear();
        tabelaMarcas.getSelectionModel().clearSelection();
    }

    private void carregarTabela() {
        try {
            ObservableList<Marca> lista = FXCollections.observableArrayList(marcaService.listarTodas());
            tabelaMarcas.setItems(lista);
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }
}