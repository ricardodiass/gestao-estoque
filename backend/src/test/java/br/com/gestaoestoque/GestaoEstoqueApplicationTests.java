package br.com.gestaoestoque;

import br.com.gestaoestoque.repository.CategoriaRepository;
import br.com.gestaoestoque.repository.ProdutoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:gestaoestoque-tests;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "spring.jpa.open-in-view=false"
    }
)
class GestaoEstoqueApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void limparBanco() {
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    private HttpResponse<String> requisicao(
            String metodo,
            String caminho,
            String corpo) throws Exception {

        HttpRequest.Builder builder = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + caminho)
        );

        if (corpo != null) {
            builder.header("Content-Type", "application/json");
            builder.method(
                    metodo,
                    HttpRequest.BodyPublishers.ofString(corpo)
            );
        } else {
            builder.method(
                    metodo,
                    HttpRequest.BodyPublishers.noBody()
            );
        }

        return httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private long extrairId(String json) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)")
                .matcher(json);

        assertTrue(matcher.find(), "Resposta sem identificador: " + json);

        return Long.parseLong(matcher.group(1));
    }

    @Test
    void deveExecutarCrudCompletoDeCategoria() throws Exception {

        var cadastro = requisicao(
                "POST",
                "/api/categorias",
                """
                {"nome":"Periféricos"}
                """
        );

        assertEquals(201, cadastro.statusCode());

        long id = extrairId(cadastro.body());

        var listagem = requisicao(
                "GET",
                "/api/categorias",
                null
        );

        assertEquals(200, listagem.statusCode());
        assertTrue(listagem.body().contains("Periféricos"));

        var consulta = requisicao(
                "GET",
                "/api/categorias/" + id,
                null
        );

        assertEquals(200, consulta.statusCode());

        var atualizacao = requisicao(
                "PUT",
                "/api/categorias/" + id,
                """
                {"nome":"Informática"}
                """
        );

        assertEquals(200, atualizacao.statusCode());
        assertTrue(atualizacao.body().contains("Informática"));

        var exclusao = requisicao(
                "DELETE",
                "/api/categorias/" + id,
                null
        );

        assertEquals(204, exclusao.statusCode());

        var consultaFinal = requisicao(
                "GET",
                "/api/categorias/" + id,
                null
        );

        assertEquals(404, consultaFinal.statusCode());
    }

    @Test
    void deveExecutarCrudCompletoDeProdutoComCategoria()
            throws Exception {

        var cadastroCategoria = requisicao(
                "POST",
                "/api/categorias",
                """
                {"nome":"Periféricos"}
                """
        );

        assertEquals(201, cadastroCategoria.statusCode());

        long categoriaId = extrairId(cadastroCategoria.body());

        String produtoJson = """
                {
                    "nome":"Teclado Mecânico",
                    "preco":249.90,
                    "categoriaId":%d
                }
                """.formatted(categoriaId);

        var cadastroProduto = requisicao(
                "POST",
                "/api/produtos",
                produtoJson
        );

        assertEquals(201, cadastroProduto.statusCode());

        long produtoId = extrairId(cadastroProduto.body());

        assertTrue(cadastroProduto.body().contains("\"categoria\""));
        assertTrue(cadastroProduto.body().contains("Periféricos"));

        var listagem = requisicao(
                "GET",
                "/api/produtos",
                null
        );

        assertEquals(200, listagem.statusCode());
        assertTrue(listagem.body().contains("Teclado Mecânico"));

        var consulta = requisicao(
                "GET",
                "/api/produtos/" + produtoId,
                null
        );

        assertEquals(200, consulta.statusCode());
        assertTrue(consulta.body().contains("Periféricos"));

        var filtro = requisicao(
                "GET",
                "/api/produtos/categoria/" + categoriaId,
                null
        );

        assertEquals(200, filtro.statusCode());
        assertTrue(filtro.body().contains("Teclado Mecânico"));

        var atualizacao = requisicao(
                "PUT",
                "/api/produtos/" + produtoId,
                """
                {
                    "nome":"Teclado Mecânico RGB",
                    "preco":299.90,
                    "categoriaId":%d
                }
                """.formatted(categoriaId)
        );

        assertEquals(200, atualizacao.statusCode());
        assertTrue(atualizacao.body().contains("Teclado Mecânico RGB"));

        var exclusaoProduto = requisicao(
                "DELETE",
                "/api/produtos/" + produtoId,
                null
        );

        assertEquals(204, exclusaoProduto.statusCode());

        var exclusaoCategoria = requisicao(
                "DELETE",
                "/api/categorias/" + categoriaId,
                null
        );

        assertEquals(204, exclusaoCategoria.statusCode());
    }

    @Test
    void deveRejeitarDadosInvalidosERecursosInexistentes()
            throws Exception {

        var produtoInvalido = requisicao(
                "POST",
                "/api/produtos",
                """
                {
                    "nome":"",
                    "preco":-10,
                    "categoriaId":1
                }
                """
        );

        assertEquals(400, produtoInvalido.statusCode());
        assertTrue(produtoInvalido.body().contains("nome"));
        assertTrue(produtoInvalido.body().contains("preco"));

        var categoriaInvalida = requisicao(
                "POST",
                "/api/categorias",
                """
                {"nome":" "}
                """
        );

        assertEquals(400, categoriaInvalida.statusCode());

        var produtoInexistente = requisicao(
                "GET",
                "/api/produtos/999999",
                null
        );

        assertEquals(404, produtoInexistente.statusCode());

        var categoriaInexistente = requisicao(
                "GET",
                "/api/categorias/999999",
                null
        );

        assertEquals(404, categoriaInexistente.statusCode());
    }

    @Test
    void deveImpedirExclusaoDeCategoriaComProdutos()
            throws Exception {

        var categoria = requisicao(
                "POST",
                "/api/categorias",
                """
                {"nome":"Eletrônicos"}
                """
        );

        long categoriaId = extrairId(categoria.body());

        var produto = requisicao(
                "POST",
                "/api/produtos",
                """
                {
                    "nome":"Mouse Gamer",
                    "preco":159.90,
                    "categoriaId":%d
                }
                """.formatted(categoriaId)
        );

        assertEquals(201, produto.statusCode());

        var exclusao = requisicao(
                "DELETE",
                "/api/categorias/" + categoriaId,
                null
        );

        assertEquals(409, exclusao.statusCode());

        assertTrue(exclusao.body().contains("produtos vinculados"));
    }
}
