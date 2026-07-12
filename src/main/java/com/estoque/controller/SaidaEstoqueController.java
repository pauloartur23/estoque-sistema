package com.estoque.controller;

import com.estoque.exception.EstoqueInsuficienteException;
import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.Produto;
import com.estoque.model.SaidaEstoque;
import com.estoque.model.SaidaItem;
import com.estoque.service.ProdutoService;
import com.estoque.service.SaidaEstoqueService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class SaidaEstoqueController {

    @FXML private ComboBox<String> comboTipoSaida;
    @FXML private TextArea campoObservacoes;

    @FXML private TextField campoCodigoProduto;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoMotivo;

    @FXML private TableView<SaidaItem> tabelaItens;
    @FXML private TableColumn<SaidaItem, String> colCodigo;
    @FXML private TableColumn<SaidaItem, String> colProduto;
    @FXML private TableColumn<SaidaItem, String> colQuantidade;
    @FXML private TableColumn<SaidaItem, String> colMotivo;

    private final SaidaEstoqueService saidaService = new SaidaEstoqueService();
    private final ProdutoService produtoService = new ProdutoService();

    private SaidaEstoque saidaAtual = new SaidaEstoque();

    @FXML
    public void initialize() {
        comboTipoSaida.setItems(FXCollections.observableArrayList(
                "Avaria", "Perda", "Uso Interno", "Devolução ao Fornecedor"));

        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getCodigo()));
        colProduto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getMotivo() == null ? "-" : d.getValue().getMotivo()));

        atualizarTabela();
    }

    @FXML
    private void onAdicionarItem() {
        try {
            String codigo = campoCodigoProduto.getText().trim();
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());

            Produto produto = produtoService.buscarPorCodigo(codigo);
            saidaService.adicionarItem(saidaAtual, produto, quantidade, campoMotivo.getText());

            campoCodigoProduto.clear();
            campoQuantidade.clear();
            campoMotivo.clear();
            atualizarTabela();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Informe uma quantidade numérica válida.");
        } catch (ProdutoNaoEncontradoException e) {
            AlertUtil.aviso("Produto não encontrado", e.getMessage());
        } catch (EstoqueInsuficienteException e) {
            AlertUtil.aviso("Estoque insuficiente", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onRemoverItem() {
        SaidaItem selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum item selecionado", "Selecione um item para remover.");
            return;
        }
        saidaAtual.removerItem(selecionado);
        atualizarTabela();
    }

    @FXML
    private void onSalvarSaida() {
        try {
            saidaAtual.setTipoSaida(comboTipoSaida.getValue());
            saidaAtual.setObservacoes(campoObservacoes.getText());

            saidaService.finalizarSaida(saidaAtual);

            AlertUtil.info("Sucesso", "Saída de estoque registrada com sucesso!");

            saidaAtual = new SaidaEstoque();
            comboTipoSaida.setValue(null);
            campoObservacoes.clear();
            atualizarTabela();

        } catch (IllegalStateException e) {
            AlertUtil.aviso("Dados incompletos", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        saidaAtual = new SaidaEstoque();
        comboTipoSaida.setValue(null);
        campoObservacoes.clear();
        atualizarTabela();
    }

    private void atualizarTabela() {
        ObservableList<SaidaItem> lista = FXCollections.observableArrayList(saidaAtual.getItens());
        tabelaItens.setItems(lista);
    }
}