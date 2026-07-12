package com.estoque.service;

import com.estoque.dao.FornecedorDAO;
import com.estoque.model.Fornecedor;

import java.sql.SQLException;
import java.util.List;

public class FornecedorService {

    private final FornecedorDAO fornecedorDAO = new FornecedorDAO();

    public Fornecedor cadastrar(String nome, String documento, String telefone, String email, String endereco)
            throws SQLException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do fornecedor é obrigatório.");
        }
        Fornecedor fornecedor = new Fornecedor(nome.trim(), documento, telefone, email, endereco);
        fornecedorDAO.inserir(fornecedor);
        return fornecedor;
    }

    public void atualizar(Fornecedor fornecedor) throws SQLException {
        fornecedorDAO.atualizar(fornecedor);
    }

    public void remover(int id) throws SQLException {
        fornecedorDAO.inativar(id);
    }

    public List<Fornecedor> listarTodos() throws SQLException {
        return fornecedorDAO.listarTodos();
    }

    public List<Fornecedor> buscar(String termo) throws SQLException {
        if (termo == null || termo.isBlank()) {
            return fornecedorDAO.listarTodos();
        }
        return fornecedorDAO.buscarPorNome(termo.trim());
    }
}