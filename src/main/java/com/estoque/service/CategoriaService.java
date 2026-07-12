package com.estoque.service;

import com.estoque.dao.CategoriaDAO;
import com.estoque.model.Categoria;

import java.sql.SQLException;
import java.util.List;

public class CategoriaService {

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public Categoria cadastrar(String nome, String descricao) throws SQLException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        Categoria categoria = new Categoria(nome.trim(), descricao);
        categoriaDAO.inserir(categoria);
        return categoria;
    }

    public void atualizar(Categoria categoria) throws SQLException {
        categoriaDAO.atualizar(categoria);
    }

    public void remover(int id) throws SQLException {
        categoriaDAO.inativar(id);
    }

    public List<Categoria> listarTodas() throws SQLException {
        return categoriaDAO.listarTodas();
    }

    public int contarTotal() throws SQLException {
        return categoriaDAO.contarTotal();
    }
}