package com.estoque.dao;

import com.estoque.model.LocalEstoque;
import com.estoque.model.Produto;
import com.estoque.model.Transferencia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de transferências de produto entre locais de estoque.
 * Não altera a quantidade total do produto (apenas simboliza
 * movimento entre locais); serve como registro histórico.
 */
public class TransferenciaDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void salvar(Transferencia transferencia) throws SQLException {
        String sql = "INSERT INTO transferencia (produto_id, origem_id, destino_id, quantidade, data_transferencia, observacoes) " +
                "VALUES (?, ?, ?, ?, NOW(), ?)";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, transferencia.getProduto().getId());
            stmt.setInt(2, transferencia.getOrigem().getId());
            stmt.setInt(3, transferencia.getDestino().getId());
            stmt.setInt(4, transferencia.getQuantidade());
            stmt.setString(5, transferencia.getObservacoes());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) transferencia.setId(rs.getInt(1));
            }
        }
    }

    public List<Transferencia> listarTodas() throws SQLException {
        String sql = "SELECT t.*, p.codigo, p.nome AS produto_nome, " +
                "lo.nome AS origem_nome, ld.nome AS destino_nome " +
                "FROM transferencia t " +
                "JOIN produto p ON p.id = t.produto_id " +
                "JOIN local_estoque lo ON lo.id = t.origem_id " +
                "JOIN local_estoque ld ON ld.id = t.destino_id " +
                "ORDER BY t.data_transferencia DESC";

        List<Transferencia> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("produto_id"));
                produto.setCodigo(rs.getString("codigo"));
                produto.setNome(rs.getString("produto_nome"));

                LocalEstoque origem = new LocalEstoque();
                origem.setId(rs.getInt("origem_id"));
                origem.setNome(rs.getString("origem_nome"));

                LocalEstoque destino = new LocalEstoque();
                destino.setId(rs.getInt("destino_id"));
                destino.setNome(rs.getString("destino_nome"));

                Transferencia t = new Transferencia();
                t.setId(rs.getInt("id"));
                t.setProduto(produto);
                t.setOrigem(origem);
                t.setDestino(destino);
                t.setQuantidade(rs.getInt("quantidade"));
                Timestamp ts = rs.getTimestamp("data_transferencia");
                if (ts != null) t.setDataTransferencia(ts.toLocalDateTime());
                t.setObservacoes(rs.getString("observacoes"));

                lista.add(t);
            }
        }
        return lista;
    }
}