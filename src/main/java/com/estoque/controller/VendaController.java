package com.estoque.controller;

import com.estoque.exception.EstoqueInsuficienteException;
import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.Cliente;
import com.estoque.model.ItemVenda;
import com.estoque.model.Venda;
import com.estoque.service.ClienteService;
import com.estoque.service.NotaFiscalService;
import com.estoque.service.VendaService;
import com.estoque.util.AlertUtil;
import com.estoque.util.IconFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class VendaController {

    @FXML private TextField campoCodigoProduto;
    @FXML private TextField campoQuantidade;
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private TextField campoDesconto;
    @FXML private HBox containerFormasPagamento;
    @FXML private HBox containerBandeiras;
    @FXML private TextArea campoObservacoes;

    @FXML private TableView<ItemVenda> tabelaItens;
    @FXML private TableColumn<ItemVenda, String> colCodigo;
    @FXML private TableColumn<ItemVenda, String> colProduto;
    @FXML private TableColumn<ItemVenda, String> colQuantidade;
    @FXML private TableColumn<ItemVenda, String> colPrecoUnitario;
    @FXML private TableColumn<ItemVenda, String> colSubtotal;

    @FXML private Label labelTotal;

    private final VendaService vendaService = new VendaService();
    private final NotaFiscalService notaFiscalService = new NotaFiscalService();
    private final ClienteService clienteService = new ClienteService();

    private final ToggleGroup grupoFormaPagamento = new ToggleGroup();
    private Venda vendaAtual = new Venda();

    @FXML
    public void initialize() {
        montarBotoesFormaPagamento();
        montarBandeirasAceitas();

        grupoFormaPagamento.selectedToggleProperty().addListener((obs, antigo, novo) -> {
            if (novo == null && antigo != null) {
                grupoFormaPagamento.selectToggle(antigo);
            }
        });

        try {
            comboCliente.setItems(FXCollections.observableArrayList(clienteService.listarTodos()));
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", "Não foi possível carregar os clientes.\n" + e.getMessage());
        }

        colCodigo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getCodigo()));
        colProduto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantidade())));
        colPrecoUnitario.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getPrecoUnitario()));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty("R$ " + d.getValue().getSubtotal()));

        atualizarTabelaETotal();

        // Ao abrir a tela, o cursor já nasce no campo de código, pronto
        // para bipar o primeiro produto sem precisar clicar em nada.
        Platform.runLater(() -> campoCodigoProduto.requestFocus());
    }

    /** Monta os botões de forma de pagamento com ícone real (Dinheiro, PIX, Débito, Crédito). */
    private void montarBotoesFormaPagamento() {
        containerFormasPagamento.getChildren().addAll(
                criarBotaoFormaPagamento("Dinheiro", "dinheiro", "#2ECC71"),
                criarBotaoFormaPagamento("PIX", "pix", "#2ECC71"),
                criarBotaoFormaPagamento("Cartão de Débito", "maquininha-cartao", "#4FA3F7"),
                criarBotaoFormaPagamento("Cartão de Crédito", "maquininha-cartao", "#9B59B6")
        );
    }

    private ToggleButton criarBotaoFormaPagamento(String rotulo, String icone, String corHex) {
        ToggleButton botao = new ToggleButton(rotulo);
        botao.setGraphic(IconFactory.criar(icone, 20, Color.web(corHex)));
        botao.setContentDisplay(ContentDisplay.TOP);
        botao.setToggleGroup(grupoFormaPagamento);
        botao.setUserData(rotulo);
        botao.getStyleClass().add("botao-forma-pagamento");
        botao.setMinWidth(110);
        return botao;
    }

    private void montarBandeirasAceitas() {
        String[][] bandeiras = {
                {"visa", "#1A1F71"}, {"mastercard", "#EB001B"}, {"elo", "#000000"},
                {"amex", "#2E77BC"}, {"hipercard", "#822124"}, {"boleto", "#C9CCD1"}
        };
        for (String[] bandeira : bandeiras) {
            Label logo = new Label();
            logo.setGraphic(IconFactory.criar(bandeira[0], 26, Color.web(bandeira[1])));
            containerBandeiras.getChildren().add(logo);
        }
    }

    /**
     * Dispara tanto ao clicar em "+ Adicionar Item" quanto ao apertar
     * Enter no campo de código — que é exatamente o que um leitor de
     * código de barras USB faz automaticamente após bipar.
     */
    @FXML
    private void onAdicionarItem() {
        try {
            String codigo = campoCodigoProduto.getText().trim();
            if (codigo.isEmpty()) {
                return;
            }
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());

            vendaService.adicionarItem(vendaAtual, codigo, quantidade);

            campoCodigoProduto.clear();
            campoQuantidade.setText("1");
            atualizarTabelaETotal();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Informe uma quantidade numérica válida.");
        } catch (ProdutoNaoEncontradoException e) {
            AlertUtil.aviso("Produto não encontrado", e.getMessage());
        } catch (EstoqueInsuficienteException e) {
            AlertUtil.aviso("Estoque insuficiente", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        } finally {
            // Devolve o foco para o campo de código, pronto para o próximo
            // produto ser bipado em sequência, sem precisar tocar no mouse.
            campoCodigoProduto.requestFocus();
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
    private void onAplicarDesconto() {
        try {
            String texto = campoDesconto.getText().trim();
            BigDecimal desconto = texto.isBlank() ? BigDecimal.ZERO : new BigDecimal(texto.replace(",", "."));
            vendaAtual.setDesconto(desconto);
            atualizarTabelaETotal();
        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Informe um valor de desconto numérico válido.");
        }
    }

    @FXML
    private void onFinalizarVenda() {
        if (vendaAtual.getItens().isEmpty()) {
            AlertUtil.aviso("Venda vazia", "Adicione ao menos um item antes de finalizar a venda.");
            return;
        }
        Toggle selecionado = grupoFormaPagamento.getSelectedToggle();
        if (selecionado == null) {
            AlertUtil.aviso("Forma de pagamento", "Selecione a forma de pagamento.");
            return;
        }

        boolean confirmado = AlertUtil.confirmar("Finalizar venda",
                "Confirmar venda no valor total de R$ " + vendaAtual.getValorTotal() + "?");
        if (!confirmado) return;

        try {
            vendaAtual.setFormaPagamento((String) selecionado.getUserData());
            vendaAtual.setCliente(comboCliente.getValue());
            vendaAtual.setNomeCliente(comboCliente.getValue() != null ? comboCliente.getValue().getNome() : "Consumidor Final");
            vendaAtual.setObservacoes(campoObservacoes.getText());
            vendaAtual.setDataVenda(java.time.LocalDateTime.now());

            vendaService.finalizarVenda(vendaAtual);

            File pdf = notaFiscalService.gerarEAbrir(vendaAtual);

            AlertUtil.info("Venda concluída",
                    "Venda registrada com sucesso!\nNota fiscal gerada em: " + pdf.getAbsolutePath());

            vendaAtual = new Venda();
            comboCliente.setValue(null);
            campoDesconto.setText("0");
            campoObservacoes.clear();
            grupoFormaPagamento.selectToggle(null);
            atualizarTabelaETotal();
            campoCodigoProduto.requestFocus();

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
        labelTotal.setText("Total: R$ " + vendaAtual.getValorTotal()
                + (vendaAtual.getDesconto() != null && vendaAtual.getDesconto().compareTo(BigDecimal.ZERO) > 0
                ? "  (desconto: R$ " + vendaAtual.getDesconto() + ")" : ""));
    }
}