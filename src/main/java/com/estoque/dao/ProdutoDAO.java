package com.estoque.dao;

import com.estoque.model.Categoria;
import com.estoque.model.Fornecedor;
import com.estoque.model.Marca;
import com.estoque.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO responsável por todo o acesso à tabela `produto`.
 * Faz JOIN com categoria, marca e fornecedor para já trazer os
 * objetos relacionados preenchidos.
 */
public class ProdutoDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    private static final String SELECT_BASE =
            "SELECT p.*, " +
                    "c.nome AS categoria_nome, " +
                    "m.nome AS marca_nome, " +
                    "f.nome AS fornecedor_nome, f.documento AS fornecedor_documento, " +
                    "f.telefone AS fornecedor_telefone, f.email AS fornecedor_email, f.endereco AS fornecedor_endereco " +
                    "FROM produto p " +
                    "LEFT JOIN categoria c ON c.id = p.categoria_id " +
                    "LEFT JOIN marca m ON m.id = p.marca_id " +
                    "LEFT JOIN fornecedor f ON f.id = p.fornecedor_id ";

    public void inserir(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto " +
                "(codigo, nome, descricao, categoria_id, marca_id, fornecedor_id, preco, custo, " +
                "quantidade, quantidade_minima, imagem_path, ativo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getCodigo());
            stmt.setString(2, produto.getNome());
            stmt.setString(3, produto.getDescricao());
            setNullableInt(stmt, 4, produto.getCategoria() != null ? produto.getCategoria().getId() : null);
            setNullableInt(stmt, 5, produto.getMarca() != null ? produto.getMarca().getId() : null);
            setNullableInt(stmt, 6, produto.getFornecedor() != null ? produto.getFornecedor().getId() : null);
            stmt.setBigDecimal(7, produto.getPreco());
            stmt.setBigDecimal(8, produto.getCusto());
            stmt.setInt(9, produto.getQuantidade());
            stmt.setInt(10, produto.getQuantidadeMinima());
            stmt.setString(11, produto.getImagemPath());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) produto.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome=?, descricao=?, categoria_id=?, marca_id=?, fornecedor_id=?, " +
                "preco=?, custo=?, quantidade=?, quantidade_minima=?, imagem_path=? WHERE id=?";

        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            setNullableInt(stmt, 3, produto.getCategoria() != null ? produto.getCategoria().getId() : null);
            setNullableInt(stmt, 4, produto.getMarca() != null ? produto.getMarca().getId() : null);
            setNullableInt(stmt, 5, produto.getFornecedor() != null ? produto.getFornecedor().getId() : null);
            stmt.setBigDecimal(6, produto.getPreco());
            stmt.setBigDecimal(7, produto.getCusto());
            stmt.setInt(8, produto.getQuantidade());
            stmt.setInt(9, produto.getQuantidadeMinima());
            stmt.setString(10, produto.getImagemPath());
            stmt.setInt(11, produto.getId());

            stmt.executeUpdate();
        }
    }

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
        String sql = SELECT_BASE + "WHERE p.codigo=? AND p.ativo=1";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Produto> buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Produto> listarTodos() throws SQLException {
        String sql = SELECT_BASE + "WHERE p.ativo=1 ORDER BY p.nome";
        List<Produto> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Produto> buscarPorNomeOuCodigo(String termo) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.ativo=1 AND (p.nome LIKE ? OR p.codigo LIKE ?) ORDER BY p.nome";
        List<Produto> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            String param = "%" + termo + "%";
            stmt.setString(1, param);
            stmt.setString(2, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Produto> listarTopPorQuantidade(int limite) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.ativo=1 ORDER BY p.quantidade DESC LIMIT ?";
        List<Produto> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Produto> listarEstoqueBaixo() throws SQLException {
        String sql = SELECT_BASE + "WHERE p.ativo=1 AND p.quantidade <= p.quantidade_minima ORDER BY p.quantidade ASC";
        List<Produto> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Produto> listarPorFornecedor(int fornecedorId) throws SQLException {
        String sql = SELECT_BASE + "WHERE p.ativo=1 AND p.fornecedor_id=? ORDER BY p.nome";
        List<Produto> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, fornecedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM produto WHERE ativo=1";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int contarEstoqueBaixo() throws SQLException {
        String sql = "SELECT COUNT(*) FROM produto WHERE ativo=1 AND quantidade <= quantidade_minima";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Soma do valor total em estoque (preço x quantidade) de todos os produtos ativos. */
    public java.math.BigDecimal somarValorTotalEstoque() throws SQLException {
        String sql = "SELECT COALESCE(SUM(preco * quantidade), 0) FROM produto WHERE ativo=1";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer valor) throws SQLException {
        if (valor == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, valor);
        }
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setCusto(rs.getBigDecimal("custo") != null ? rs.getBigDecimal("custo") : java.math.BigDecimal.ZERO);
        p.setQuantidade(rs.getInt("quantidade"));
        p.setQuantidadeMinima(rs.getInt("quantidade_minima"));
        p.setImagemPath(rs.getString("imagem_path"));

        int categoriaId = rs.getInt("categoria_id");
        if (!rs.wasNull()) {
            Categoria categoria = new Categoria();
            categoria.setId(categoriaId);
            categoria.setNome(rs.getString("categoria_nome"));
            p.setCategoria(categoria);
        }

        int marcaId = rs.getInt("marca_id");
        if (!rs.wasNull()) {
            Marca marca = new Marca();
            marca.setId(marcaId);
            marca.setNome(rs.getString("marca_nome"));
            p.setMarca(marca);
        }

        int fornecedorId = rs.getInt("fornecedor_id");
        if (!rs.wasNull()) {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setId(fornecedorId);
            fornecedor.setNome(rs.getString("fornecedor_nome"));
            fornecedor.setDocumento(rs.getString("fornecedor_documento"));
            fornecedor.setTelefone(rs.getString("fornecedor_telefone"));
            fornecedor.setEmail(rs.getString("fornecedor_email"));
            fornecedor.setEndereco(rs.getString("fornecedor_endereco"));
            p.setFornecedor(fornecedor);
        }

        Timestamp ts = rs.getTimestamp("data_cadastro");
        if (ts != null) p.setDataCadastro(ts.toLocalDateTime());
        p.setAtivo(rs.getBoolean("ativo"));
        return p;
    }
}