package com.estoque.controller;

import com.estoque.exception.CodigoDuplicadoException;
import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.Categoria;
import com.estoque.model.Fornecedor;
import com.estoque.model.Marca;
import com.estoque.model.Produto;
import com.estoque.service.*;
import com.estoque.util.AlertUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProdutoController {

    @FXML private TextField campoCodigo;
    @FXML private TextField campoNome;
    @FXML private TextArea campoDescricao;
    @FXML private ComboBox<Categoria> comboCategoria;
    @FXML private ComboBox<Marca> comboMarca;
    @FXML private ComboBox<Fornecedor> comboFornecedor;
    @FXML private TextField campoPreco;
    @FXML private TextField campoCusto;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoQuantidadeMinima;

    @FXML private ImageView previewImagem;
    @FXML private javafx.scene.control.Label labelSemImagem;

    @FXML private TextField campoCodigoEntrada;
    @FXML private TextField campoQuantidadeEntrada;

    @FXML private TextField campoBusca;

    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, Produto> colFoto;
    @FXML private TableColumn<Produto, String> colCodigo;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, String> colCategoria;
    @FXML private TableColumn<Produto, String> colPreco;
    @FXML private TableColumn<Produto, String> colQuantidade;
    @FXML private TableColumn<Produto, String> colStatus;

    private final ProdutoService produtoService = new ProdutoService();
    private final CategoriaService categoriaService = new CategoriaService();
    private final MarcaService marcaService = new MarcaService();
    private final FornecedorService fornecedorService = new FornecedorService();
    private final ImagemService imagemService = new ImagemService();
    private final BarcodeService barcodeService = new BarcodeService();

    private String caminhoImagemSelecionada;

    @FXML
    public void initialize() {
        carregarCombos();

        colFoto.setCellValueFactory(dados -> new SimpleObjectProperty<>(dados.getValue()));
        colFoto.setCellFactory(coluna -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(36);
                imageView.setFitHeight(36);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Produto produto, boolean vazio) {
                super.updateItem(produto, vazio);
                if (vazio || produto == null || !produto.temImagem()) {
                    setGraphic(null);
                } else {
                    Image imagem = imagemService.carregarParaPreview(produto.getImagemPath(), 36, 36);
                    imageView.setImage(imagem);
                    setGraphic(imageView);
                }
            }
        });

        colCodigo.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getCodigo()));
        colNome.setCellValueFactory(dados -> new SimpleStringProperty(dados.getValue().getNome()));
        colCategoria.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().getCategoria() == null ? "-" : dados.getValue().getCategoria().getNome()));
        colPreco.setCellValueFactory(dados -> new SimpleStringProperty(
                "R$ " + dados.getValue().getPreco().toString()));
        colQuantidade.setCellValueFactory(dados -> new SimpleStringProperty(
                String.valueOf(dados.getValue().getQuantidade())));
        colStatus.setCellValueFactory(dados -> new SimpleStringProperty(
                dados.getValue().isEstoqueBaixo() ? "⚠ Estoque baixo" : "OK"));

        carregarTabela();

        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                campoCodigo.setText(novo.getCodigo());
                campoNome.setText(novo.getNome());
                campoDescricao.setText(novo.getDescricao());
                comboCategoria.setValue(novo.getCategoria());
                comboMarca.setValue(novo.getMarca());
                comboFornecedor.setValue(novo.getFornecedor());
                campoPreco.setText(novo.getPreco().toString());
                campoCusto.setText(novo.getCusto() != null ? novo.getCusto().toString() : "0");
                campoQuantidade.setText(String.valueOf(novo.getQuantidade()));
                campoQuantidadeMinima.setText(String.valueOf(novo.getQuantidadeMinima()));

                caminhoImagemSelecionada = novo.getImagemPath();
                exibirPreview(caminhoImagemSelecionada);
            }
        });
    }

    private void carregarCombos() {
        try {
            comboCategoria.setItems(FXCollections.observableArrayList(categoriaService.listarTodas()));
            comboMarca.setItems(FXCollections.observableArrayList(marcaService.listarTodas()));
            comboFornecedor.setItems(FXCollections.observableArrayList(fornecedorService.listarTodos()));
        } catch (SQLException e) {
            AlertUtil.erro("Erro no banco de dados", "Não foi possível carregar categorias/marcas/fornecedores.\n" + e.getMessage());
        }
    }

    @FXML
    private void onEscolherImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar foto do produto");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

        Window janela = previewImagem.getScene().getWindow();
        File arquivo = fileChooser.showOpenDialog(janela);
        if (arquivo == null) return;

        try {
            String codigoBase = campoCodigo.getText().isBlank() ? "produto" : campoCodigo.getText().trim();
            caminhoImagemSelecionada = imagemService.salvar(codigoBase, arquivo);
            exibirPreview(caminhoImagemSelecionada);
        } catch (IOException e) {
            AlertUtil.erro("Erro ao carregar imagem", "Não foi possível salvar a imagem selecionada.\n" + e.getMessage());
        }
    }

    private void exibirPreview(String caminho) {
        Image imagem = imagemService.carregarParaPreview(caminho, 140, 140);
        previewImagem.setImage(imagem);
        labelSemImagem.setVisible(imagem == null);
        previewImagem.setVisible(imagem != null);
    }

    @FXML
    private void onCadastrar() {
        try {
            String codigo = campoCodigo.getText().trim();
            String nome = campoNome.getText().trim();
            String descricao = campoDescricao.getText();
            BigDecimal preco = new BigDecimal(campoPreco.getText().trim().replace(",", "."));
            BigDecimal custo = campoCusto.getText().isBlank()
                    ? BigDecimal.ZERO : new BigDecimal(campoCusto.getText().trim().replace(",", "."));
            int quantidade = Integer.parseInt(campoQuantidade.getText().trim());
            int quantidadeMinima = campoQuantidadeMinima.getText().isBlank()
                    ? 0 : Integer.parseInt(campoQuantidadeMinima.getText().trim());

            produtoService.cadastrar(codigo, nome, descricao, comboCategoria.getValue(), comboMarca.getValue(),
                    comboFornecedor.getValue(), preco, custo, quantidade, quantidadeMinima, caminhoImagemSelecionada);

            AlertUtil.info("Sucesso", "Produto cadastrado com sucesso!");
            limparCampos();
            carregarTabela();

        } catch (NumberFormatException e) {
            AlertUtil.erro("Erro de validação", "Verifique se o preço, custo e quantidades foram preenchidos corretamente (use números).");
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
            selecionado.setCategoria(comboCategoria.getValue());
            selecionado.setMarca(comboMarca.getValue());
            selecionado.setFornecedor(comboFornecedor.getValue());
            selecionado.setPreco(new BigDecimal(campoPreco.getText().trim().replace(",", ".")));
            selecionado.setCusto(campoCusto.getText().isBlank()
                    ? BigDecimal.ZERO : new BigDecimal(campoCusto.getText().trim().replace(",", ".")));
            selecionado.setQuantidade(Integer.parseInt(campoQuantidade.getText().trim()));
            selecionado.setQuantidadeMinima(Integer.parseInt(campoQuantidadeMinima.getText().trim()));
            selecionado.setImagemPath(caminhoImagemSelecionada);

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

    /** Gera um PDF com etiqueta(s) de código de barras do produto selecionado e abre automaticamente. */
    @FXML
    private void onGerarEtiqueta() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertUtil.aviso("Nenhum produto selecionado", "Selecione um produto na tabela para gerar a etiqueta.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Gerar Etiqueta");
        dialog.setHeaderText("Quantas etiquetas deseja imprimir para '" + selecionado.getNome() + "'?");
        dialog.setContentText("Quantidade:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isEmpty()) return;

        try {
            int copias = Integer.parseInt(resultado.get().trim());
            if (copias <= 0) {
                AlertUtil.aviso("Quantidade inválida", "A quantidade deve ser maior que zero.");
                return;
            }

            File arquivo = barcodeService.gerarFolhaDeEtiquetas(List.of(selecionado), copias);
            barcodeService.abrirArquivo(arquivo);

            AlertUtil.info("Etiqueta gerada",
                    "Etiqueta(s) gerada(s) com sucesso!\nArquivo: " + arquivo.getAbsolutePath());

        } catch (NumberFormatException e) {
            AlertUtil.aviso("Quantidade inválida", "Informe um número válido de etiquetas.");
        } catch (Exception e) {
            AlertUtil.erro("Erro ao gerar etiqueta", "Não foi possível gerar o PDF da etiqueta.\n" + e.getMessage());
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
        comboCategoria.setValue(null);
        comboMarca.setValue(null);
        comboFornecedor.setValue(null);
        campoPreco.clear();
        campoCusto.clear();
        campoQuantidade.clear();
        campoQuantidadeMinima.clear();
        caminhoImagemSelecionada = null;
        exibirPreview(null);
    }
}