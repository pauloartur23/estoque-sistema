package com.estoque.service;

import com.estoque.dao.ProdutoDAO;
import com.estoque.dao.SaidaEstoqueDAO;
import com.estoque.exception.EstoqueInsuficienteException;
import com.estoque.model.Produto;
import com.estoque.model.SaidaEstoque;
import com.estoque.model.SaidaItem;

import java.sql.SQLException;

public class SaidaEstoqueService {

    private final SaidaEstoqueDAO saidaDAO = new SaidaEstoqueDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    public SaidaItem adicionarItem(SaidaEstoque saida, Produto produto, int quantidade, String motivo)
            throws SQLException, EstoqueInsuficienteException {

        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        int jaNaSaida = saida.getItens().stream()
                .filter(i -> i.getProduto().getId() == produto.getId())
                .mapToInt(SaidaItem::getQuantidade)
                .sum();

        if (produto.getQuantidade() < quantidade + jaNaSaida) {
            throw new EstoqueInsuficienteException(
                    "Estoque insuficiente para '" + produto.getNome() + "'. Disponível: "
                            + produto.getQuantidade() + " un.");
        }

        SaidaItem item = new SaidaItem(produto, quantidade, motivo);
        saida.adicionarItem(item);
        return item;
    }

    public void finalizarSaida(SaidaEstoque saida) throws SQLException {
        if (saida.getItens().isEmpty()) {
            throw new IllegalStateException("Adicione ao menos um produto à saída.");
        }
        if (saida.getTipoSaida() == null || saida.getTipoSaida().isBlank()) {
            throw new IllegalStateException("Selecione o tipo de saída.");
        }
        saidaDAO.salvarComBaixaEstoque(saida);
    }

    public int contarSaidasNoMes() throws SQLException {
        return saidaDAO.contarSaidasNoMes();
    }
}