package com.estoque.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Representa um pedido de compra feito a um fornecedor (ainda não recebido no estoque). */
public class Pedido {

    public enum Status {
        PENDENTE, APROVADO, RECEBIDO, CANCELADO
    }

    private int id;
    private Fornecedor fornecedor;
    private LocalDateTime dataPedido;
    private Status status;
    private BigDecimal valorTotal;
    private String observacoes;
    private List<PedidoItem> itens;

    public Pedido() {
        this.itens = new ArrayList<>();
        this.valorTotal = BigDecimal.ZERO;
        this.status = Status.PENDENTE;
    }

    public void adicionarItem(PedidoItem item) {
        this.itens.add(item);
        recalcularTotal();
    }

    public void removerItem(PedidoItem item) {
        this.itens.remove(item);
        recalcularTotal();
    }

    private void recalcularTotal() {
        this.valorTotal = itens.stream()
                .map(PedidoItem::getSubtotal)
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

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<PedidoItem> getItens() {
        return itens;
    }

    public void setItens(List<PedidoItem> itens) {
        this.itens = itens;
        recalcularTotal();
    }
}