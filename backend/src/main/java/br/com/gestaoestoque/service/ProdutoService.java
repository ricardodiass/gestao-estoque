package br.com.gestaoestoque.service;

import br.com.gestaoestoque.entity.Categoria;
import br.com.gestaoestoque.entity.Produto;
import br.com.gestaoestoque.exception.RecursoNaoEncontradoException;
import br.com.gestaoestoque.repository.CategoriaRepository;
import br.com.gestaoestoque.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Produto> listarPorCategoria(Long categoriaId) {
        verificarCategoria(categoriaId);
        return produtoRepository.findByCategoria_Id(categoriaId);
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado: " + id));
    }

    @Transactional
    public Produto criar(String nome, BigDecimal preco, Long categoriaId) {
        Categoria categoria = verificarCategoria(categoriaId);

        Produto produto = new Produto(nome, preco, categoria);

        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizar(
            Long id,
            String nome,
            BigDecimal preco,
            Long categoriaId) {

        Produto produto = buscarPorId(id);
        Categoria categoria = verificarCategoria(categoriaId);

        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setCategoria(categoria);

        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }

    private Categoria verificarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada: " + categoriaId));
    }
}
