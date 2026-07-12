package com.estoque.service;

import com.estoque.dao.ProdutoDAO;
import com.estoque.exception.CodigoDuplicadoException;
import com.estoque.exception.ProdutoNaoEncontradoException;
import com.estoque.model.Categoria;
import com.estoque.model.Fornecedor;
import com.estoque.model.Marca;
import com.estoque.model.Produto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProdutoService {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    public Produto cadastrar(String codigo, String nome, String descricao, Categoria categoria, Marca marca,
                             Fornecedor fornecedor, BigDecimal preco, BigDecimal custo, int quantidade,
                             int quantidadeMinima, String imagemPath)
            throws SQLException, CodigoDuplicadoException {

        validarCamposObrigatorios(codigo, nome, preco, quantidade);

        if (produtoDAO.buscarPorCodigo(codigo).isPresent()) {
            throw new CodigoDuplicadoException("Já existe um produto cadastrado com o código '" + codigo + "'.");
        }

        Produto produto = new Produto(codigo, nome, descricao, preco, quantidade, quantidadeMinima);
        produto.setCategoria(categoria);
        produto.setMarca(marca);
        produto.setFornecedor(fornecedor);
        produto.setCusto(custo != null ? custo : BigDecimal.ZERO);
        produto.setImagemPath(imagemPath);
        produtoDAO.inserir(produto);
        return produto;
    }

    public Produto darEntradaEstoque(String codigo, int quantidadeAdicional)
            throws SQLException, ProdutoNaoEncontradoException {

        Produto produto = buscarPorCodigo(codigo);
        produto.setQuantidade(produto.getQuantidade() + quantidadeAdicional);
        produtoDAO.atualizarQuantidade(produto.getId(), produto.getQuantidade());
        return produto;
    }

    public Produto buscarPorCodigo(String codigo) throws SQLException, ProdutoNaoEncontradoException {
        return produtoDAO.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ProdutoNaoEncontradoException(
                        "Nenhum produto encontrado com o código '" + codigo + "'."));
    }

    public List<Produto> listarTodos() throws SQLException {
        return produtoDAO.listarTodos();
    }

    public List<Produto> buscar(String termo) throws SQLException {
        if (termo == null || termo.isBlank()) {
            return produtoDAO.listarTodos();
        }
        return produtoDAO.buscarPorNomeOuCodigo(termo.trim());
    }

    public List<Produto> listarPorFornecedor(int fornecedorId) throws SQLException {
        return produtoDAO.listarPorFornecedor(fornecedorId);
    }

    public void atualizar(Produto produto) throws SQLException {
        produtoDAO.atualizar(produto);
    }

    public void remover(int id) throws SQLException {
        produtoDAO.inativar(id);
    }

    public List<Produto> listarTopPorQuantidade(int limite) throws SQLException {
        return produtoDAO.listarTopPorQuantidade(limite);
    }

    public List<Produto> listarEstoqueBaixo() throws SQLException {
        return produtoDAO.listarEstoqueBaixo();
    }

    public BigDecimal somarValorTotalEstoque() throws SQLException {
        return produtoDAO.somarValorTotalEstoque();
    }

    private void validarCamposObrigatorios(String codigo, String nome, BigDecimal preco, int quantidade) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código do produto é obrigatório.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }
    }
}