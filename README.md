
# Teste Estágio — Integração com API e Processamento de Dados (Java + Maven)

## Descrição

Este projeto consiste em uma aplicação Java desenvolvida com **Maven**, focada em **integração com API externa** e **processamento de dados**.

O objetivo do projeto é demonstrar habilidades em:

* Consumo de APIs REST
* Manipulação e processamento de dados
* Estruturação de código em Java
* Boas práticas de organização e arquitetura

---

## Funcionalidades

* Consumo de dados a partir de uma **API externa**
* Processamento e transformação de dados
* Filtragem, agregação e análise de informações
* Geração de saída estruturada (ex: console, arquivo ou relatório)

---

## Pré-requisitos

Antes de executar o projeto, você precisa ter instalado:

* **Java 11+**
* **Maven 3.6+**
* **Git**

---

## Instalação e Execução

### 1️. Clone o repositório

```bash
git clone https://github.com/Le0z1nk/teste-estagio.git
cd teste-estagio
```

### 2️. Compile o projeto

```bash
mvn clean install
```

### 3️. Execute a aplicação

```bash
mvn exec:java
```

Ou, se houver uma classe principal:

```bash
java -jar target/teste-estagio.jar
```

---
## IMPORTANTE
As atividades 1 e 2 devem ser executadas separadamente dentro do projeto maven e estão separadas em packages: atividade1 e atividade2.

# Atividade 1

## Visão Geral
Este projeto processa arquivos CSV extraídos de ZIPs, filtrando **Despesas com Eventos/Sinistros** e consolidando os dados em um único CSV final compactado.

O código utiliza **dois métodos de processamento**:
- Processamento incremental (linha a linha)
- Armazenamento dos resultados filtrados em memória

---

## 1. Processamento Incremental
### Trecho do código:
```java
try (BufferedReader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
    String linha = reader.readLine();
    while ((linha = reader.readLine()) != null) {
        ...
    }
}
```

### Benefícios:
- Baixo uso de memória
- Melhor desempenho para arquivos grandes
- Mais escalável

---

## 2. Processamento dos Resultados em Memória

Embora os arquivos sejam lidos incrementalmente, os **dados filtrados são armazenados em memória** na lista:

```java
static List<String[]> registrosFinais = new ArrayList<>();
```

### O que isso significa?
- CSVs brutos não ficam em memória
- Apenas registros válidos são armazenados
---

## 3. Filtro Aplicado

Somente registros de **Eventos/Sinistros** são mantidos:

```java
if (!descricao.contains("eventos/sinistros")) continue;
```

---
## 5. Análise Crítica

### Pontos positivos
- Leitura eficiente
- Escalável para arquivos grandes
- Código simples e organizado
---

## Conclusão
O código adota uma estratégia híbrida:
- Incremental na leitura
- Em memória na consolidação

Isso equilibra eficiência, simplicidade e desempenho.

---

## Tratamento de inconsistências no CSV

```java
if (valor <= 0) continue;
```
Se algum valor final for 0 ou menor que 0, o código pula a linha do csv que está com inconsistência
```java
static String normalizarTrimestre(String trimestre) {
        trimestre = trimestre.toUpperCase();
        if (trimestre.contains("-01-")) return "Q1";
        if (trimestre.contains("-04-")) return "Q2";
        if (trimestre.contains("-07-")) return "Q3";

        return "DESCONHECIDO";
    }
```
A coluna do csv que mostra a data é usada para mostrar no csv final qual é o trimestre respectivo de cada dado. Se o mês começar em 01 é o trimestre Q1, se começar em 04 é o trimestre Q2 e se começar em 07 é o trimestre Q3

---
# Atividade 2

## Visão Geral

Este projeto processa os dados
do arquivo CSV de despesas, realiza o *join* com o cadastro ANS, remove
registros duplicados e gera um arquivo consolidado.

## Tipo de Processamento

