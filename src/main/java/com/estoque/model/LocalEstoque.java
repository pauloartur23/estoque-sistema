package com.estoque.model;

public class LocalEstoque {

    private int id;
    private String nome;
    private boolean ativo;

    public LocalEstoque() {
    }

    public LocalEstoque(String nome) {
        this.nome = nome;
        this.ativo = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return nome;
    }
}