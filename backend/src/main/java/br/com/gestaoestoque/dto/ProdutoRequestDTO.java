package br.com.gestaoestoque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record ProdutoRequestDTO(

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    String nome,

    @NotNull(message = "O preço é obrigatório")
    @Positive(message = "O preço deve ser maior que zero")
    @Digits(integer = 10, fraction = 2,
            message = "O preço deve ter até 10 dígitos inteiros e 2 decimais")
    BigDecimal preco,

    @NotNull(message = "A categoria é obrigatória")
    Long categoriaId

) {}
