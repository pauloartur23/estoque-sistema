package com.estoque.controller;

import com.estoque.model.Fornecedor;
import com.estoque.service.FornecedorService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class FornecedorController {

    @FXML private TextField campoNome;
    @FXML private TextField campoDocumento;
    @FXML private TextField campoTelefone;
    @FXML private TextField campoEmail;
    @FXML private TextField campoEndereco;
    @FXML private TextField campoBusca;

    @FXML private TableView<Fornecedor> tabelaFornecedores;
    @FXML private TableColumn<Fornecedor, String> colNome;
    @FXML private TableColumn<Fornecedor, String> colDocumento;
    @FXML private TableColumn<Fornecedor, String> colTelefone;
    @FXML private TableColumn<Fornecedor, String> colEmail;

    private final FornecedorService fornecedorService = new FornecedorService();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNome()));
        colDocumento.setCellValueFactory(d -> new SimpleStringProperty(vazioSeNull(d.getValue().getDocumento())));
        colTelefone.setCellValueFactory(d -> new SimpleStringProperty(vazioSeNull(d.getValue().getTelefone())));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(vazioSeNull(d.getValue().getEmail())));

        carregarTabela();

        tabelaFornecedores.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoNome.setText(novo.getNome());
                campoDocumento.setText(novo.getDocumento());
                campoTelefone.setText(novo.getTelefone());
                campoEmail.setText(novo.getEmail());
                campoEndereco.setText(novo.getEndereco());
            }
        });
    }

    @FXML
    private void onSalvar() {
        try {
            Fornecedor selecionado = tabelaFornecedores.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                selecionado.setNome(campoNome.getText().trim());
                selecionado.setDocumento(campoDocumento.getText());
                selecionado.setTelefone(campoTelefone.getText());
                selecionado.setEmail(campoEmail.getText());
                selecionado.setEndereco(campoEndereco.getText());
                fornecedorService.atualizar(selecionado);
                AlertUtil.info("Sucesso", "Fornecedor atualizado com sucesso!");
            } else {
                fornecedorService.cadastrar(campoNome.getText().trim(), campoDocumento.getText(),
                        campoTelefone.getText(), campoEmail.getText(), campoEndereco.getText());
                AlertUtil.info("Sucesso", "Fornecedor cadastrado com sucesso!");
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
        Fornecedor selecionado = tabelaFornecedores.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum fornecedor selecionado", "Selecione um fornecedor na tabela para remover.");
            return;
        }
        boolean confirmado = AlertUtil.confirmar("Confirmar remoção",
                "Deseja realmente remover o fornecedor '" + selecionado.getNome() + "'?");
        if (!confirmado) return;

        try {
            fornecedorService.remover(selecionado.getId());
            limparCampos();
            carregarTabela();
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onNovo() {
        limparCampos();
        tabelaFornecedores.getSelectionModel().clearSelection();
    }

    @FXML
    private void onBuscar() {
        carregarTabela();
    }

    private void carregarTabela() {
        try {
            ObservableList<Fornecedor> lista = FXCollections.observableArrayList(
                    fornecedorService.buscar(campoBusca.getText()));
            tabelaFornecedores.setItems(lista);
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    private void limparCampos() {
        campoNome.clear();
        campoDocumento.clear();
        campoTelefone.clear();
        campoEmail.clear();
        campoEndereco.clear();
    }

    private String vazioSeNull(String texto) {
        return texto == null ? "-" : texto;
    }
}