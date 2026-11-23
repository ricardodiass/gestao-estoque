# Sistema de Gestão de Estoque

Trabalho Prático 2 — API REST Relacional e Interface Gráfica (GUI).

Sistema Fullstack para gerenciamento de produtos e categorias, desenvolvido com Java, Spring Boot, Spring Data JPA, H2 e interface Web.

## 1. Tecnologias utilizadas

### Backend
- Java 21
- Spring Boot 4.2.0
- Spring Web
- Spring Data JPA
- Spring Validation
- Maven
- H2 Database

### Frontend
- HTML5
- CSS3
- JavaScript
- Fetch API

## 2. Funcionalidades

- CRUD completo de produtos.
- CRUD completo de categorias.
- Relacionamento entre produtos e categorias.
- Pesquisa de produtos.
- Filtro por categoria.
- Dashboard com indicadores.
- Validação de campos obrigatórios.
- Tratamento de erros HTTP.
- Proteção contra exclusão de categorias vinculadas.

## 3. Modelagem do banco de dados

### Categoria
- id: identificador.
- nome: nome da categoria.

### Produto
- id: identificador.
- nome: nome do produto.
- preco: preço.
- categoria_id: chave estrangeira.

### Relacionamento

Uma categoria pode possuir vários produtos (1:N).

- Categoria: @OneToMany
- Produto: @ManyToOne
- Chave estrangeira: categoria_id.

## 4. Estrutura do projeto

- backend/: aplicação Spring Boot.
- frontend/: interface HTML, CSS e JavaScript.

## 5. Como executar

### Backend

No terminal:

    cd backend
    mvn test
    mvn spring-boot:run

API: http://localhost:8081

### Frontend

Em outro terminal:

    cd frontend
    python3 -m http.server 5500

Interface: http://localhost:5500

### Banco H2

Console: http://localhost:8081/h2-console

JDBC URL: jdbc:h2:file:./data/gestaoestoque

Usuário: sa

Senha: em branco.

## 6. Endpoints da API

### Categorias

| Método | Endpoint | Descrição |
|---|---|---|
| GET | /api/categorias | Listar categorias |
| GET | /api/categorias/{id} | Buscar categoria |
| POST | /api/categorias | Cadastrar categoria |
| PUT | /api/categorias/{id} | Atualizar categoria |
| DELETE | /api/categorias/{id} | Excluir categoria |

### Produtos

| Método | Endpoint | Descrição |
|---|---|---|
| GET | /api/produtos | Listar produtos |
| GET | /api/produtos/{id} | Buscar produto |
| GET | /api/produtos/categoria/{categoriaId} | Filtrar por categoria |
| POST | /api/produtos | Cadastrar produto |
| PUT | /api/produtos/{id} | Atualizar produto |
| DELETE | /api/produtos/{id} | Excluir produto |

## 7. Exemplos de JSON

### Cadastro de categoria

    {
      "nome": "Eletrônicos"
    }

### Cadastro de produto

    {
      "nome": "Mouse Gamer",
      "preco": 159.90,
      "categoriaId": 1
    }

### Resposta de produto

    {
      "id": 1,
      "nome": "Mouse Gamer",
      "preco": 159.90,
      "categoria": {
        "id": 1,
        "nome": "Eletrônicos"
      }
    }

## 8. Validações e tratamento de erros

- Nome do produto obrigatório.
- Nome da categoria obrigatório.
- Preço obrigatório e positivo.
- Categoria obrigatória.
- Limites de caracteres.

| Código HTTP | Descrição |
|---|---|
| 200 | Sucesso |
| 201 | Registro criado |
| 204 | Registro excluído |
| 400 | Dados inválidos |
| 404 | Registro não encontrado |
| 409 | Conflito de integridade |

## 9. Testes realizados

- Cadastro, consulta, atualização e exclusão de produtos.
- Cadastro, consulta e exclusão de categorias.
- Teste do relacionamento entre as entidades.
- Teste de validações HTTP 400.
- Teste de recurso inexistente HTTP 404.
- Teste de conflito HTTP 409.
- Testes de integração pela interface gráfica.
- Compilação e testes com Maven.