O processamento é feito de forma **incremental**, ou seja, **linha por
linha**, sem carregar o arquivo inteiro na memória. Isso torna o método
eficiente para arquivos grandes.

## Etapas do Processamento

### 1. Criação do Arquivo de Saída

O método cria a pasta `resultado` e define o caminho do arquivo final:

``` java
Path output = Paths.get("resultado/consolidado_join.csv");
Files.createDirectories(output.getParent());
```

### 2. Controle de Duplicidade

É utilizado um `HashSet` para armazenar chaves únicas e evitar linhas
duplicadas:

``` java
Set<String> jaProcessados = new HashSet<>();
```

### 3. Leitura e Escrita Simultâneas

O arquivo de despesas é lido linha por linha enquanto o CSV final é
escrito ao mesmo tempo.

### 5. Leitura Incremental das Linhas

Cada linha do CSV original é processada individualmente: - Divide a
linha em colunas - Extrai campos relevantes - Busca dados no cadastro
ANS - Ignora registros inexistentes

### 6. Realização do Join

Se o registro existir no cadastro ANS, os dados são combinados: - Razão
Social - CNPJ - Modalidade - UF

### 7. Remoção de Duplicados

Uma chave única é criada combinando:

    registroAns + razaoSocial + trimestre

Se a chave já existir, a linha é ignorada.

### 8. Escrita no CSV Consolidado

Os dados tratados são gravados diretamente no arquivo final.

## Benefícios da Abordagem

-   Baixo consumo de memória
-   Boa performance para arquivos grandes
-   Join em tempo real
-   Evita registros duplicados
-   Processamento escalável

## Conclusão

Essa estratégia é eficiente, segura e adequada para grandes volumes de
dados, garantindo integridade e desempenho.

------------------------------------------------------------------------

## Estratégia de Ordenação dos Dados

## Visão Geral da Estratégia

O código aplica uma **ordenação pós-agrupamento**, ou seja:
1. Primeiro os dados são **agrupados e somados**
2. Depois os resultados são **convertidos em lista**
3. A lista é **ordenada pelo valor total**
4. O CSV é gerado **já ordenado**

---

## Estrutura dos Dados

Os totais são armazenados no Map:

```
Map<String, Double> somaPorRazaoEUf
```

- **Chave:** `RazaoSocial|UF`
- **Valor:** soma total das despesas do grupo

Como `Map` não mantém ordem, é necessário convertê-lo em uma lista.

---

## Conversão do Map para Lista

```java
List<Map.Entry<String, Double>> listaOrdenada =
    new ArrayList<>(somaPorRazaoEUf.entrySet());
```

Isso permite aplicar ordenação usando `Collections.sort()`.

---

## 📊 Critério de Ordenação

```java
listaOrdenada.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
```

### O que isso faz:
- Compara os valores totais
- Ordena em **ordem decrescente**
- Maiores valores aparecem primeiro

---

# Atividade 3

Este código sql utiliza duas tabelas principais para análise de despesas de
operadoras:

-   `dados_operadoras` --- tabela de dados detalhados
-   `despesas_agregadas` --- tabela de dados agregados

A estrutura foi pensada para equilibrar **integridade dos dados**,
**volume esperado**, **performance** e **eficiência em consultas
analíticas**.

------------------------------------------------------------------------

## Estrutura das Tabelas

### Tabela `dados_operadoras`

Armazena os registros detalhados por operadora, trimestre e ano.

### Tabela `despesas_agregadas`

Armazena métricas estatísticas resumidas por operadora e estado

------------------------------------------------------------------------

## Por que `dados_operadoras` é normalizada?

A tabela segue os princípios fundamentais da normalização:

### Dados atômicos (1FN)

Cada linha representa **um único evento temporal**: - Uma operadora - Um
trimestre - Um valor de despesa

### Sem campos derivados

Os valores armazenados são **dados primários**, não cálculos.

