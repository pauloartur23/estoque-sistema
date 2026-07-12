package com.estoque.service;

import com.estoque.dao.PedidoDAO;
import com.estoque.model.Fornecedor;
import com.estoque.model.Pedido;
import com.estoque.model.PedidoItem;
import com.estoque.model.Produto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PedidoService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    public PedidoItem adicionarItem(Pedido pedido, Produto produto, int quantidade, BigDecimal precoUnitario) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        PedidoItem item = new PedidoItem(produto, quantidade, precoUnitario);
        pedido.adicionarItem(item);
        return item;
    }

    public void criarPedido(Pedido pedido, Fornecedor fornecedor) throws SQLException {
        if (pedido.getItens().isEmpty()) {
            throw new IllegalStateException("Adicione ao menos um produto ao pedido.");
        }
        pedido.setFornecedor(fornecedor);
        pedidoDAO.salvar(pedido);
    }

    public void atualizarStatus(int pedidoId, Pedido.Status status) throws SQLException {
        pedidoDAO.atualizarStatus(pedidoId, status);
    }

    public List<Pedido> listarTodos() throws SQLException {
        return pedidoDAO.listarTodos();
    }

    public List<Pedido> listarAbertos() throws SQLException {
        return pedidoDAO.listarAbertos();
    }

    public int contarAbertos() throws SQLException {
        return pedidoDAO.contarAbertos();
    }
}