package br.com.gestaoestoque.controller;

import br.com.gestaoestoque.dto.CategoriaRequestDTO;
import br.com.gestaoestoque.dto.CategoriaResponseDTO;
import br.com.gestaoestoque.entity.Categoria;
import br.com.gestaoestoque.service.CategoriaService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponseDTO> listar() {
        return categoriaService.listarTodas()
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoriaResponseDTO buscarPorId(@PathVariable Long id) {
        return paraResposta(categoriaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> criar(
            @Valid @RequestBody CategoriaRequestDTO dados) {

        Categoria categoria = categoriaService.criar(
                new Categoria(dados.nome()));

        CategoriaResponseDTO resposta = paraResposta(categoria);

        return ResponseEntity
                .created(URI.create("/api/categorias/" + categoria.getId()))
                .body(resposta);
    }

    @PutMapping("/{id}")
    public CategoriaResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO dados) {

        Categoria categoria = categoriaService.atualizar(
                id,
                new Categoria(dados.nome()));

        return paraResposta(categoria);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private CategoriaResponseDTO paraResposta(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome());
    }
}
