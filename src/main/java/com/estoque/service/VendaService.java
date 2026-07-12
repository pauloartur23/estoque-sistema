package com.estoque.service;

import com.estoque.dao.ProdutoDAO;
import com.estoque.dao.VendaDAO;
import com.estoque.exception.EstoqueInsuficienteException;
import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.ItemVenda;
import com.estoque.model.Produto;
import com.estoque.model.Venda;

import java.sql.SQLException;

public class VendaService {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();

    public ItemVenda adicionarItem(Venda venda, String codigoProduto, int quantidade)
            throws SQLException, ProdutoNaoEncontradoException, EstoqueInsuficienteException {

        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        Produto produto = produtoDAO.buscarPorCodigo(codigoProduto)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(
                        "Nenhum produto encontrado com o código '" + codigoProduto + "'."));

        int jaNaVenda = venda.getItens().stream()
                .filter(i -> i.getProduto().getId() == produto.getId())
                .mapToInt(ItemVenda::getQuantidade)
                .sum();

        if (produto.getQuantidade() < quantidade + jaNaVenda) {
            throw new EstoqueInsuficienteException(
                    "Estoque insuficiente para '" + produto.getNome() + "'. Disponível: "
                            + produto.getQuantidade() + " un.");
        }

        ItemVenda item = new ItemVenda(produto, quantidade);
        venda.adicionarItem(item);
        return item;
    }

    public void finalizarVenda(Venda venda) throws SQLException {
        if (venda.getItens().isEmpty()) {
            throw new IllegalStateException("Não é possível finalizar uma venda sem itens.");
        }
        if (venda.getFormaPagamento() == null || venda.getFormaPagamento().isBlank()) {
            throw new IllegalStateException("Selecione a forma de pagamento.");
        }
        vendaDAO.salvarVendaComBaixaEstoque(venda);
    }
}