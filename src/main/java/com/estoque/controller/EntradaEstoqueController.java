package com.estoque.controller;

import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.EntradaEstoque;
import com.estoque.model.EntradaItem;
import com.estoque.model.Fornecedor;
import com.estoque.model.Produto;
import com.estoque.service.EntradaEstoqueService;
import com.estoque.service.FornecedorService;
import com.estoque.service.ProdutoService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.SQLException;

public class EntradaEstoqueController {

    @FXML private ComboBox<Fornecedor> comboFornecedor;
    @FXML private TextField campoNumeroNota;
    @FXML private TextArea campoObservacoes;

    @FXML private TextField campoCodigoProduto;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoCustoUnitario;

    @FXML private TableView<EntradaItem> tabelaItens;
    @FXML private TableColumn<EntradaItem, String> colCodigo;
    @FXML private TableColumn<EntradaItem, String> colProduto;
    @FXML private TableColumn<EntradaItem, String> colQuantidade;
    @FXML private TableColumn<EntradaItem, String> colCusto;
    @FXML private TableColumn<EntradaItem, String> colSubtotal;

    @FXML private Label labelTotal;

    private final EntradaEstoqueService entradaService = new EntradaEstoqueService();
    private final ProdutoService produtoService = new ProdutoService();
    private final FornecedorService fornecedorService = new FornecedorService();

    private EntradaEstoque entradaAtual = new EntradaEstoque();

    @FXML
    public void initialize() {
        try {
            comboFornecedor.setItems(FXCollections.observableArrayList(fornecedorService.listarTodos()));
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }

        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getCodigo()));
        colProduto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colCusto.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getCustoUnitario()));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getSubtotal()));

        atualizarTabelaETotal();
    }

    @FXML
    private void onAdicionarItem() {
        try {
            String codigo = campoCodigoProduto.getText().trim();
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());
            BigDecimal custo = new BigDecimal(campoCustoUnitario.getText().trim().replace(",", "."));

            Produto produto = produtoService.buscarPorCodigo(codigo);
            entradaService.adicionarItem(entradaAtual, produto, quantidade, custo);

            campoCodigoProduto.clear();
            campoQuantidade.clear();
            campoCustoUnitario.clear();
            atualizarTabelaETotal();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Verifique a quantidade e o custo informados.");
        } catch (ProdutoNaoEncontradoException e) {
            AlertUtil.aviso("Produto não encontrado", e.getMessage());
        } catch (IllegalArgumentException e) {
            AlertUtil.aviso("Campo inválido", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onRemoverItem() {
        EntradaItem selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum item selecionado", "Selecione um item para remover.");
            return;
        }
        entradaAtual.removerItem(selecionado);
        atualizarTabelaETotal();
    }

    @FXML
    private void onSalvarEntrada() {
        try {
            entradaAtual.setFornecedor(comboFornecedor.getValue());
            entradaAtual.setNumeroNota(campoNumeroNota.getText());
            entradaAtual.setObservacoes(campoObservacoes.getText());

            entradaService.finalizarEntrada(entradaAtual);

            AlertUtil.info("Sucesso", "Entrada de estoque registrada com sucesso!");

            entradaAtual = new EntradaEstoque();
            comboFornecedor.setValue(null);
            campoNumeroNota.clear();
            campoObservacoes.clear();
            atualizarTabelaETotal();

        } catch (IllegalStateException e) {
            AlertUtil.aviso("Dados incompletos", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        entradaAtual = new EntradaEstoque();
        comboFornecedor.setValue(null);
        campoNumeroNota.clear();
        campoObservacoes.clear();
        atualizarTabelaETotal();
    }

    private void atualizarTabelaETotal() {
        ObservableList<EntradaItem> lista = FXCollections.observableArrayList(entradaAtual.getItens());
        tabelaItens.setItems(lista);
        labelTotal.setText("Total: R$ " + entradaAtual.getValorTotal());
    }
}