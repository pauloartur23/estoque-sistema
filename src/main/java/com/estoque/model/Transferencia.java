package com.estoque.model;

import java.time.LocalDateTime;

/** Representa a movimentação de um produto entre dois locais de estoque. */
public class Transferencia {

    private int id;
    private Produto produto;
    private LocalEstoque origem;
    private LocalEstoque destino;
    private int quantidade;
    private LocalDateTime dataTransferencia;
    private String observacoes;

    public Transferencia() {
    }

    public Transferencia(Produto produto, LocalEstoque origem, LocalEstoque destino, int quantidade) {
        this.produto = produto;
        this.origem = origem;
        this.destino = destino;
        this.quantidade = quantidade;
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

    public LocalEstoque getOrigem() {
        return origem;
    }

    public void setOrigem(LocalEstoque origem) {
        this.origem = origem;
    }

    public LocalEstoque getDestino() {
        return destino;
    }

    public void setDestino(LocalEstoque destino) {
        this.destino = destino;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataTransferencia() {
        return dataTransferencia;
    }

    public void setDataTransferencia(LocalDateTime dataTransferencia) {
        this.dataTransferencia = dataTransferencia;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}