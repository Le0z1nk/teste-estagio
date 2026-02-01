package atividade2;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {

	static final String BASE_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/";

	public static void main(String[] args) throws Exception {

		System.out.println("Acessando página...");
		Document raiz = Jsoup.connect(BASE_URL).get();
		Elements links = raiz.select("a[href]");
		for (Element link : links) {
			String href = link.attr("href");
			if (href.endsWith(".csv")) {
				String urlCompleta = BASE_URL + href;
				baixarArquivo(urlCompleta);
			}
		}

		System.out.println("Download finalizado!");
		
		Path despesasCsv = Paths.get("resultado/consolidado_despesas.csv");
	    Path cadastroCsv = Paths.get("downloads/Relatorio_cadop.csv");
	    JoinArquivos.processarJoin(despesasCsv, cadastroCsv);
		
	}

	static void baixarArquivo(String url) throws Exception {
		System.out.println("Baixando: " + url);
		Path path = Paths.get("downloads/" + Paths.get(new URL(url).getPath()).getFileName());
		Files.createDirectories(path.getParent());
		try (InputStream in = new URL(url).openStream()) {
			Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
}
