package com.estoque.dao;

import com.estoque.model.Fornecedor;
import com.estoque.model.Pedido;
import com.estoque.model.PedidoItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de pedidos de compra a fornecedores (ainda não recebidos —
 * não afeta estoque até virar uma Entrada de fato).
 */
public class PedidoDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void salvar(Pedido pedido) throws SQLException {
        String sqlPedido = "INSERT INTO pedido (fornecedor_id, data_pedido, status, valor_total, observacoes) " +
                "VALUES (?, NOW(), ?, ?, ?)";
        String sqlItem = "INSERT INTO pedido_item (pedido_id, produto_id, quantidade, preco_unitario, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = conexaoFactory.getConnection();
            con.setAutoCommit(false);

            int pedidoId;
            try (PreparedStatement stmt = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, pedido.getFornecedor().getId());
                stmt.setString(2, pedido.getStatus().name());
                stmt.setBigDecimal(3, pedido.getValorTotal());
                stmt.setString(4, pedido.getObservacoes());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    pedidoId = rs.getInt(1);
                    pedido.setId(pedidoId);
                }
            }

            try (PreparedStatement stmtItem = con.prepareStatement(sqlItem)) {
                for (PedidoItem item : pedido.getItens()) {
                    stmtItem.setInt(1, pedidoId);
                    stmtItem.setInt(2, item.getProduto().getId());
                    stmtItem.setInt(3, item.getQuantidade());
                    stmtItem.setBigDecimal(4, item.getPrecoUnitario());
                    stmtItem.setBigDecimal(5, item.getSubtotal());
                    stmtItem.executeUpdate();
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

    public void atualizarStatus(int pedidoId, Pedido.Status status) throws SQLException {
        String sql = "UPDATE pedido SET status=? WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, pedidoId);
            stmt.executeUpdate();
        }
    }

    public List<Pedido> listarTodos() throws SQLException {
        String sql = "SELECT p.*, f.nome AS fornecedor_nome FROM pedido p " +
                "JOIN fornecedor f ON f.id = p.fornecedor_id ORDER BY p.data_pedido DESC";

        List<Pedido> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("id"));

                Fornecedor f = new Fornecedor();
                f.setId(rs.getInt("fornecedor_id"));
                f.setNome(rs.getString("fornecedor_nome"));
                pedido.setFornecedor(f);

                Timestamp ts = rs.getTimestamp("data_pedido");
                if (ts != null) pedido.setDataPedido(ts.toLocalDateTime());
                pedido.setStatus(Pedido.Status.valueOf(rs.getString("status")));
                pedido.setValorTotal(rs.getBigDecimal("valor_total"));
                pedido.setObservacoes(rs.getString("observacoes"));

                lista.add(pedido);
            }
        }
        return lista;
    }

    public List<Pedido> listarAbertos() throws SQLException {
        String sql = "SELECT p.*, f.nome AS fornecedor_nome FROM pedido p " +
                "JOIN fornecedor f ON f.id = p.fornecedor_id " +
                "WHERE p.status IN ('PENDENTE', 'APROVADO') ORDER BY p.data_pedido DESC";

        List<Pedido> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId(rs.getInt("id"));

                Fornecedor f = new Fornecedor();
                f.setId(rs.getInt("fornecedor_id"));
                f.setNome(rs.getString("fornecedor_nome"));
                pedido.setFornecedor(f);

                Timestamp ts = rs.getTimestamp("data_pedido");
                if (ts != null) pedido.setDataPedido(ts.toLocalDateTime());
                pedido.setStatus(Pedido.Status.valueOf(rs.getString("status")));
                pedido.setValorTotal(rs.getBigDecimal("valor_total"));
                pedido.setObservacoes(rs.getString("observacoes"));

                lista.add(pedido);
            }
        }
        return lista;
    }

    public int contarAbertos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedido WHERE status IN ('PENDENTE', 'APROVADO')";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}