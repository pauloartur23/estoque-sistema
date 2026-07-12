package com.estoque.model;

import java.math.BigDecimal;

/** Item (linha de produto) dentro de uma entrada de estoque. */
public class EntradaItem {

    private int id;
    private Produto produto;
    private int quantidade;
    private BigDecimal custoUnitario;
    private BigDecimal subtotal;

    public EntradaItem() {
    }

    public EntradaItem(Produto produto, int quantidade, BigDecimal custoUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.subtotal = custoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public void setCustoUnitario(BigDecimal custoUnitario) {
        this.custoUnitario = custoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}