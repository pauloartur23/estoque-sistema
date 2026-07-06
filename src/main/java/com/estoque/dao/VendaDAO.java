package com.estoque.dao;

import com.estoque.model.ItemVenda;
import com.estoque.model.Produto;
import com.estoque.model.Venda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO responsável por persistir vendas.
 * A gravação de uma venda é transacional: grava o cabeçalho (venda),
 * os itens (item_venda) e dá baixa no estoque (produto) — tudo ou nada.
 */
public class VendaDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void salvarVendaComBaixaEstoque(Venda venda) throws SQLException {
        String sqlVenda = "INSERT INTO venda (data_venda, valor_total, forma_pagamento, nome_cliente) " +
                "VALUES (NOW(), ?, ?, ?)";
        String sqlItem = "INSERT INTO item_venda (venda_id, produto_id, quantidade, preco_unitario, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";
        String sqlBaixaEstoque = "UPDATE produto SET quantidade = quantidade - ? WHERE id = ?";

        Connection con = null;
        try {
            con = conexaoFactory.getConnection();
            con.setAutoCommit(false);

            int vendaId;
            try (PreparedStatement stmtVenda = con.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                stmtVenda.setBigDecimal(1, venda.getValorTotal());
                stmtVenda.setString(2, venda.getFormaPagamento());
                stmtVenda.setString(3, venda.getNomeCliente());
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
                    stmtItem.addBatch();

                    stmtBaixa.setInt(1, item.getQuantidade());
                    stmtBaixa.setInt(2, item.getProduto().getId());
                    stmtBaixa.addBatch();
                }

                stmtItem.executeBatch();
                stmtBaixa.executeBatch();
            }

            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                con.rollback();
            }
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

            while (rs.next()) {
                vendas.add(mapearCabecalho(rs));
            }
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
                if (!rs.next()) {
                    return null;
                }
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

    private Venda mapearCabecalho(ResultSet rs) throws SQLException {
        Venda venda = new Venda();
        venda.setId(rs.getInt("id"));
        Timestamp ts = rs.getTimestamp("data_venda");
        if (ts != null) {
            venda.setDataVenda(ts.toLocalDateTime());
        }
        venda.setValorTotal(rs.getBigDecimal("valor_total"));
        venda.setFormaPagamento(rs.getString("forma_pagamento"));
        venda.setNomeCliente(rs.getString("nome_cliente"));
        return venda;
    }
}
