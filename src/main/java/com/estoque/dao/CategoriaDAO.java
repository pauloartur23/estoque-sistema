package com.estoque.dao;

import com.estoque.model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void inserir(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nome, descricao, ativo) VALUES (?, ?, 1)";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) categoria.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categoria SET nome=?, descricao=? WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.setInt(3, categoria.getId());
            stmt.executeUpdate();
        }
    }

    public void inativar(int id) throws SQLException {
        String sql = "UPDATE categoria SET ativo=0 WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Optional<Categoria> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Categoria> listarTodas() throws SQLException {
        String sql = "SELECT * FROM categoria WHERE ativo=1 ORDER BY nome";
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM categoria WHERE ativo=1";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setDescricao(rs.getString("descricao"));
        c.setAtivo(rs.getBoolean("ativo"));
        return c;
    }
}