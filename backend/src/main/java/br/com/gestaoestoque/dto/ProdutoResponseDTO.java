package br.com.gestaoestoque.dto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
    Long id,
    String nome,
    BigDecimal preco,
    CategoriaResponseDTO categoria
) {}
