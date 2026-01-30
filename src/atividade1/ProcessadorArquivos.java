package atividade1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ProcessadorArquivos {

	static List<String[]> registrosFinais = new ArrayList<>();

    public static void processarCSVs(Path pastaExtraida) throws IOException {
        Files.walk(pastaExtraida)
                .filter(path -> path.toString().endsWith(".csv"))
                .forEach(csv -> {
                    try {
                        lerCSV(csv);
                    } catch (Exception e) {
                        System.out.println("Erro ao ler: " + csv);
                    }
                });

        gerarCSVFinal();
        ziparResultado();
    }

    static void lerCSV(Path arquivo) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8);) {
            String linha = reader.readLine(); // pula cabeçalho
            while ((linha = reader.readLine()) != null) {
                String[] colunas = linha.split(";");
                String data = colunas[0].replace("\"", "").substring(0, 4);
                String regAns = colunas[1].replace("\"", "");
                String descricao = colunas[3].replace("\"", "").toLowerCase();
                String trimestre = normalizarTrimestre(colunas[0]);
                String valorTexto = colunas[5].replace("\"", "").trim();
                if (valorTexto.isEmpty()) continue;
                
                double valor = Double.parseDouble(colunas[5].replace(".", "").replace(",", "."));
                boolean ehEventoOuSinistro =
                	    descricao.contains("eventos/sinistros");
                	if (!ehEventoOuSinistro) continue;
                	
                	if (valor <= 0) continue;

                	registrosFinais.add(new String[]{
                		    regAns, descricao, trimestre, data, String.valueOf(valor)
                		});
            }
        }
    }


    static String normalizarTrimestre(String trimestre) {
        trimestre = trimestre.toUpperCase();
        if (trimestre.contains("-01-")) return "Q1";
        if (trimestre.contains("-04-")) return "Q2";
        if (trimestre.contains("-07-")) return "Q3";

        return "DESCONHECIDO";
    }

    static void gerarCSVFinal() throws IOException {
        Path output = Paths.get("resultado/consolidado_despesas.csv");
        Files.createDirectories(output.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output)) {
            writer.write("CNPJ,RazaoSocial,Trimestre,Ano,ValorDespesas");
            writer.newLine();
            for (String[] linha : registrosFinais) {
                writer.write(String.join(",", linha));
                writer.newLine();
            }
        }

        System.out.println("CSV consolidado gerado!");
    }

    static void ziparResultado() throws IOException {
        Path zipPath = Paths.get("resultado/consolidado_despesas.zip");
        Path csvPath = Paths.get("resultado/consolidado_despesas.csv");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry entry = new ZipEntry(csvPath.getFileName().toString());
            zos.putNextEntry(entry);
            Files.copy(csvPath, zos);
            zos.closeEntry();
        }

        System.out.println("ZIP gerado: consolidado_despesas.zip");
    }
}
