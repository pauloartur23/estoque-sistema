package com.estoque.dao;

import com.estoque.model.MovimentacaoEstoque;
import com.estoque.model.SaidaEstoque;
import com.estoque.model.SaidaItem;

import java.sql.*;

/**
 * DAO de saída de produtos (avaria, perda, uso interno, devolução —
 * tudo que NÃO é venda). Transacional: grava cabeçalho, itens,
 * subtrai do estoque e registra a movimentação.
 */
public class SaidaEstoqueDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();
    private final MovimentacaoEstoqueDAO movimentacaoDAO = new MovimentacaoEstoqueDAO();

    public void salvarComBaixaEstoque(SaidaEstoque saida) throws SQLException {
        String sqlSaida = "INSERT INTO saida_estoque (tipo_saida, cliente_id, data_saida, observacoes) " +
                "VALUES (?, ?, NOW(), ?)";
        String sqlItem = "INSERT INTO saida_item (saida_id, produto_id, quantidade, motivo) " +
                "VALUES (?, ?, ?, ?)";
        String sqlAtualizaEstoque = "UPDATE produto SET quantidade = quantidade - ? WHERE id = ?";

        Connection con = null;
        try {
            con = conexaoFactory.getConnection();
            con.setAutoCommit(false);

            int saidaId;
            try (PreparedStatement stmt = con.prepareStatement(sqlSaida, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, saida.getTipoSaida());
                if (saida.getCliente() != null) {
                    stmt.setInt(2, saida.getCliente().getId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                stmt.setString(3, saida.getObservacoes());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    rs.next();
                    saidaId = rs.getInt(1);
                    saida.setId(saidaId);
                }
            }

            try (PreparedStatement stmtItem = con.prepareStatement(sqlItem);
                 PreparedStatement stmtEstoque = con.prepareStatement(sqlAtualizaEstoque)) {

                for (SaidaItem item : saida.getItens()) {
                    stmtItem.setInt(1, saidaId);
                    stmtItem.setInt(2, item.getProduto().getId());
                    stmtItem.setInt(3, item.getQuantidade());
                    stmtItem.setString(4, item.getMotivo());
                    stmtItem.executeUpdate();

                    stmtEstoque.setInt(1, item.getQuantidade());
                    stmtEstoque.setInt(2, item.getProduto().getId());
                    stmtEstoque.executeUpdate();

                    MovimentacaoEstoque mov = new MovimentacaoEstoque(
                            MovimentacaoEstoque.Tipo.SAIDA, item.getProduto(), item.getQuantidade(),
                            saida.getTipoSaida() + (item.getMotivo() != null ? " - " + item.getMotivo() : ""),
                            saidaId);
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

    public int contarSaidasNoMes() throws SQLException {
        String sql = "SELECT COALESCE(SUM(qtd), 0) FROM (" +
                "SELECT SUM(si.quantidade) AS qtd FROM saida_item si " +
                "JOIN saida_estoque s ON s.id = si.saida_id " +
                "WHERE MONTH(s.data_saida) = MONTH(CURDATE()) AND YEAR(s.data_saida) = YEAR(CURDATE())" +
                ") AS sub";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}