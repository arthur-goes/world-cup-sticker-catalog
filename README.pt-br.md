[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

# World Cup Sticker Catalog

MVP de catalogo digital para consulta publica de estoque de figurinhas da Copa do Mundo, com area administrativa para gestao de inventario.

O objetivo principal e reduzir perguntas repetitivas no WhatsApp, permitindo que clientes consultem a disponibilidade das figurinhas antes de entrar em contato para compra.

## Stack

- Java 21
- Spring Boot 3
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- PostgreSQL
- Flyway
- Maven
- Docker Compose

## Funcionalidades Planejadas

- Catalogo publico de figurinhas
- Filtros avancados para consulta do estoque
- Suporte a figurinhas normais
- Suporte a variacoes Legends: Bronze, Prata e Ouro
- Area administrativa protegida
- Gestao de estoque
- Controle de inventario
- Versionamento de schema com migrations

## Requisitos

Antes de rodar o projeto localmente, verifique se possui:

- JDK 21
- Docker e Docker Compose
- Maven Wrapper, ja incluido no projeto

Para conferir o Java usado pelo Maven:

```bash
./mvnw -version
```

## Como Rodar Localmente

Suba as dependencias locais:

```bash
docker compose up -d
```

Execute os testes:

```bash
./mvnw test
```

Rode a aplicacao:

```bash
./mvnw spring-boot:run
```

A aplicacao ficara disponivel em:

```text
http://localhost:8080
```

Para parar as dependencias locais:

```bash
docker compose down
```

## Banco de Dados

O projeto utiliza PostgreSQL como banco de dados principal.

Durante o desenvolvimento local, o banco deve rodar separado da aplicacao, preferencialmente via Docker Compose. Em producao, a intencao e utilizar um PostgreSQL gerenciado, como Railway.

As alteracoes de schema devem ser versionadas com Flyway. A aplicacao nao deve depender de geracao automatica de schema pelo Hibernate em ambientes relevantes.

Configuracao esperada para JPA:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

## Diretrizes de Dominio

O dominio deve separar conceitos que parecem semelhantes, mas possuem responsabilidades diferentes:

- **Figurinha**: definicao de catalogo, como codigo, selecao, jogador, tipo e variante.
- **Item de estoque**: disponibilidade comercial de uma figurinha.
- **Movimento de inventario**: historico de entradas, saidas e ajustes de estoque.

Essa separacao evita misturar regras de catalogo, venda e inventario na mesma entidade.

## Arquitetura Esperada

O projeto deve evoluir de forma incremental, preservando baixo acoplamento entre as camadas:

- Controllers para entrada web
- Services ou application services para coordenacao de casos de uso
- Dominio para regras centrais
- Repositories para persistencia
- Templates Thymeleaf para renderizacao server-side

Filtros dinamicos do catalogo devem preferencialmente ser implementados com Spring Data JPA Specification, evitando combinacoes rigidas de metodos no repositorio.

## Status

Projeto em fase inicial de MVP.

Proximas decisoes tecnicas importantes:

- Consolidar o ambiente local com PostgreSQL
- Definir a primeira migration
- Modelar figurinhas, variantes e estoque
- Criar o catalogo publico
- Estruturar a area administrativa
