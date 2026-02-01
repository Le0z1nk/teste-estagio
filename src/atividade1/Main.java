package atividade1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Main {

	static final String BASE_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/";

	public static void main(String[] args) throws Exception {

		Document raiz = Jsoup.connect(BASE_URL).get();
		String pastaDemonstracoes = encontrarLink(raiz, "demonstracoes_contabeis");
		if (pastaDemonstracoes == null) {
			System.out.println("Pasta demonstracoes_contabeis não encontrada!");
			return;
		}

		String urlDemonstracoes = BASE_URL + pastaDemonstracoes;
		Document demonstracoesDoc = Jsoup.connect(urlDemonstracoes).get();
		String pasta2025 = encontrarLink(demonstracoesDoc, "2025");
		if (pasta2025 == null) {
			System.out.println("Pasta 2025 não encontrada!");
			return;
		}

		String url2025 = urlDemonstracoes + pasta2025;
		Document anoDoc = Jsoup.connect(url2025).get();
		for (Element link : anoDoc.select("a[href$=.zip]")) {
			String zipUrl = url2025 + link.attr("href");
			baixarEProcessar(zipUrl);
		}
	}

	static String encontrarLink(Document doc, String nomePasta) {
		for (Element link : doc.select("a[href]")) {
			if (link.attr("href").contains(nomePasta)) {
				return link.attr("href");
			}
		}
		return null;
	}

	static void baixarEProcessar(String url) throws Exception {
		System.out.println("Baixando: " + url);
		Path zipPath = Paths.get("downloads/" + Paths.get(new URL(url).getPath()).getFileName());
		Files.createDirectories(zipPath.getParent());
		try (InputStream in = new URL(url).openStream()) {
			Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
		}

		Path pastaExtraida = Paths.get("extraidos/" + zipPath.getFileName().toString().replace(".zip", ""));
		extrairZip(zipPath, pastaExtraida);
		ProcessadorArquivos.processarCSVs(pastaExtraida);
	}

	static void extrairZip(Path zipPath, Path destino) throws IOException {
		Files.createDirectories(destino);
		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				Path novoArquivo = destino.resolve(entry.getName());
				if (entry.isDirectory()) {
					Files.createDirectories(novoArquivo);
				} else {
					Files.createDirectories(novoArquivo.getParent());
					Files.copy(zis, novoArquivo, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}

		System.out.println("Extraído: " + destino);
	}

}
