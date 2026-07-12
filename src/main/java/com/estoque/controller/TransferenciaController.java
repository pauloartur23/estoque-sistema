package com.estoque.controller;

import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.LocalEstoque;
import com.estoque.model.Produto;
import com.estoque.model.Transferencia;
import com.estoque.service.LocalEstoqueService;
import com.estoque.service.ProdutoService;
import com.estoque.service.TransferenciaService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class TransferenciaController {

    @FXML private TextField campoCodigoProduto;
    @FXML private ComboBox<LocalEstoque> comboOrigem;
    @FXML private ComboBox<LocalEstoque> comboDestino;
    @FXML private TextField campoQuantidade;
    @FXML private TextArea campoObservacoes;

    @FXML private TableView<Transferencia> tabelaTransferencias;
    @FXML private TableColumn<Transferencia, String> colProduto;
    @FXML private TableColumn<Transferencia, String> colOrigem;
    @FXML private TableColumn<Transferencia, String> colDestino;
    @FXML private TableColumn<Transferencia, String> colQuantidade;
    @FXML private TableColumn<Transferencia, String> colData;

    private final TransferenciaService transferenciaService = new TransferenciaService();
    private final ProdutoService produtoService = new ProdutoService();
    private final LocalEstoqueService localEstoqueService = new LocalEstoqueService();

    @FXML
    public void initialize() {
        try {
            ObservableList<LocalEstoque> locais = FXCollections.observableArrayList(localEstoqueService.listarTodos());
            comboOrigem.setItems(locais);
            comboDestino.setItems(locais);
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }

        colProduto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getNome()));
        colOrigem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrigem().getNome()));
        colDestino.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDestino().getNome()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colData.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDataTransferencia() != null ? d.getValue().getDataTransferencia().toString() : "-"));

        carregarTabela();
    }

    @FXML
    private void onRegistrarTransferencia() {
        try {
            String codigo = campoCodigoProduto.getText().trim();
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());

            if (comboOrigem.getValue() == null || comboDestino.getValue() == null) {
                AlertUtil.aviso("Locais obrigatórios", "Selecione o local de origem e o de destino.");
                return;
            }

            Produto produto = produtoService.buscarPorCodigo(codigo);
            transferenciaService.registrar(produto, comboOrigem.getValue(), comboDestino.getValue(),
                    quantidade, campoObservacoes.getText());

            AlertUtil.info("Sucesso", "Transferência registrada com sucesso!");

            campoCodigoProduto.clear();
            campoQuantidade.clear();
            campoObservacoes.clear();
            comboOrigem.setValue(null);
            comboDestino.setValue(null);
            carregarTabela();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Informe uma quantidade numérica válida.");
        } catch (ProdutoNaoEncontradoException e) {
            AlertUtil.aviso("Produto não encontrado", e.getMessage());
        } catch (IllegalArgumentException e) {
            AlertUtil.aviso("Dados inválidos", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    private void carregarTabela() {
        try {
            ObservableList<Transferencia> lista = FXCollections.observableArrayList(transferenciaService.listarTodas());
            tabelaTransferencias.setItems(lista);
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }
}