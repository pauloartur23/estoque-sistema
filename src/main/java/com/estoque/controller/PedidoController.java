package com.estoque.controller;

import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.Fornecedor;
import com.estoque.model.Pedido;
import com.estoque.model.PedidoItem;
import com.estoque.model.Produto;
import com.estoque.service.FornecedorService;
import com.estoque.service.PedidoService;
import com.estoque.service.ProdutoService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.SQLException;

public class PedidoController {

    @FXML private ComboBox<Fornecedor> comboFornecedor;
    @FXML private TextArea campoObservacoes;

    @FXML private TextField campoCodigoProduto;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoPrecoUnitario;

    @FXML private TableView<PedidoItem> tabelaItensNovoPedido;
    @FXML private TableColumn<PedidoItem, String> colCodigoItem;
    @FXML private TableColumn<PedidoItem, String> colProdutoItem;
    @FXML private TableColumn<PedidoItem, String> colQuantidadeItem;
    @FXML private TableColumn<PedidoItem, String> colPrecoItem;
    @FXML private TableColumn<PedidoItem, String> colSubtotalItem;
    @FXML private Label labelTotalNovoPedido;

    @FXML private TableView<Pedido> tabelaPedidos;
    @FXML private TableColumn<Pedido, String> colFornecedor;
    @FXML private TableColumn<Pedido, String> colData;
    @FXML private TableColumn<Pedido, String> colStatus;
    @FXML private TableColumn<Pedido, String> colValorTotal;

    private final PedidoService pedidoService = new PedidoService();
    private final ProdutoService produtoService = new ProdutoService();
    private final FornecedorService fornecedorService = new FornecedorService();

    private Pedido pedidoAtual = new Pedido();

    @FXML
    public void initialize() {
        try {
            comboFornecedor.setItems(FXCollections.observableArrayList(fornecedorService.listarTodos()));
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }

        colCodigoItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getCodigo()));
        colProdutoItem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getNome()));
        colQuantidadeItem.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colPrecoItem.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getPrecoUnitario()));
        colSubtotalItem.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getSubtotal()));

        colFornecedor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFornecedor().getNome()));
        colData.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDataPedido() != null ? d.getValue().getDataPedido().toString() : "-"));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().name()));
        colValorTotal.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getValorTotal()));

        atualizarTabelaItensETotal();
        carregarTabelaPedidos();
    }

    @FXML
    private void onAdicionarItem() {
        try {
            String codigo = campoCodigoProduto.getText().trim();
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());
            BigDecimal preco = new BigDecimal(campoPrecoUnitario.getText().trim().replace(",", "."));

            Produto produto = produtoService.buscarPorCodigo(codigo);
            pedidoService.adicionarItem(pedidoAtual, produto, quantidade, preco);

            campoCodigoProduto.clear();
            campoQuantidade.clear();
            campoPrecoUnitario.clear();
            atualizarTabelaItensETotal();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Verifique a quantidade e o preço informados.");
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
        PedidoItem selecionado = tabelaItensNovoPedido.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum item selecionado", "Selecione um item para remover.");
            return;
        }
        pedidoAtual.removerItem(selecionado);
        atualizarTabelaItensETotal();
    }

    @FXML
    private void onCriarPedido() {
        if (comboFornecedor.getValue() == null) {
            AlertUtil.aviso("Fornecedor obrigatório", "Selecione o fornecedor do pedido.");
            return;
        }
        try {
            pedidoAtual.setObservacoes(campoObservacoes.getText());
            pedidoService.criarPedido(pedidoAtual, comboFornecedor.getValue());

            AlertUtil.info("Sucesso", "Pedido criado com sucesso!");

            pedidoAtual = new Pedido();
            comboFornecedor.setValue(null);
            campoObservacoes.clear();
            atualizarTabelaItensETotal();
            carregarTabelaPedidos();

        } catch (IllegalStateException e) {
            AlertUtil.aviso("Dados incompletos", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onMarcarComoRecebido() {
        Pedido selecionado = tabelaPedidos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum pedido selecionado", "Selecione um pedido na tabela.");
            return;
        }
        boolean confirmado = AlertUtil.confirmar("Confirmar recebimento",
                "Marcar o pedido #" + selecionado.getId() + " como recebido?\n" +
                        "Lembre-se: isso NÃO dá entrada automática no estoque — registre a entrada separadamente na tela 'Entrada de Produtos'.");
        if (!confirmado) return;

        try {
            pedidoService.atualizarStatus(selecionado.getId(), Pedido.Status.RECEBIDO);
            carregarTabelaPedidos();
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onCancelarPedido() {
        Pedido selecionado = tabelaPedidos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum pedido selecionado", "Selecione um pedido na tabela.");
            return;
        }
        try {
            pedidoService.atualizarStatus(selecionado.getId(), Pedido.Status.CANCELADO);
            carregarTabelaPedidos();
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    private void atualizarTabelaItensETotal() {
        ObservableList<PedidoItem> lista = FXCollections.observableArrayList(pedidoAtual.getItens());
        tabelaItensNovoPedido.setItems(lista);
        labelTotalNovoPedido.setText("Total: R$ " + pedidoAtual.getValorTotal());
    }

    private void carregarTabelaPedidos() {
        try {
            ObservableList<Pedido> lista = FXCollections.observableArrayList(pedidoService.listarTodos());
            tabelaPedidos.setItems(lista);
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }
}