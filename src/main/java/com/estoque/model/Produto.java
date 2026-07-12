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
    private Categoria categoria;
    private Marca marca;
    private Fornecedor fornecedor;
    private BigDecimal preco;
    private BigDecimal custo;
    private int quantidade;
    private int quantidadeMinima;
    private String imagemPath;
    private LocalDateTime dataCadastro;
    private boolean ativo;

    public Produto() {
        this.custo = BigDecimal.ZERO;
    }

    public Produto(String codigo, String nome, String descricao, BigDecimal preco,
                   int quantidade, int quantidadeMinima) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidade = quantidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custo = BigDecimal.ZERO;
        this.ativo = true;
    }

    public boolean isEstoqueBaixo() {
        return quantidade <= quantidadeMinima;
    }

    public boolean temImagem() {
        return imagemPath != null && !imagemPath.isBlank();
    }

    /** Margem de lucro unitária (preço de venda - custo). */
    public BigDecimal getMargemLucro() {
        BigDecimal c = custo == null ? BigDecimal.ZERO : custo;
        return preco.subtract(c);
    }

    /** Valor total do estoque desse produto (preço x quantidade). */
    public BigDecimal getValorEmEstoque() {
        return preco.multiply(BigDecimal.valueOf(quantidade));
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public void setCusto(BigDecimal custo) {
        this.custo = custo;
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

    public String getImagemPath() {
        return imagemPath;
    }

    public void setImagemPath(String imagemPath) {
        this.imagemPath = imagemPath;
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