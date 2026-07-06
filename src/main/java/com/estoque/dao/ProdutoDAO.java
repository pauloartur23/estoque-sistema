package com.estoque.dao;

import com.estoque.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO responsável por todo o acesso à tabela `produto`.
 * Nenhuma regra de negócio deve ficar aqui — apenas SQL.
 */
public class ProdutoDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto " +
                "(codigo, nome, descricao, categoria, preco, quantidade, quantidade_minima, ativo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getCodigo());
            stmt.setString(2, produto.getNome());
            stmt.setString(3, produto.getDescricao());
            stmt.setString(4, produto.getCategoria());
            stmt.setBigDecimal(5, produto.getPreco());
            stmt.setInt(6, produto.getQuantidade());
            stmt.setInt(7, produto.getQuantidadeMinima());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setId(rs.getInt(1));
                }
            }
        }
    }

    public void atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome=?, descricao=?, categoria=?, preco=?, " +
                "quantidade=?, quantidade_minima=? WHERE id=?";

        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setString(3, produto.getCategoria());
            stmt.setBigDecimal(4, produto.getPreco());
            stmt.setInt(5, produto.getQuantidade());
            stmt.setInt(6, produto.getQuantidadeMinima());
            stmt.setInt(7, produto.getId());

            stmt.executeUpdate();
        }
    }

    /** Atualiza somente a quantidade em estoque (usado após uma venda ou entrada). */
    public void atualizarQuantidade(int produtoId, int novaQuantidade) throws SQLException {
        String sql = "UPDATE produto SET quantidade=? WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, novaQuantidade);
            stmt.setInt(2, produtoId);
            stmt.executeUpdate();
        }
    }

    public void inativar(int id) throws SQLException {
        String sql = "UPDATE produto SET ativo=0 WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Optional<Produto> buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM produto WHERE codigo=? AND ativo=1";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Produto> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Produto> listarTodos() throws SQLException {
        String sql = "SELECT * FROM produto WHERE ativo=1 ORDER BY nome";
        List<Produto> lista = new ArrayList<>();

        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Busca por nome ou código (usado na busca da tela de produtos/vendas). */
    public List<Produto> buscarPorNomeOuCodigo(String termo) throws SQLException {
        String sql = "SELECT * FROM produto WHERE ativo=1 AND (nome LIKE ? OR codigo LIKE ?) ORDER BY nome";
        List<Produto> lista = new ArrayList<>();

        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            String param = "%" + termo + "%";
            stmt.setString(1, param);
            stmt.setString(2, param);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setCategoria(rs.getString("categoria"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setQuantidade(rs.getInt("quantidade"));
        p.setQuantidadeMinima(rs.getInt("quantidade_minima"));
        Timestamp ts = rs.getTimestamp("data_cadastro");
        if (ts != null) {
            p.setDataCadastro(ts.toLocalDateTime());
        }
        p.setAtivo(rs.getBoolean("ativo"));
        return p;
    }
}
