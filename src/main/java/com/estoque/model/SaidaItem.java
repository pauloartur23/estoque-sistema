package com.estoque.model;

/** Item (linha de produto) dentro de uma saída de estoque. */
public class SaidaItem {

    private int id;
    private Produto produto;
    private int quantidade;
    private String motivo;

    public SaidaItem() {
    }

    public SaidaItem(Produto produto, int quantidade, String motivo) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.motivo = motivo;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}