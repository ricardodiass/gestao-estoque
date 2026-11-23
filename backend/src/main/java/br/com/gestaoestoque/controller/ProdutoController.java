package br.com.gestaoestoque.controller;

import br.com.gestaoestoque.dto.CategoriaResponseDTO;
import br.com.gestaoestoque.dto.ProdutoRequestDTO;
import br.com.gestaoestoque.dto.ProdutoResponseDTO;
import br.com.gestaoestoque.entity.Produto;
import br.com.gestaoestoque.service.ProdutoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<ProdutoResponseDTO> listar() {
        return produtoService.listarTodos()
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    @GetMapping("/{id}")
    public ProdutoResponseDTO buscarPorId(@PathVariable Long id) {
        return paraResposta(produtoService.buscarPorId(id));
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<ProdutoResponseDTO> listarPorCategoria(
            @PathVariable Long categoriaId) {

        return produtoService.listarPorCategoria(categoriaId)
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(
            @Valid @RequestBody ProdutoRequestDTO dados) {

        Produto produto = produtoService.criar(
                dados.nome(),
                dados.preco(),
                dados.categoriaId());

        ProdutoResponseDTO resposta = paraResposta(produto);

        return ResponseEntity
                .created(URI.create("/api/produtos/" + produto.getId()))
                .body(resposta);
    }

    @PutMapping("/{id}")
    public ProdutoResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoRequestDTO dados) {

        Produto produto = produtoService.atualizar(
                id,
                dados.nome(),
                dados.preco(),
                dados.categoriaId());

        return paraResposta(produto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private ProdutoResponseDTO paraResposta(Produto produto) {
        CategoriaResponseDTO categoria = new CategoriaResponseDTO(
                produto.getCategoria().getId(),
                produto.getCategoria().getNome());

        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                categoria);
    }
}
