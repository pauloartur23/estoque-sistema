package com.estoque.dao;

import com.estoque.model.Fornecedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FornecedorDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void inserir(Fornecedor fornecedor) throws SQLException {
        String sql = "INSERT INTO fornecedor (nome, documento, telefone, email, endereco, ativo) " +
                "VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, fornecedor.getNome());
            stmt.setString(2, fornecedor.getDocumento());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEmail());
            stmt.setString(5, fornecedor.getEndereco());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) fornecedor.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Fornecedor fornecedor) throws SQLException {
        String sql = "UPDATE fornecedor SET nome=?, documento=?, telefone=?, email=?, endereco=? WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, fornecedor.getNome());
            stmt.setString(2, fornecedor.getDocumento());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEmail());
            stmt.setString(5, fornecedor.getEndereco());
            stmt.setInt(6, fornecedor.getId());
            stmt.executeUpdate();
        }
    }

    public void inativar(int id) throws SQLException {
        String sql = "UPDATE fornecedor SET ativo=0 WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Optional<Fornecedor> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM fornecedor WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Fornecedor> listarTodos() throws SQLException {
        String sql = "SELECT * FROM fornecedor WHERE ativo=1 ORDER BY nome";
        List<Fornecedor> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Fornecedor> buscarPorNome(String termo) throws SQLException {
        String sql = "SELECT * FROM fornecedor WHERE ativo=1 AND nome LIKE ? ORDER BY nome";
        List<Fornecedor> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, "%" + termo + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Fornecedor mapear(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setId(rs.getInt("id"));
        f.setNome(rs.getString("nome"));
        f.setDocumento(rs.getString("documento"));
        f.setTelefone(rs.getString("telefone"));
        f.setEmail(rs.getString("email"));
        f.setEndereco(rs.getString("endereco"));
        f.setAtivo(rs.getBoolean("ativo"));
        return f;
    }
}