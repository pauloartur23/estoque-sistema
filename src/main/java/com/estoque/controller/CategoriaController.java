package com.estoque.controller;

import com.estoque.model.Categoria;
import com.estoque.service.CategoriaService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class CategoriaController {

    @FXML private TextField campoNome;
    @FXML private TextArea campoDescricao;

    @FXML private TableView<Categoria> tabelaCategorias;
    @FXML private TableColumn<Categoria, String> colNome;
    @FXML private TableColumn<Categoria, String> colDescricao;

    private final CategoriaService categoriaService = new CategoriaService();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNome()));
        colDescricao.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDescricao() == null ? "-" : d.getValue().getDescricao()));

        carregarTabela();

        tabelaCategorias.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoNome.setText(novo.getNome());
                campoDescricao.setText(novo.getDescricao());
            }
        });
    }

    @FXML
    private void onSalvar() {
        try {
            Categoria selecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
            if (selecionada != null) {
                selecionada.setNome(campoNome.getText().trim());
                selecionada.setDescricao(campoDescricao.getText());
                categoriaService.atualizar(selecionada);
                AlertUtil.info("Sucesso", "Categoria atualizada com sucesso!");
            } else {
                categoriaService.cadastrar(campoNome.getText().trim(), campoDescricao.getText());
                AlertUtil.info("Sucesso", "Categoria cadastrada com sucesso!");
            }
            limparCampos();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            AlertUtil.aviso("Campo inválido", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onRemover() {
        Categoria selecionada = tabelaCategorias.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            AlertUtil.aviso("Nenhuma categoria selecionada", "Selecione uma categoria na tabela para remover.");
            return;
        }
        boolean confirmado = AlertUtil.confirmar("Confirmar remoção",
                "Deseja realmente remover a categoria '" + selecionada.getNome() + "'?");
        if (!confirmado) return;

        try {
            categoriaService.remover(selecionada.getId());
            limparCampos();
            carregarTabela();
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onNovo() {
        limparCampos();
        tabelaCategorias.getSelectionModel().clearSelection();
    }

    private void carregarTabela() {
        try {
            ObservableList<Categoria> lista = FXCollections.observableArrayList(categoriaService.listarTodas());
            tabelaCategorias.setItems(lista);
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    private void limparCampos() {
        campoNome.clear();
        campoDescricao.clear();
    }
}