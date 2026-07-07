package com.estoque.controller;

import com.estoque.exception.EstoqueInsuficienteException;
import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.ItemVenda;
import com.estoque.model.Venda;
import com.estoque.service.NotaFiscalService;
import com.estoque.service.VendaService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


/**
 * Controller da tela de Vendas: adiciona itens (validando estoque),
 * calcula total e, ao finalizar, grava a venda e gera a Nota Fiscal em PDF.
 */
public class VendaController {

    @FXML private TextField campoCodigoProduto;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoNomeCliente;
    @FXML private ComboBox<String> comboFormaPagamento;

    @FXML private TableView<ItemVenda> tabelaItens;
    @FXML private TableColumn<ItemVenda, String> colCodigo;
    @FXML private TableColumn<ItemVenda, String> colProduto;
    @FXML private TableColumn<ItemVenda, String> colQuantidade;
    @FXML private TableColumn<ItemVenda, String> colPrecoUnitario;
    @FXML private TableColumn<ItemVenda, String> colSubtotal;

    @FXML private Label labelTotal;

    private final VendaService vendaService = new VendaService();
    private final NotaFiscalService notaFiscalService = new NotaFiscalService();

    private Venda vendaAtual = new Venda();

    @FXML
    public void initialize() {
        comboFormaPagamento.setItems(FXCollections.observableArrayList(
                "Dinheiro", "Cartão de Débito", "Cartão de Crédito", "PIX"));

        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getCodigo()));
        colProduto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colPrecoUnitario.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getPrecoUnitario()));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getSubtotal()));

        atualizarTabelaETotal();
    }

    @FXML

    private void onAdicionarItem() {
        try {
            String codigo = campoCodigoProduto.getText().trim();
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());

            vendaService.adicionarItem(vendaAtual, codigo, quantidade);

            campoCodigoProduto.clear();
            campoQuantidade.clear();
            atualizarTabelaETotal();

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
        ItemVenda selecionado = tabelaItens.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum item selecionado", "Selecione um item da venda para remover.");
            return;
        }
        vendaAtual.removerItem(selecionado);
        atualizarTabelaETotal();
    }

    @FXML
    private void onFinalizarVenda() {
        if (vendaAtual.getItens().isEmpty()) {
            AlertUtil.aviso("Venda vazia", "Adicione ao menos um item antes de finalizar a venda.");
            return;
        }
        if (comboFormaPagamento.getValue() == null) {
            AlertUtil.aviso("Forma de pagamento", "Selecione a forma de pagamento.");
            return;
        }

        boolean confirmado = AlertUtil.confirmar("Finalizar venda",
                "Confirmar venda no valor total de R$ " + vendaAtual.getValorTotal() + "?");
        if (!confirmado) return;

        try {
            vendaAtual.setFormaPagamento(comboFormaPagamento.getValue());
            if (!campoNomeCliente.getText().isBlank()) {
                vendaAtual.setNomeCliente(campoNomeCliente.getText().trim());
            }
            vendaAtual.setDataVenda(java.time.LocalDateTime.now());

            vendaService.finalizarVenda(vendaAtual);

            File pdf = notaFiscalService.gerarEAbrir(vendaAtual);

            AlertUtil.info("Venda concluída",
                    "Venda registrada com sucesso!\nNota fiscal gerada em: " + pdf.getAbsolutePath());

            // reinicia a tela para uma nova venda
            vendaAtual = new Venda();
            campoNomeCliente.clear();
            comboFormaPagamento.setValue(null);
            atualizarTabelaETotal();

        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", "Não foi possível registrar a venda.\n" + e.getMessage());
        } catch (IOException e) {
            AlertUtil.erro("Erro ao gerar PDF", "A venda foi registrada, mas houve um erro ao gerar a nota fiscal.\n" + e.getMessage());
        }
    }

    @FXML
    private void onCancelarVenda() {
        if (vendaAtual.getItens().isEmpty()) return;
        boolean confirmado = AlertUtil.confirmar("Cancelar venda", "Deseja descartar todos os itens desta venda?");
        if (confirmado) {
            vendaAtual = new Venda();
            atualizarTabelaETotal();
        }
    }

    private void atualizarTabelaETotal() {
        ObservableList<ItemVenda> lista = FXCollections.observableArrayList(vendaAtual.getItens());
        tabelaItens.setItems(lista);
        labelTotal.setText("Total: R$ " + vendaAtual.getValorTotal());
    }
}