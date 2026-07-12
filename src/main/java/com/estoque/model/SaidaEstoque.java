package com.estoque.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Representa uma saída de produtos do estoque que NÃO é venda (avaria, perda, uso interno, devolução). */
public class SaidaEstoque {

    private int id;
    private String tipoSaida;
    private Cliente cliente;
    private LocalDateTime dataSaida;
    private String observacoes;
    private List<SaidaItem> itens;

    public SaidaEstoque() {
        this.itens = new ArrayList<>();
    }

    public void adicionarItem(SaidaItem item) {
        this.itens.add(item);
    }

    public void removerItem(SaidaItem item) {
        this.itens.remove(item);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoSaida() {
        return tipoSaida;
    }

    public void setTipoSaida(String tipoSaida) {
        this.tipoSaida = tipoSaida;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDateTime dataSaida) {
        this.dataSaida = dataSaida;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<SaidaItem> getItens() {
        return itens;
    }

    public void setItens(List<SaidaItem> itens) {
        this.itens = itens;
    }
}