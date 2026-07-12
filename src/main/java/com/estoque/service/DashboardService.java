package com.estoque.service;

import com.estoque.dao.*;
import com.estoque.model.MovimentacaoEstoque;
import com.estoque.model.Produto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrega TODOS os dados reais exibidos no Dashboard: cards de resumo,
 * gráfico de movimentações (entradas x saídas), distribuição por
 * categoria, produtos com estoque baixo e últimas movimentações.
 */
public class DashboardService {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();
    private final EntradaEstoqueDAO entradaDAO = new EntradaEstoqueDAO();
    private final SaidaEstoqueDAO saidaDAO = new SaidaEstoqueDAO();
    private final MovimentacaoEstoqueDAO movimentacaoDAO = new MovimentacaoEstoqueDAO();
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String montarJsonDashboard() throws SQLException {
        StringBuilder json = new StringBuilder("{");

        // ---- Cards de resumo ----
        int totalProdutos = produtoDAO.contarTotal();
        BigDecimal valorEstoque = produtoDAO.somarValorTotalEstoque();
        int entradasMes = entradaDAO.contarEntradasNoMes();
        int saidasMes = saidaDAO.contarSaidasNoMes();

        json.append("\"totalProdutos\": ").append(totalProdutos).append(",");
        json.append("\"valorEstoque\": \"").append(formatarMoeda(valorEstoque)).append("\",");
        json.append("\"entradasMes\": ").append(entradasMes).append(",");
        json.append("\"saidasMes\": ").append(saidasMes).append(",");

        // ---- Gráfico de linha: Movimentações do período (últimos 15 dias) ----
        Map<String, int[]> movPorDia = movimentacaoDAO.mapaEntradasSaidasPorDia(15);
        json.append("\"movLabels\": [")
                .append(movPorDia.keySet().stream().map(l -> "\"" + l + "\"").reduce((a, b) -> a + "," + b).orElse(""))
                .append("],");
        json.append("\"movEntradas\": [")
                .append(movPorDia.values().stream().map(v -> String.valueOf(v[0])).reduce((a, b) -> a + "," + b).orElse(""))
                .append("],");
        json.append("\"movSaidas\": [")
                .append(movPorDia.values().stream().map(v -> String.valueOf(v[1])).reduce((a, b) -> a + "," + b).orElse(""))
                .append("],");

        // ---- Gráfico de rosca: Distribuição por categoria ----
        List<Produto> todosProdutos = produtoDAO.listarTodos();
        Map<String, Integer> distribuicao = new LinkedHashMap<>();
        for (Produto p : todosProdutos) {
            String nomeCategoria = (p.getCategoria() != null) ? p.getCategoria().getNome() : "Sem categoria";
            distribuicao.merge(nomeCategoria, 1, Integer::sum);
        }
        json.append("\"categoriaLabels\": [")
                .append(distribuicao.keySet().stream().map(l -> "\"" + escapar(l) + "\"").reduce((a, b) -> a + "," + b).orElse(""))
                .append("],");
        json.append("\"categoriaValores\": [")
                .append(distribuicao.values().stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""))
                .append("],");

        // ---- Tabela: Produtos com estoque baixo ----
        List<Produto> estoqueBaixo = produtoDAO.listarEstoqueBaixo();
        json.append("\"estoqueBaixo\": [").append(montarArrayEstoqueBaixo(estoqueBaixo)).append("],");

        // ---- Tabela: Últimas movimentações ----
        List<MovimentacaoEstoque> ultimasMov = movimentacaoDAO.listarUltimas(8);
        json.append("\"ultimasMovimentacoes\": [").append(montarArrayMovimentacoes(ultimasMov)).append("]");

        json.append("}");
        return json.toString();
    }

    private String montarArrayEstoqueBaixo(List<Produto> produtos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            sb.append("{")
                    .append("\"nome\": \"").append(escapar(p.getNome())).append("\",")
                    .append("\"codigo\": \"").append(escapar(p.getCodigo())).append("\",")
                    .append("\"quantidade\": ").append(p.getQuantidade()).append(",")
                    .append("\"quantidadeMinima\": ").append(p.getQuantidadeMinima())
                    .append("}");
            if (i < produtos.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private String montarArrayMovimentacoes(List<MovimentacaoEstoque> movimentacoes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < movimentacoes.size(); i++) {
            MovimentacaoEstoque m = movimentacoes.get(i);
            sb.append("{")
                    .append("\"tipo\": \"").append(m.getTipo().name()).append("\",")
                    .append("\"descricao\": \"").append(escapar(m.getProduto().getNome())).append("\",")
                    .append("\"quantidade\": ").append(m.getQuantidade()).append(",")
                    .append("\"data\": \"").append(m.getDataMovimento().format(FORMATO_DATA_HORA)).append("\"")
                    .append("}");
            if (i < movimentacoes.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) valor = BigDecimal.ZERO;
        return String.format("%,.2f", valor).replace(",", "X").replace(".", ",").replace("X", ".");
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("\"", "\\\"");
    }
}