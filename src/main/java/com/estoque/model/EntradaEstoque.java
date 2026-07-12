package com.estoque.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Representa uma entrada de produtos no estoque (compra de fornecedor). */
public class EntradaEstoque {

    private int id;
    private Fornecedor fornecedor;
    private LocalDateTime dataEntrada;
    private String numeroNota;
    private String observacoes;
    private BigDecimal valorTotal;
    private List<EntradaItem> itens;

    public EntradaEstoque() {
        this.itens = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
    }

    public void adicionarItem(EntradaItem item) {
        this.itens.add(item);
        recalcularTotal();
    }

    public void removerItem(EntradaItem item) {
        this.itens.remove(item);
        recalcularTotal();
    }

    private void recalcularTotal() {
        this.valorTotal = itens.stream()
                .map(EntradaItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDateTime dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public String getNumeroNota() {
        return numeroNota;
    }

    public void setNumeroNota(String numeroNota) {
        this.numeroNota = numeroNota;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<EntradaItem> getItens() {
        return itens;
    }

    public void setItens(List<EntradaItem> itens) {
        this.itens = itens;
        recalcularTotal();
    }
}