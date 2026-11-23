package br.com.gestaoestoque.service;

import br.com.gestaoestoque.entity.Categoria;
import br.com.gestaoestoque.exception.CategoriaEmUsoException;
import br.com.gestaoestoque.exception.RecursoNaoEncontradoException;
import br.com.gestaoestoque.repository.CategoriaRepository;
import br.com.gestaoestoque.repository.ProdutoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada: " + id));
    }

    @Transactional
    public Categoria criar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria dados) {
        Categoria categoria = buscarPorId(id);
        categoria.setNome(dados.getNome());

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);

        if (produtoRepository.existsByCategoria_Id(id)) {
            throw new CategoriaEmUsoException(
                    "Não é possível excluir uma categoria que possui produtos vinculados.");
        }

        categoriaRepository.delete(categoria);
    }
}
