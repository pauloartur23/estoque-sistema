package com.estoque.service;

import com.estoque.dao.ClienteDAO;
import com.estoque.model.Cliente;

import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public Cliente cadastrar(String nome, String documento, String telefone, String email, String endereco)
            throws SQLException {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        Cliente cliente = new Cliente(nome.trim(), documento, telefone, email, endereco);
        clienteDAO.inserir(cliente);
        return cliente;
    }

    public void atualizar(Cliente cliente) throws SQLException {
        clienteDAO.atualizar(cliente);
    }

    public void remover(int id) throws SQLException {
        clienteDAO.inativar(id);
    }

    public List<Cliente> listarTodos() throws SQLException {
        return clienteDAO.listarTodos();
    }

    public List<Cliente> buscar(String termo) throws SQLException {
        if (termo == null || termo.isBlank()) {
            return clienteDAO.listarTodos();
        }
        return clienteDAO.buscarPorNome(termo.trim());
    }

    public int contarTotal() throws SQLException {
        return clienteDAO.contarTotal();
    }
}