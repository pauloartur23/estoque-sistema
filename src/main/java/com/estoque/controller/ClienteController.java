package com.estoque.controller;

import com.estoque.model.Cliente;
import com.estoque.service.ClienteService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class ClienteController {

    @FXML private TextField campoNome;
    @FXML private TextField campoDocumento;
    @FXML private TextField campoTelefone;
    @FXML private TextField campoEmail;
    @FXML private TextField campoEndereco;
    @FXML private TextField campoBusca;

    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colDocumento;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colEmail;

    private final ClienteService clienteService = new ClienteService();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNome()));
        colDocumento.setCellValueFactory(d -> new SimpleStringProperty(vazioSeNull(d.getValue().getDocumento())));
        colTelefone.setCellValueFactory(d -> new SimpleStringProperty(vazioSeNull(d.getValue().getTelefone())));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(vazioSeNull(d.getValue().getEmail())));

        carregarTabela();

        tabelaClientes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
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
            Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                selecionado.setNome(campoNome.getText().trim());
                selecionado.setDocumento(campoDocumento.getText());
                selecionado.setTelefone(campoTelefone.getText());
                selecionado.setEmail(campoEmail.getText());
                selecionado.setEndereco(campoEndereco.getText());
                clienteService.atualizar(selecionado);
                AlertUtil.info("Sucesso", "Cliente atualizado com sucesso!");
            } else {
                clienteService.cadastrar(campoNome.getText().trim(), campoDocumento.getText(),
                        campoTelefone.getText(), campoEmail.getText(), campoEndereco.getText());
                AlertUtil.info("Sucesso", "Cliente cadastrado com sucesso!");
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
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum cliente selecionado", "Selecione um cliente na tabela para remover.");
            return;
        }
        boolean confirmado = AlertUtil.confirmar("Confirmar remoção",
                "Deseja realmente remover o cliente '" + selecionado.getNome() + "'?");
        if (!confirmado) return;

        try {
            clienteService.remover(selecionado.getId());
            limparCampos();
            carregarTabela();
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onNovo() {
        limparCampos();
        tabelaClientes.getSelectionModel().clearSelection();
    }

    @FXML
    private void onBuscar() {
        carregarTabela();
    }

    private void carregarTabela() {
        try {
            ObservableList<Cliente> lista = FXCollections.observableArrayList(
                    clienteService.buscar(campoBusca.getText()));
            tabelaClientes.setItems(lista);
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