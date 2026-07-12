package com.estoque.dao;

import com.estoque.model.Cliente;
import com.estoque.model.ItemVenda;
import com.estoque.model.MovimentacaoEstoque;
import com.estoque.model.Produto;
import com.estoque.model.Venda;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VendaDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();
    private final MovimentacaoEstoqueDAO movimentacaoDAO = new MovimentacaoEstoqueDAO();
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    public void salvarVendaComBaixaEstoque(Venda venda) throws SQLException {
        String sqlVenda = "INSERT INTO venda (cliente_id, data_venda, valor_total, desconto, forma_pagamento, nome_cliente, observacoes) " +
                "VALUES (?, NOW(), ?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO item_venda (venda_id, produto_id, quantidade, preco_unitario, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";
        String sqlBaixaEstoque = "UPDATE produto SET quantidade = quantidade - ? WHERE id = ?";

        Connection con = null;
        try {
            con = conexaoFactory.getConnection();
            con.setAutoCommit(false);

            int vendaId;
            try (PreparedStatement stmtVenda = con.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                if (venda.getCliente() != null) {
                    stmtVenda.setInt(1, venda.getCliente().getId());
                } else {
                    stmtVenda.setNull(1, Types.INTEGER);
                }
                stmtVenda.setBigDecimal(2, venda.getValorTotal());
                stmtVenda.setBigDecimal(3, venda.getDesconto());
                stmtVenda.setString(4, venda.getFormaPagamento());
                stmtVenda.setString(5, venda.getNomeCliente());
                stmtVenda.setString(6, venda.getObservacoes());
                stmtVenda.executeUpdate();

                try (ResultSet rs = stmtVenda.getGeneratedKeys()) {
                    rs.next();
                    vendaId = rs.getInt(1);
                    venda.setId(vendaId);
                }
            }

            try (PreparedStatement stmtItem = con.prepareStatement(sqlItem);
                 PreparedStatement stmtBaixa = con.prepareStatement(sqlBaixaEstoque)) {

                for (ItemVenda item : venda.getItens()) {
                    stmtItem.setInt(1, vendaId);
                    stmtItem.setInt(2, item.getProduto().getId());
                    stmtItem.setInt(3, item.getQuantidade());
                    stmtItem.setBigDecimal(4, item.getPrecoUnitario());
                    stmtItem.setBigDecimal(5, item.getSubtotal());
                    stmtItem.executeUpdate();

                    stmtBaixa.setInt(1, item.getQuantidade());
                    stmtBaixa.setInt(2, item.getProduto().getId());
                    stmtBaixa.executeUpdate();

                    MovimentacaoEstoque mov = new MovimentacaoEstoque(
                            MovimentacaoEstoque.Tipo.VENDA, item.getProduto(), item.getQuantidade(),
                            "Venda #" + vendaId, vendaId);
                    movimentacaoDAO.registrar(con, mov);
                }
            }

            con.commit();

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    public List<Venda> listarTodas() throws SQLException {
        String sql = "SELECT * FROM venda ORDER BY data_venda DESC";
        List<Venda> vendas = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) vendas.add(mapearCabecalho(rs));
        }
        return vendas;
    }

    public Venda buscarPorId(int id) throws SQLException {
        String sqlVenda = "SELECT * FROM venda WHERE id=?";
        String sqlItens = "SELECT iv.*, p.codigo, p.nome FROM item_venda iv " +
                "JOIN produto p ON p.id = iv.produto_id WHERE iv.venda_id=?";

        Venda venda;
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sqlVenda)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                venda = mapearCabecalho(rs);
            }

            try (PreparedStatement stmtItens = con.prepareStatement(sqlItens)) {
                stmtItens.setInt(1, id);
                try (ResultSet rs = stmtItens.executeQuery()) {
                    while (rs.next()) {
                        Produto p = new Produto();
                        p.setId(rs.getInt("produto_id"));
                        p.setCodigo(rs.getString("codigo"));
                        p.setNome(rs.getString("nome"));

                        ItemVenda item = new ItemVenda();
                        item.setId(rs.getInt("id"));
                        item.setProduto(p);
                        item.setQuantidade(rs.getInt("quantidade"));
                        item.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));
                        item.setSubtotal(rs.getBigDecimal("subtotal"));

                        venda.getItens().add(item);
                    }
                }
            }
        }
        return venda;
    }

    public int contarVendasHoje() throws SQLException {
        String sql = "SELECT COUNT(*) FROM venda WHERE DATE(data_venda) = CURDATE()";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public BigDecimal somarFaturamentoHoje() throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor_total), 0) FROM venda WHERE DATE(data_venda) = CURDATE()";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    public Map<String, BigDecimal> faturamentoUltimosDias(int dias) throws SQLException {
        String sql = "SELECT DATE(data_venda) AS dia, SUM(valor_total) AS total FROM venda " +
                "WHERE data_venda >= (CURDATE() - INTERVAL ? DAY) GROUP BY DATE(data_venda)";

        Map<LocalDate, BigDecimal> porData = new java.util.HashMap<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, dias - 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    porData.put(rs.getDate("dia").toLocalDate(), rs.getBigDecimal("total"));
                }
            }
        }

        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        LocalDate hoje = LocalDate.now();
        for (int i = dias - 1; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            resultado.put(dia.format(FORMATO_DIA), porData.getOrDefault(dia, BigDecimal.ZERO));
        }
        return resultado;
    }

    private Venda mapearCabecalho(ResultSet rs) throws SQLException {
        Venda venda = new Venda();
        venda.setId(rs.getInt("id"));

        int clienteId = rs.getInt("cliente_id");
        if (!rs.wasNull()) {
            Cliente c = new Cliente();
            c.setId(clienteId);
            venda.setCliente(c);
        }

        Timestamp ts = rs.getTimestamp("data_venda");
        if (ts != null) venda.setDataVenda(ts.toLocalDateTime());
        venda.setValorTotal(rs.getBigDecimal("valor_total"));
        venda.setDesconto(rs.getBigDecimal("desconto") != null ? rs.getBigDecimal("desconto") : BigDecimal.ZERO);
        venda.setFormaPagamento(rs.getString("forma_pagamento"));
        venda.setNomeCliente(rs.getString("nome_cliente"));
        venda.setObservacoes(rs.getString("observacoes"));
        return venda;
    }
}