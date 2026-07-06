package com.estoque.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma venda completa (cabeçalho + itens), equivalente aos
 * dados de uma Nota Fiscal.
 */
public class Venda {

    private int id;
    private LocalDateTime dataVenda;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String nomeCliente;
    private List<ItemVenda> itens;

    public Venda() {
        this.itens = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
        this.nomeCliente = "Consumidor Final";
    }

    /** Adiciona um item e recalcula o valor total da venda. */
    public void adicionarItem(ItemVenda item) {
        this.itens.add(item);
        recalcularTotal();
    }

    public void removerItem(ItemVenda item) {
        this.itens.remove(item);
        recalcularTotal();
    }

    private void recalcularTotal() {
        this.valorTotal = itens.stream()
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemVenda> itens) {
        this.itens = itens;
        recalcularTotal();
    }
}
