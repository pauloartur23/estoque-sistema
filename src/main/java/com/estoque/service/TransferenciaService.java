package com.estoque.service;

import com.estoque.dao.TransferenciaDAO;
import com.estoque.model.LocalEstoque;
import com.estoque.model.Produto;
import com.estoque.model.Transferencia;

import java.sql.SQLException;
import java.util.List;

public class TransferenciaService {

    private final TransferenciaDAO transferenciaDAO = new TransferenciaDAO();

    public Transferencia registrar(Produto produto, LocalEstoque origem, LocalEstoque destino, int quantidade,
                                   String observacoes) throws SQLException {

        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (origem.getId() == destino.getId()) {
            throw new IllegalArgumentException("O local de origem e destino não podem ser o mesmo.");
        }
        if (produto.getQuantidade() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente para transferir essa quantidade.");
        }

        Transferencia transferencia = new Transferencia(produto, origem, destino, quantidade);
        transferencia.setObservacoes(observacoes);
        transferenciaDAO.salvar(transferencia);
        return transferencia;
    }

    public List<Transferencia> listarTodas() throws SQLException {
        return transferenciaDAO.listarTodas();
    }
}