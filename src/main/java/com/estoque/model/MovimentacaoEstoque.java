package com.estoque.model;

import java.time.LocalDateTime;

/**
 * Registro unificado de toda movimentação de estoque (entrada, saída,
 * venda, transferência). Alimenta a tabela "Últimas Movimentações" do
 * Dashboard.
 */
public class MovimentacaoEstoque {

    public enum Tipo {
        ENTRADA, SAIDA, VENDA, TRANSFERENCIA
    }

    private int id;
    private Tipo tipo;
    private Produto produto;
    private int quantidade;
    private String descricao;
    private LocalDateTime dataMovimento;
    private Integer referenciaId;

    public MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(Tipo tipo, Produto produto, int quantidade, String descricao, Integer referenciaId) {
        this.tipo = tipo;
        this.produto = produto;
        this.quantidade = quantidade;
        this.descricao = descricao;
        this.referenciaId = referenciaId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataMovimento() {
        return dataMovimento;
    }

    public void setDataMovimento(LocalDateTime dataMovimento) {
        this.dataMovimento = dataMovimento;
    }

    public Integer getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Integer referenciaId) {
        this.referenciaId = referenciaId;
    }
}