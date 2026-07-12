package com.estoque.service;

import com.estoque.dao.MarcaDAO;
import com.estoque.model.Marca;

import java.sql.SQLException;
import java.util.List;

public class MarcaService {

    private final MarcaDAO marcaDAO = new MarcaDAO();

    public Marca cadastrar(String nome) throws SQLException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da marca é obrigatório.");
        }
        Marca marca = new Marca(nome.trim());
        marcaDAO.inserir(marca);
        return marca;
    }

    public void atualizar(Marca marca) throws SQLException {
        marcaDAO.atualizar(marca);
    }

    public void remover(int id) throws SQLException {
        marcaDAO.inativar(id);
    }

    public List<Marca> listarTodas() throws SQLException {
        return marcaDAO.listarTodas();
    }
}