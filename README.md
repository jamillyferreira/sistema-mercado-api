# Sistema de Mercado

API para gestão de **vendas fiado** e **controle de estoque** de pequenos comércios. Projeto voluntário, 
desenvolvido para um mercadinho de bairro.

> 🚧 **Status:** em desenvolvimento. A fundação do projeto (banco, migrations, CI) está pronta, e a implementação 
> do domínio está começando.

## Problema que resolve

Pequenos comércios costumam controlar o fiado e o estoque em cadernos ou planilhas. Isso dificulta saber quanto 
cada cliente deve, o que foi pago e o que realmente tem no estoque.

## Funcionalidades planejadas

- Cadastro de clientes, funcionários, categorias e produtos
- Registro de vendas, inclusive fiado
- Recebimento de pagamentos (totais ou parciais)
- Controle de estoque por movimentações (entradas, saídas, ajustes)
- Cancelamento e estorno, sem apagar histórico
- Perfis de acesso: administrador e funcionário

## Arquitetura

**Monolito modular organizado por domínio.** Cada módulo é um pacote isolado, com suas próprias entidades, 
repositório e serviço:

- `cliente`
- `produto`
- `estoque`
- `fiado`

A escolha evita a bagunça de uma estrutura em camadas globais e mantém o caminho aberto para extrair um módulo no 
futuro, se for necessário.

## Tecnologias

- Java e Spring Boot
- PostgreSQL
- Flyway (migrations versionadas)
- Docker e Docker Compose (banco local)
- GitHub Actions (CI)

## Como rodar localmente

**Pré-requisitos:** Java 21, Docker e Docker Compose.

1. Clone o repositório:
   ```bash
   git clone https://github.com/jamillyferreira/sistema-mercado-api.git
   cd sistema-mercado-api
   ```
2. Copie o `.env.example` para `.env`
3. Suba o banco:
   ```bash
   docker compose up -d
   ```
4. Rode a aplicação com o perfil `dev` (as migrations do Flyway são aplicadas na inicialização).

## Fluxo de trabalho

- Git Flow, com `main` e `develop` protegidas
- Uma branch por assunto, e tudo entra via pull request
- Commits seguem Conventional Commits
- O CI roda o build e os testes em cada pull request

## Autora

**Jamilly Ferreira**, Desenvolvedora backend Java.
[LinkedIn](https://www.linkedin.com/in/jamillyferreira/)