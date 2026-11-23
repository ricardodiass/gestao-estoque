package br.com.gestaoestoque.repository;

import br.com.gestaoestoque.entity.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Produto> findAll();

    @Override
    @EntityGraph(attributePaths = "categoria")
    Optional<Produto> findById(Long id);

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByCategoria_Id(Long categoriaId);

    boolean existsByCategoria_Id(Long categoriaId);
}
