package com.estoque.dao;

import com.estoque.model.EntradaEstoque;
import com.estoque.model.EntradaItem;
import com.estoque.model.Fornecedor;
import com.estoque.model.MovimentacaoEstoque;
import com.estoque.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de entrada de produtos (compras). A gravação é transacional:
 * grava o cabeçalho, os itens, soma no estoque do produto e registra
 * a movimentação — tudo ou nada.
 */
public class EntradaEstoqueDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();
    private final MovimentacaoEstoqueDAO movimentacaoDAO = new MovimentacaoEstoqueDAO();

    public void salvarComEntradaNoEstoque(EntradaEstoque entrada) throws SQLException {
        String sqlEntrada = "INSERT INTO entrada_estoque (fornecedor_id, data_entrada, numero_nota, observacoes, valor_total) " +
                "VALUES (?, NOW(), ?, ?, ?)";
        String sqlItem = "INSERT INTO entrada_item (entrada_id, produto_id, quantidade, custo_unitario, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";
        String sqlAtualizaEstoque = "UPDATE produto SET quantidade = quantidade + ? WHERE id = ?";

        Connection con = null;
        try {
            con = conexaoFactory.getConnection();
            con.setAutoCommit(false);

            int entradaId;
            try (PreparedStatement stmt = con.prepareStatement(sqlEntrada, Statement.RETURN_GENERATED_KEYS)) {
                if (entrada.getFornecedor() != null) {
                    stmt.setInt(1, entrada.getFornecedor().getId());
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                stmt.setString(2, entrada.getNumeroNota());
                stmt.setString(3, entrada.getObservacoes());
                stmt.setBigDecimal(4, entrada.getValorTotal());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    entradaId = rs.getInt(1);
                    entrada.setId(entradaId);
                }
            }

            try (PreparedStatement stmtItem = con.prepareStatement(sqlItem);
                 PreparedStatement stmtEstoque = con.prepareStatement(sqlAtualizaEstoque)) {

                for (EntradaItem item : entrada.getItens()) {
                    stmtItem.setInt(1, entradaId);
                    stmtItem.setInt(2, item.getProduto().getId());
                    stmtItem.setInt(3, item.getQuantidade());
                    stmtItem.setBigDecimal(4, item.getCustoUnitario());
                    stmtItem.setBigDecimal(5, item.getSubtotal());
                    stmtItem.executeUpdate();

                    stmtEstoque.setInt(1, item.getQuantidade());
                    stmtEstoque.setInt(2, item.getProduto().getId());
                    stmtEstoque.executeUpdate();

                    MovimentacaoEstoque mov = new MovimentacaoEstoque(
                            MovimentacaoEstoque.Tipo.ENTRADA, item.getProduto(), item.getQuantidade(),
                            "Entrada de estoque" + (entrada.getNumeroNota() != null ? " - NF " + entrada.getNumeroNota() : ""),
                            entradaId);
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

    public List<EntradaEstoque> listarTodas() throws SQLException {
        String sql = "SELECT e.*, f.nome AS fornecedor_nome FROM entrada_estoque e " +
                "LEFT JOIN fornecedor f ON f.id = e.fornecedor_id ORDER BY e.data_entrada DESC";

        List<EntradaEstoque> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EntradaEstoque entrada = new EntradaEstoque();
                entrada.setId(rs.getInt("id"));

                int fornecedorId = rs.getInt("fornecedor_id");
                if (!rs.wasNull()) {
                    Fornecedor f = new Fornecedor();
                    f.setId(fornecedorId);
                    f.setNome(rs.getString("fornecedor_nome"));
                    entrada.setFornecedor(f);
                }

                Timestamp ts = rs.getTimestamp("data_entrada");
                if (ts != null) entrada.setDataEntrada(ts.toLocalDateTime());
                entrada.setNumeroNota(rs.getString("numero_nota"));
                entrada.setObservacoes(rs.getString("observacoes"));
                entrada.setValorTotal(rs.getBigDecimal("valor_total"));

                lista.add(entrada);
            }
        }
        return lista;
    }

    public int contarEntradasNoMes() throws SQLException {
        String sql = "SELECT COALESCE(SUM(qtd), 0) FROM (" +
                "SELECT SUM(ei.quantidade) AS qtd FROM entrada_item ei " +
                "JOIN entrada_estoque e ON e.id = ei.entrada_id " +
                "WHERE MONTH(e.data_entrada) = MONTH(CURDATE()) AND YEAR(e.data_entrada) = YEAR(CURDATE())" +
                ") AS sub";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}