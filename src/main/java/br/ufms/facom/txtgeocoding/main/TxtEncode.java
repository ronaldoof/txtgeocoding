package br.ufms.facom.txtgeocoding.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.StringUtils;

public class TxtEncode {

	private String input;
	private String output;
	private List<List<String>> result;

	private static final String GEO_URL = "http://websensor.facom.ufms.br/websensors/geonames/";

	public TxtEncode(String input, String output) {
		this.input = input;
		this.output = output;
		this.result = new ArrayList<List<String>>();
	}

	public List<List<String>> encode() throws IOException, Exception {
		Path dir = Paths.get(input);
		if (dir.toFile().isDirectory()) {
			Files.newDirectoryStream(dir).forEach(f -> {
				if (f.toString().endsWith(".txt")) {
					try {
						System.out.printf("Processando arquivo [%s]\n",f.getFileName());
						String content = new String(Files.readAllBytes(f));
						String[] coords = processEvent(content);
						if (coords != null) {
							String[] tuple = new String[3];
							tuple[0] = f.getFileName().toString();
							tuple[1] = coords[0];
							tuple[2] = coords[1];
							this.result.add(Arrays.asList(tuple));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			System.out.printf("Processamento finalizado, escrevendo arquivo [%s].\n",this.output);
			saveToCSV();
			return this.result;
		} else {
			throw new Exception("O caminho de entrada nao é um diretorio.");
		}
	}

	void saveToCSV() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File(this.output));
			StringBuilder sb = new StringBuilder();
			this.result.stream().forEachOrdered(r -> {
				sb.append(r.get(0));
				sb.append(';');
				sb.append(r.get(1));
				sb.append(';');
				sb.append(r.get(2));
				sb.append(';');
				sb.append('\n');
			});
			pw.write(sb.toString());
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	String[] processEvent(String event) {
		String[] terms = splitTerms(event);
		String[] coords = null;
		for (int i = 0; i < terms.length; i++) {
			if(!Character.isUpperCase(terms[i].codePointAt(0))){
				continue;
			}
			coords = getGeoTag(terms[i]);
			if (coords != null && ((i + 1) < terms.length)) {
				coords = getGeoTag(terms[i] + " " + terms[i + 1]);
			}
		}
		return coords;
	}

	String[] splitTerms(String event) {
		Scanner scanner = new Scanner(event);
		List<String> terms = new ArrayList<String>();
		while (scanner.hasNext()) {
			terms.add(scanner.next());
		}
		scanner.close();
		String[] strTerms = new String[terms.size()];
		terms.toArray(strTerms);
		return strTerms;
	}

	public String[] getGeoTag(String word) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet get = new HttpGet(buildURL(word));
		try (CloseableHttpResponse response = httpclient.execute(get)) {

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				try {
					String json = IOUtils.toString(instream);
					if (json.lastIndexOf("/>") != -1) {
						json = json.substring(json.lastIndexOf("/>") + 2);
					}
					if(StringUtils.isEmpty(json)){
						return null;
					}
					JSONParser parser = new JSONParser();
					Object obj = parser.parse(json);
					if (!(obj instanceof JSONObject)) {
						return null;
					}
					JSONObject jsonObject = (JSONObject) obj;
					for (int i = 0; i <= 7; i++) {
						JSONArray alg = (JSONArray) jsonObject.get("0");
						if (!alg.isEmpty()) {
							String[] coords = new String[2];
							coords[0] = ((JSONObject) alg.get(0)).get("longitude").toString();
							coords[1] = ((JSONObject) alg.get(0)).get("latitude").toString();
							return coords;
						}
					}
				} catch (ParseException e) {
					e.printStackTrace();
				} finally {
					instream.close();
				}
			}
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	URI buildURL(String word) {
		try {
			URI uri = new URIBuilder(GEO_URL).setParameter("q", word).build();
			return uri;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

	}

}
