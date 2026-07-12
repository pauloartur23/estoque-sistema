package com.estoque.dao;

import com.estoque.model.Marca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarcaDAO {

    private final ConexaoFactory conexaoFactory = ConexaoFactory.getInstance();

    public void inserir(Marca marca) throws SQLException {
        String sql = "INSERT INTO marca (nome, ativo) VALUES (?, 1)";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, marca.getNome());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) marca.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Marca marca) throws SQLException {
        String sql = "UPDATE marca SET nome=? WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, marca.getNome());
            stmt.setInt(2, marca.getId());
            stmt.executeUpdate();
        }
    }

    public void inativar(int id) throws SQLException {
        String sql = "UPDATE marca SET ativo=0 WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Optional<Marca> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM marca WHERE id=?";
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Marca> listarTodas() throws SQLException {
        String sql = "SELECT * FROM marca WHERE ativo=1 ORDER BY nome";
        List<Marca> lista = new ArrayList<>();
        try (Connection con = conexaoFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Marca mapear(ResultSet rs) throws SQLException {
        Marca m = new Marca();
        m.setId(rs.getInt("id"));
        m.setNome(rs.getString("nome"));
        m.setAtivo(rs.getBoolean("ativo"));
        return m;
    }
}