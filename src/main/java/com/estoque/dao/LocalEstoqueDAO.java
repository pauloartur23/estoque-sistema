package com.estoque.dao;

import com.estoque.model.LocalEstoque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalEstoqueDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void inserir(LocalEstoque local) throws SQLException {
        String sql = "INSERT INTO local_estoque (nome, ativo) VALUES (?, 1)";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, local.getNome());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) local.setId(rs.getInt(1));
            }
        }
    }

    public Optional<LocalEstoque> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM local_estoque WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<LocalEstoque> listarTodos() throws SQLException {
        String sql = "SELECT * FROM local_estoque WHERE ativo=1 ORDER BY nome";
        List<LocalEstoque> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private LocalEstoque mapear(ResultSet rs) throws SQLException {
        LocalEstoque l = new LocalEstoque();
        l.setId(rs.getInt("id"));
        l.setNome(rs.getString("nome"));
        l.setAtivo(rs.getBoolean("ativo"));
        return l;
    }
}