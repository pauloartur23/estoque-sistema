package com.estoque.dao;

import com.estoque.model.MovimentacaoEstoque;
import com.estoque.model.Produto;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO do log unificado de movimentações — alimenta o Dashboard
 * (gráfico de entradas x saídas e a tabela de últimas movimentações).
 */
public class MovimentacaoEstoqueDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    /** Registra uma movimentação. Deve ser chamado dentro da MESMA transação da operação de origem. */
    public void registrar(Connection con, MovimentacaoEstoque mov) throws SQLException {
        String sql = "INSERT INTO movimentacao_estoque (tipo, produto_id, quantidade, descricao, referencia_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, mov.getTipo().name());
            stmt.setInt(2, mov.getProduto().getId());
            stmt.setInt(3, mov.getQuantidade());
            stmt.setString(4, mov.getDescricao());
            if (mov.getReferenciaId() != null) {
                stmt.setInt(5, mov.getReferenciaId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    /** Últimas N movimentações (mais recentes primeiro) — usado no Dashboard. */
    public List<MovimentacaoEstoque> listarUltimas(int limite) throws SQLException {
        String sql = "SELECT me.*, p.codigo, p.nome FROM movimentacao_estoque me " +
                "JOIN produto p ON p.id = me.produto_id " +
                "ORDER BY me.data_movimento DESC LIMIT ?";

        List<MovimentacaoEstoque> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto();
                    produto.setId(rs.getInt("produto_id"));
                    produto.setCodigo(rs.getString("codigo"));
                    produto.setNome(rs.getString("nome"));

                    MovimentacaoEstoque mov = new MovimentacaoEstoque();
                    mov.setId(rs.getInt("id"));
                    mov.setTipo(MovimentacaoEstoque.Tipo.valueOf(rs.getString("tipo")));
                    mov.setProduto(produto);
                    mov.setQuantidade(rs.getInt("quantidade"));
                    mov.setDescricao(rs.getString("descricao"));
                    Timestamp ts = rs.getTimestamp("data_movimento");
                    if (ts != null) mov.setDataMovimento(ts.toLocalDateTime());
                    int refId = rs.getInt("referencia_id");
                    if (!rs.wasNull()) mov.setReferenciaId(refId);

                    lista.add(mov);
                }
            }
        }
        return lista;
    }

    /**
     * Totais diários de entrada x saída dos últimos N dias, para o
     * gráfico de linha "Movimentações do Período" do Dashboard.
     * ENTRADA conta como entrada; SAIDA e VENDA contam como saída.
     */
    public Map<String, int[]> mapaEntradasSaidasPorDia(int dias) throws SQLException {
        String sql = "SELECT DATE(data_movimento) AS dia, tipo, SUM(quantidade) AS total " +
                "FROM movimentacao_estoque " +
                "WHERE data_movimento >= (CURDATE() - INTERVAL ? DAY) " +
                "GROUP BY DATE(data_movimento), tipo";

        Map<LocalDate, int[]> porData = new java.util.HashMap<>();

        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, dias - 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate dia = rs.getDate("dia").toLocalDate();
                    String tipo = rs.getString("tipo");
                    int total = rs.getInt("total");

                    int[] valores = porData.computeIfAbsent(dia, k -> new int[]{0, 0});
                    if ("ENTRADA".equals(tipo)) {
                        valores[0] += total;
                    } else if ("SAIDA".equals(tipo) || "VENDA".equals(tipo)) {
                        valores[1] += total;
                    }
                }
            }
        }

        Map<String, int[]> resultado = new LinkedHashMap<>();
        LocalDate hoje = LocalDate.now();
        for (int i = dias - 1; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            resultado.put(dia.format(FORMATO_DIA), porData.getOrDefault(dia, new int[]{0, 0}));
        }
        return resultado;
    }
}