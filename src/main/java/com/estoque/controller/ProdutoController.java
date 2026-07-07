package com.estoque.controller;

import com.estoque.exception.CodigoDuplicadoException;
import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.Produto;
import com.estoque.service.ProdutoService;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Controller da tela de Produtos: cadastro (entrada), busca por código,
 * entrada adicional de estoque e listagem.
 */
public class ProdutoController {

    // ---- Formulário de cadastro ----
    @FXML private TextField campoCodigo;
    @FXML private TextField campoNome;
    @FXML private TextArea campoDescricao;
    @FXML private TextField campoCategoria;
    @FXML private TextField campoPreco;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoQuantidadeMinima;

    // ---- Entrada de estoque adicional ----
    @FXML private TextField campoCodigoEntrada;
    @FXML private TextField campoQuantidadeEntrada;

    // ---- Busca ----
    @FXML private TextField campoBusca;

    // ---- Tabela ----
    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, String> colPreco;
    @FXML private TableColumn<Produto, String> colQuantidade;
    @FXML private TableColumn<Produto, String> colStatus;

    private final ProdutoService produtoService = new ProdutoService();

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getCodigo()));
        colNome.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getNome()));
        colCategoria.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().getCategoria() == null ? "-" : dados.getValue().getCategoria()));
        colPreco.setCellValueFactory(dados -> new SimpleStringProperty(
                "R$ " + dados.getValue().getPreco().toString()));
        colQuantidade.setCellValueFactory(dados -> new SimpleStringProperty(
                String.valueOf(dados.getValue().getQuantidade())));
        colStatus.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().isEstoqueBaixo() ? "⚠ Estoque baixo" : "OK"));

        carregarTabela();

        // ao clicar em uma linha, joga os dados para o formulário (facilita edição futura)
        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoCodigo.setText(novo.getCodigo());
                campoNome.setText(novo.getNome());
                campoDescricao.setText(novo.getDescricao());
                campoCategoria.setText(novo.getCategoria());
                campoPreco.setText(novo.getPreco().toString());
                campoQuantidade.setText(String.valueOf(novo.getQuantidade()));
                campoQuantidadeMinima.setText(String.valueOf(novo.getQuantidadeMinima()));
            }
        });
    }

    @FXML
    private void onCadastrar() {
        try {
            String codigo = campoCodigo.getText().trim();
            String nome = campoNome.getText().trim();
            String descricao = campoDescricao.getText();
            String categoria = campoCategoria.getText().trim();
            BigDecimal preco = new BigDecimal(campoPreco.getText().trim().replace(",", "."));
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());
            int quantidadeMinima = campoQuantidadeMinima.getText().isBlank()
                    ? 0 : Integer.parseInt(campoQuantidadeMinima.getText().trim());

            produtoService.cadastrar(codigo, nome, descricao, categoria, preco, quantidade, quantidadeMinima);

            AlertUtil.info("Sucesso", "Produto cadastrado com sucesso!");
            limparCampos();
            carregarTabela();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Verifique se o preço e as quantidades foram preenchidos corretamente (use números).");
        } catch (CodigoDuplicadoException e) {
            AlertUtil.aviso("Código duplicado", e.getMessage());
        } catch (IllegalArgumentException e) {
            AlertUtil.aviso("Campos inválidos", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", "Não foi possível cadastrar o produto.\n" + e.getMessage());
        }
    }

    @FXML
    private void onDarEntrada() {
        try {
            String codigo = campoCodigoEntrada.getText().trim();
            int quantidade = Integer.parseInt(campoQuantidadeEntrada.getText().trim());

            Produto produto = produtoService.darEntradaEstoque(codigo, quantidade);

            AlertUtil.info("Entrada registrada",
                    "Novo estoque de '" + produto.getNome() + "': " + produto.getQuantidade() + " un.");
            campoCodigoEntrada.clear();
            campoQuantidadeEntrada.clear();
            carregarTabela();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Informe uma quantidade numérica válida.");
        } catch (ProdutoNaoEncontradoException e) {
            AlertUtil.aviso("Produto não encontrado", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onAtualizar() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum produto selecionado", "Selecione um produto na tabela para atualizar.");
            return;
        }
        try {
            selecionado.setNome(campoNome.getText().trim());
            selecionado.setDescricao(campoDescricao.getText());
            selecionado.setCategoria(campoCategoria.getText().trim());
            selecionado.setPreco(new BigDecimal(campoPreco.getText().trim().replace(",", ".")));
            selecionado.setQuantidade(Integer.parseInt(campoQuantidade.getText().trim()));
            selecionado.setQuantidadeMinima(Integer.parseInt(campoQuantidadeMinima.getText().trim()));

            produtoService.atualizar(selecionado);
            AlertUtil.info("Sucesso", "Produto atualizado com sucesso!");
            carregarTabela();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Verifique os campos numéricos.");
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onRemover() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum produto selecionado", "Selecione um produto na tabela para remover.");
            return;
        }
        boolean confirmado = AlertUtil.confirmar("Confirmar remoção",
                "Deseja realmente remover o produto '" + selecionado.getNome() + "'?");
        if (!confirmado) return;

        try {
            produtoService.remover(selecionado.getId());
            AlertUtil.info("Removido", "Produto removido com sucesso.");
            limparCampos();
            carregarTabela();
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", e.getMessage());
        }
    }

    @FXML
    private void onBuscar() {
        carregarTabela();
    }

    private void carregarTabela() {
        try {
            ObservableList<Produto> lista = FXCollections.observableArrayList(
                    produtoService.buscar(campoBusca.getText()));
            tabelaProdutos.setItems(lista);
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", "Não foi possível carregar os produtos.\n" + e.getMessage());
        }
    }

    private void limparCampos() {
        campoCodigo.clear();
        campoNome.clear();
        campoDescricao.clear();
        campoCategoria.clear();
        campoPreco.clear();
        campoQuantidade.clear();
        campoQuantidadeMinima.clear();
    }
}