### Integridade e consistência

Os dados permitem: - Reprocessamento estatístico - Auditoria -
Recalcular agregações sem perda de informação

### Adequada ao volume esperado

Essa tabela pode crescer bastante ao longo do tempo, mas: - Permite
**indexação eficiente** - Mantém **boa escalabilidade** - Suporta
análises históricas completas

------------------------------------------------------------------------

##  Por que `despesas_agregadas` é desnormalizada?

A tabela contém **dados derivados**, que poderiam ser recalculados a
partir da tabela principal:

### Armazena valores agregados

-   `valor_total` → soma
-   `media` → média estatística
-   `desvio_padrao` → cálculo estatístico

Esses valores **não são dados primários**, mas **resultados de
processamento**.

### Objetivo da desnormalização

A desnormalização foi adotada para:

-   Reduzir **complexidade de queries analíticas**
-   Melhorar **tempo de resposta em relatórios**
-   Evitar **reprocessamento pesado** sobre grandes volumes de dados
-   Facilitar **dashboards e análises frequentes**

------------------------------------------------------------------------

## Impacto no Volume de Dados

### `dados_operadoras`

-   Crescimento contínuo ao longo do tempo
-   Maior volume
-   Ideal para **consultas detalhadas e auditoria**

### `despesas_agregadas`

-   Volume menor
-   Atualização periódica
-   Ideal para **consultas rápidas e sumarizadas**

Essa separação melhora **performance geral** do sistema.

------------------------------------------------------------------------

## Impacto na Complexidade das Queries

### 🔹 Sem tabela agregada:

Consultas analíticas exigiriam: - `GROUP BY` extensos - Funções
estatísticas frequentes - Maior custo computacional

### 🔹 Com tabela agregada:

-   Queries mais **simples**
-   Menos custo de processamento
-   Melhor **legibilidade**
-   Melhor **tempo de resposta**

------------------------------------------------------------------------

## Justificativa Arquitetura

O modelo adota um padrão comum em **ambientes analíticos**:

-   **Tabela normalizada** → dados base confiáveis (fonte da verdade)
-   **Tabela desnormalizada** → otimização para análise e relatórios

------------------------------------------------------------------------

## Conclusão

-   A tabela `dados_operadoras` prioriza **integridade e
    rastreabilidade**
-   A tabela `despesas_agregadas` prioriza **performance e simplicidade
    analítica**

Esse equilíbrio torna o modelo **eficiente, escalável e adequado ao
volume de dados esperado**.

------------------------------------------------------------------------

## Tipos de dados

Foi usado DECIMAL para valores monetários e VARCHAR para datas

------------------------------------------------------------------------

### Justificativa

DECIMAL:
- Armazena números exatamente
- Ideal para valores financeiros
- Sem erro de arredondamento inesperado
- Mais seguro para dados críticos

VARCHAR:
Já que foi apenas usado a coluna ano, não é preciso usar DATE ou TIMESTAMP

------------------------------------------------------------------------

## Querys Analiticas

### Query 1

A query 1 trata das operadoras sem todos os trimestres do seguinte modo:
- Usa o primeiro trimestre disponível
- Usa o último trimestre disponível
- Não exige sequência completa
- Compara apenas os pontos existentes

------------------------------------------------------------------------

### Query 3

A query 3 foi resolvida do seguinte modo:
- Subquery calcula a média geral
```sql
SELECT AVG(media) FROM despesas_agregadas
```
- Filtra registros acima dessa média
```sql
WHERE d.valor_despesas > WHERE d.valor_despesas > (
        SELECT AVG(media)
        FROM despesas_agregadas
    )
```
- Conta em quantos trimestres cada operadora passou da média
```sql
COUNT(*) AS trimestres_acima_media
```
- Retorna apenas operadoras com ≥ 2 trimestres acima
```sql
WHERE trimestres_acima_media >= 2
```
