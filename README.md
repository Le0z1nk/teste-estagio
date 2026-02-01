
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
