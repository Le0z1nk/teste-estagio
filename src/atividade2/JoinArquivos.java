package atividade2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JoinArquivos {

	static Map<String, String[]> cadastroANS = new HashMap<>();
	static Map<String, Double> somaPorRazaoEUf = new HashMap<>();
	static Map<String, List<Double>> valoresPorGrupo = new HashMap<>();

	public static void processarJoin(Path despesasCsv, Path cadastroCsv) throws Exception {
		carregarCadastro(cadastroCsv);
		gerarCSVJoin(despesasCsv);
		agrupar(Paths.get("resultado/consolidado_join.csv"));
		gerarCSVOrdenado();
	}

	static void carregarCadastro(Path arquivo) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
			String linha = reader.readLine(); 
			while ((linha = reader.readLine()) != null) {
				String[] colunas = linha.split(";");
				String registroAns = colunas[0].replace("\"", "").trim();
				String razaoSocial = colunas[2].replace("\"", "").trim();
				String cnpj = colunas[1].replace("\"", "").trim();
				String modalidade = colunas[4].replace("\"", "").trim();
				String uf = colunas[10].replace("\"", "").trim();
				cadastroANS.put(registroAns, new String[] { razaoSocial, cnpj, modalidade, uf });
			}
		}

		System.out.println("Cadastro carregado: " + cadastroANS.size() + " registros");
	}

	static void gerarCSVJoin(Path despesasCsv) throws IOException {
		Path output = Paths.get("resultado/consolidado_join.csv");
		Files.createDirectories(output.getParent());
		Set<String> jaProcessados = new HashSet<>();
		try (BufferedReader reader = Files.newBufferedReader(despesasCsv, StandardCharsets.UTF_8);
				BufferedWriter writer = Files.newBufferedWriter(output)) {
			writer.write("RegistroAns;RazãoSocial;CNPJ;Modalidade;UF;Trimestre;Ano;ValorDespesas");
			writer.newLine();
			String linha = reader.readLine(); 
			while ((linha = reader.readLine()) != null) {
				String[] colunas = linha.split(";");
				String registroAns = colunas[0].trim();
				String trimestre = colunas[2].trim();
				String ano = colunas[3].trim();
				String valor = colunas[4].trim();
				if (!cadastroANS.containsKey(registroAns))
					continue;
				
				String[] cadastro = cadastroANS.get(registroAns);
				String razaoSocial = cadastro[0];
				String cnpj = cadastro[1];
				String modalidade = cadastro[2];
				String uf = cadastro[3];
				String chaveUnica = registroAns + "-" + razaoSocial + "-" + trimestre;
				if (jaProcessados.contains(chaveUnica))
					continue;
				
				jaProcessados.add(chaveUnica);
				writer.write(String.join(";", registroAns, razaoSocial, cnpj, modalidade, uf, trimestre, ano, valor));
				writer.newLine();
			}
		}

		System.out.println("JOIN concluído!");
	}
	static void agrupar(Path joinCsv) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(joinCsv, StandardCharsets.UTF_8)) {
			String linha = reader.readLine();
			while ((linha = reader.readLine()) != null) {
				String[] colunas = linha.split(";"); 
				if (colunas.length < 8)
					continue;

				String razaoSocial = colunas[1].trim();
				String uf = colunas[4].trim();
				String valorTexto = colunas[7].trim();
				if (valorTexto.isEmpty())
					continue;

				double valor;
				try {
					valor = Double.parseDouble(valorTexto);
				} catch (NumberFormatException e) {
					continue; 
				}
				
				String chave = razaoSocial + "|" + uf;
				somaPorRazaoEUf.put(chave, somaPorRazaoEUf.getOrDefault(chave, 0.0) + valor);
				valoresPorGrupo.putIfAbsent(chave, new ArrayList<>());
				valoresPorGrupo.get(chave).add(valor);
			}
		}

		System.out.println("Agrupamento por Razão Social + UF concluído!");
	}

	static void gerarCSVOrdenado() throws IOException {
		Path output = Paths.get("resultado/despesas_agregadas.csv");
		Files.createDirectories(output.getParent());
		List<Map.Entry<String, Double>> listaOrdenada = new ArrayList<>(somaPorRazaoEUf.entrySet());
		listaOrdenada.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
		try (BufferedWriter writer = Files.newBufferedWriter(output)) {
			writer.write("RazaoSocial;UF;ValorTotal;MediaDespesas;DesvioPadrão");
			writer.newLine();
			for (var entry : listaOrdenada) {
				String[] partes = entry.getKey().split("\\|");
				String razao = partes[0];
				String uf = partes[1];
				double total = entry.getValue();
			    List<Double> valores = valoresPorGrupo.get(entry.getKey());
			    double media = calcularMedia(valores);
			    double desvio = calcularDesvioPadrao(valores, media);
				writer.write(String.join(";", razao, uf, String.format("%.2f", total).replace(",", "."), String.format("%.2f", media).replace(",", "."), String.format("%.2f", desvio).replace(",", ".")));
				writer.newLine();
			}
		}

		System.out.println("CSV gerado!");
	}

	static double calcularMedia(List<Double> valores) {
		double soma = 0;
		for (double v : valores)
			soma += v;
		return soma / valores.size();
	}

	static double calcularDesvioPadrao(List<Double> valores, double media) {
		double somaQuadrados = 0;
		for (double v : valores) {
			somaQuadrados += Math.pow(v - media, 2);
		}

		return Math.sqrt(somaQuadrados / valores.size());
	}

}
