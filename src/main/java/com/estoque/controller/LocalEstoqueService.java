package com.estoque.service;

import com.estoque.dao.LocalEstoqueDAO;
import com.estoque.model.LocalEstoque;

import java.sql.SQLException;
import java.util.List;

public class LocalEstoqueService {

    private final LocalEstoqueDAO localEstoqueDAO = new LocalEstoqueDAO();

    public LocalEstoque cadastrar(String nome) throws SQLException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do local é obrigatório.");
        }
        LocalEstoque local = new LocalEstoque(nome.trim());
        localEstoqueDAO.inserir(local);
        return local;
    }

    public List<LocalEstoque> listarTodos() throws SQLException {
        return localEstoqueDAO.listarTodos();
    }
}