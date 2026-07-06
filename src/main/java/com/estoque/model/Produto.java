package com.estoque.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um produto do estoque.
 * Classe de domínio (Model) — não conhece banco de dados nem telas.
 */
public class Produto {

    private int id;
    private String codigo;
    private String nome;
    private String descricao;
    private String categoria;
    private BigDecimal preco;
    private int quantidade;
    private int quantidadeMinima;
    private LocalDateTime dataCadastro;
    private boolean ativo;

    public Produto() {
    }

    public Produto(String codigo, String nome, String descricao, String categoria,
                   BigDecimal preco, int quantidade, int quantidadeMinima) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
        this.quantidade = quantidade;
        this.quantidadeMinima = quantidadeMinima;
        this.ativo = true;
    }

    /** Regra de negócio: indica se o produto está abaixo do estoque mínimo. */
    public boolean isEstoqueBaixo() {
        return quantidade <= quantidadeMinima;
    }

    // ----- Getters e Setters -----

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(int quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}