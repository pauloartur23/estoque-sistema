package com.estoque.service;

import com.estoque.dao.EntradaEstoqueDAO;
import com.estoque.model.EntradaEstoque;
import com.estoque.model.EntradaItem;
import com.estoque.model.Produto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class EntradaEstoqueService {

    private final EntradaEstoqueDAO entradaDAO = new EntradaEstoqueDAO();

    public EntradaItem adicionarItem(EntradaEstoque entrada, Produto produto, int quantidade, BigDecimal custoUnitario) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        if (custoUnitario == null || custoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O custo unitário não pode ser negativo.");
        }
        EntradaItem item = new EntradaItem(produto, quantidade, custoUnitario);
        entrada.adicionarItem(item);
        return item;
    }

    public void finalizarEntrada(EntradaEstoque entrada) throws SQLException {
        if (entrada.getItens().isEmpty()) {
            throw new IllegalStateException("Adicione ao menos um produto à entrada.");
        }
        if (entrada.getFornecedor() == null) {
            throw new IllegalStateException("Selecione o fornecedor.");
        }
        entradaDAO.salvarComEntradaNoEstoque(entrada);
    }

    public List<EntradaEstoque> listarTodas() throws SQLException {
        return entradaDAO.listarTodas();
    }

    public int contarEntradasNoMes() throws SQLException {
        return entradaDAO.contarEntradasNoMes();
    }
}