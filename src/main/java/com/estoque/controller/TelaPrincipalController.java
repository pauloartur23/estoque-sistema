package com.estoque.controller;

import com.estoque.service.DashboardService;
import com.estoque.util.AlertUtil;
import com.estoque.util.IconFactory;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Controller do Dashboard principal: monta a sidebar com ícones reais
 * (via IconFactory), os cards de KPI do topo, e carrega o dashboard
 * completo (gráficos/tabelas) dentro do WebView.
 */
public class TelaPrincipalController {

    @FXML private Label logoIcone;
    @FXML private Label iconeSino;
    @FXML private HBox containerKpis;
    @FXML private WebView webViewDashboard;

    @FXML private Button botaoDashboard;
    @FXML private Button botaoProdutos;
    @FXML private Button botaoCategorias;
    @FXML private Button botaoMarcas;
    @FXML private Button botaoFornecedores;
    @FXML private Button botaoEntrada;
    @FXML private Button botaoSaida;
    @FXML private Button botaoTransferencias;
    @FXML private Button botaoVendas;
    @FXML private Button botaoClientes;
    @FXML private Button botaoPedidos;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        montarIconesSidebar();
        montarIconesTopbar();
        montarWebView();
    }

    private void montarIconesSidebar() {
        logoIcone.setGraphic(IconFactory.criar("dashboard", 22, Color.web("#FF6A00")));

        botaoDashboard.setGraphic(IconFactory.criar("dashboard", 18, Color.WHITE));
        botaoProdutos.setGraphic(IconFactory.criar("produtos", 18, Color.web("#C9CCD1")));
        botaoCategorias.setGraphic(IconFactory.criar("etiqueta-preco", 18, Color.web("#C9CCD1")));
        botaoMarcas.setGraphic(IconFactory.criar("marcador", 18, Color.web("#C9CCD1")));
        botaoFornecedores.setGraphic(IconFactory.criar("fornecedores", 18, Color.web("#C9CCD1")));
        botaoEntrada.setGraphic(IconFactory.criar("importar", 18, Color.web("#C9CCD1")));
        botaoSaida.setGraphic(IconFactory.criar("exportar", 18, Color.web("#C9CCD1")));
        botaoTransferencias.setGraphic(IconFactory.criar("transferencias", 18, Color.web("#C9CCD1")));
        botaoVendas.setGraphic(IconFactory.criar("venda", 18, Color.web("#C9CCD1")));
        botaoClientes.setGraphic(IconFactory.criar("clientes", 18, Color.web("#C9CCD1")));
        botaoPedidos.setGraphic(IconFactory.criar("notas-fiscais", 18, Color.web("#C9CCD1")));
    }

    private void montarIconesTopbar() {
        iconeSino.setGraphic(IconFactory.criar("sino", 20, Color.web("#C9CCD1")));
    }

    private void montarWebView() {
        webViewDashboard.setContextMenuEnabled(false);

        WebEngine engine = webViewDashboard.getEngine();
        engine.setOnError(event -> System.err.println("Erro no WebView: " + event.getMessage()));

        engine.getLoadWorker().stateProperty().addListener((obs, estadoAntigo, novoEstado) -> {
            if (novoEstado == Worker.State.SUCCEEDED) {
                atualizarDashboard();
            }
        });

        String urlHtml = Objects.requireNonNull(
                getClass().getResource("/webview/dashboard-cards.html")).toExternalForm();
        engine.load(urlHtml);
    }

    private void atualizarDashboard() {
        try {
            String json = dashboardService.montarJsonDashboard();
            webViewDashboard.getEngine().executeScript("atualizarDashboard(" + json + ")");
            montarKpis();
        } catch (SQLException e) {
            System.err.println("Não foi possível carregar os dados do dashboard: " + e.getMessage());
        }
    }

    /** Monta os 4 cards de KPI coloridos no topo (Total Produtos, Valor Estoque, Entradas, Saídas). */
    private void montarKpis() {
        containerKpis.getChildren().clear();
        try {
            com.estoque.dao.ProdutoDAO produtoDAO = new com.estoque.dao.ProdutoDAO();
            com.estoque.dao.EntradaEstoqueDAO entradaDAO = new com.estoque.dao.EntradaEstoqueDAO();
            com.estoque.dao.SaidaEstoqueDAO saidaDAO = new com.estoque.dao.SaidaEstoqueDAO();

            int totalProdutos = produtoDAO.contarTotal();
            var valorEstoque = produtoDAO.somarValorTotalEstoque();
            int entradasMes = entradaDAO.contarEntradasNoMes();
            int saidasMes = saidaDAO.contarSaidasNoMes();

            containerKpis.getChildren().add(criarCardKpi("caixa", "Total de Produtos",
                    String.valueOf(totalProdutos), "#4FA3F7"));
            containerKpis.getChildren().add(criarCardKpi("dinheiro", "Valor do Estoque",
                    "R$ " + valorEstoque, "#2ECC71"));
            containerKpis.getChildren().add(criarCardKpi("importar", "Entradas (Mês)",
                    String.valueOf(entradasMes), "#F5B942"));
            containerKpis.getChildren().add(criarCardKpi("exportar", "Saídas (Mês)",
                    String.valueOf(saidasMes), "#E74C3C"));

        } catch (Exception e) {
            System.err.println("Não foi possível montar os KPIs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox criarCardKpi(String icone, String titulo, String valor, String corHex) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-kpi");
        card.setStyle("-fx-border-color: " + corHex + "55; -fx-background-color: " + corHex + "1A;");
        card.setPadding(new Insets(16));
        card.setPrefWidth(220);

        Label labelIcone = new Label();
        labelIcone.setGraphic(IconFactory.criar(icone, 24, Color.web(corHex)));

        Label labelTitulo = new Label(titulo);
        labelTitulo.getStyleClass().add("kpi-titulo");

        Label labelValor = new Label(valor);
        labelValor.getStyleClass().add("kpi-valor");
        labelValor.setStyle("-fx-text-fill: " + corHex + ";");

        card.getChildren().addAll(labelIcone, labelValor, labelTitulo);
        return card;
    }

    // ---- Navegação da sidebar ----

    @FXML private void abrirProdutos() {
        abrirJanela("/fxml/TelaProduto.fxml", "Produtos", 1100, 700);
    }

    @FXML private void abrirCategorias() {
        abrirJanela("/fxml/TelaCategoria.fxml", "Categorias", 750, 550);
    }

    @FXML private void abrirMarcas() {
        abrirJanela("/fxml/TelaMarca.fxml", "Marcas", 650, 500);
    }

    @FXML private void abrirFornecedores() {
        abrirJanela("/fxml/TelaFornecedor.fxml", "Fornecedores", 950, 600);
    }

    @FXML private void abrirEntradaProdutos() {
        abrirJanela("/fxml/TelaEntradaEstoque.fxml", "Entrada de Produtos", 1000, 650);
    }

    @FXML private void abrirSaidaProdutos() {
        abrirJanela("/fxml/TelaSaidaEstoque.fxml", "Saída de Produtos", 950, 600);
    }

    @FXML private void abrirTransferencias() {
        abrirJanela("/fxml/TelaTransferencia.fxml", "Transferências", 950, 600);
    }

    @FXML private void abrirVendas() {
        abrirJanela("/fxml/TelaVenda.fxml", "Nova Venda", 1000, 650);
    }

    @FXML private void abrirClientes() {
        abrirJanela("/fxml/TelaCliente.fxml", "Clientes", 950, 600);
    }

    @FXML private void abrirPedidos() {
        abrirJanela("/fxml/TelaPedido.fxml", "Pedidos", 1100, 700);
    }

    private void abrirJanela(String caminhoFxml, String titulo, int largura, int altura) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(caminhoFxml)));
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root, largura, altura);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

            stage.setTitle(titulo);
            stage.setScene(scene);
            stage.show();

            stage.setOnHiding(event -> atualizarDashboard());

        } catch (IOException e) {
            AlertUtil.erro("Erro ao abrir tela", "Não foi possível abrir a tela solicitada.\n" + e.getMessage());
        }
    }
}