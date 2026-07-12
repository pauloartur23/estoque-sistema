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
    private Cliente cliente;
    private LocalDateTime dataVenda;
    private BigDecimal valorTotal;
    private BigDecimal desconto;
    private String formaPagamento;
    private String nomeCliente;
    private String observacoes;
    private List<ItemVenda> itens;

    public Venda() {
        this.itens = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
        this.desconto = BigDecimal.ZERO;
        this.nomeCliente = "Consumidor Final";
    }

    public void adicionarItem(ItemVenda item) {
        this.itens.add(item);
        recalcularTotal();
    }

    public void removerItem(ItemVenda item) {
        this.itens.remove(item);
        recalcularTotal();
    }

    private void recalcularTotal() {
        BigDecimal subtotalItens = itens.stream()
                .map(ItemVenda::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorTotal = subtotalItens.subtract(desconto == null ? BigDecimal.ZERO : desconto);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
        recalcularTotal();
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

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemVenda> itens) {
        this.itens = itens;
        recalcularTotal();
    }
}