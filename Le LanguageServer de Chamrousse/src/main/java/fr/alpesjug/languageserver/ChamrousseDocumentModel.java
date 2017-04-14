package fr.alpesjug.languageserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class ChamrousseDocumentModel {

	public static class DocumentRoute {
		private final int line;
		private final String text;
		private final int charOffset;
		private final String name;
		private final int length;
		
		public DocumentRoute(int line, String text, int charOffset, int length, String name) {
			this.line = line;
			this.text = text;
			this.charOffset = charOffset;
			this.length = length;
			this.name = name;
		}
		
		public int getLine() {
			return line;
		}

		public int getCharOffset() {
			return charOffset;
		}

		public String getName() {
			return name;
		}

		public int getLength() {
			return length;
		}
		
		public String getText() {
			return text;
		}
	}
	
	private final List<String> lines = new ArrayList<>();
	private final PriorityQueue<DocumentRoute> routes = new PriorityQueue<>((route1, route2) -> {
		int diff = route1.getLine() - route2.getLine();
		if (diff != 0) {
			return diff;
		} else {
			return route1.getCharOffset() - route2.getCharOffset();
		}
	});
	private final Map<String, String> variables = new HashMap<>();
	private final Map<String, Integer> variableDefinitionLines = new HashMap<>();
	
	public ChamrousseDocumentModel(String text) {
		try (
			Reader r = new StringReader(text);
			BufferedReader reader = new BufferedReader(r);
		) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
				// EDIT: languge syntax change
				/*if (line.startsWith("#")) {
					continue;
				}*/
				if (line.contains("=")) {
					variableDefinition(lines.size() - 1, line);
				} else if (!line.trim().isEmpty()) {
					routes.add(new DocumentRoute(lines.size() - 1, line, 0, line.length(), resolve(line)));
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String resolve(String line) {
		for (Entry<String, String> variable : variables.entrySet()) {
			line = line.replace("${" + variable.getKey() + "}", variable.getValue());
		}
		return line;
	}

	private void variableDefinition(int lineNumber, String line) {
		String[] segments = line.split("=");
		if (segments.length == 2) {
			variables.put(segments[0], segments[1]);
			variableDefinitionLines.put(segments[0], lineNumber);
		}
	}

	public Collection<DocumentRoute> getResolvedRoutes() {
		return this.routes;
	}

	public String getVariable(int line, int character) {
		String text = lines.get(line);
		if (text.contains("=") && character < text.indexOf("=")) {
			return text.split("=")[0];
		}
		int prefix = text.substring(0, character).lastIndexOf("${");
		int suffix = text.indexOf("}", character);
		if (prefix >= 0 && suffix >= 0) {
			return text.substring(prefix + "${".length(), suffix);
		}
		return null;
	}

	public int getDefintionLine(String variable) {
		if (this.variableDefinitionLines.containsKey(variable)) {
			return this.variableDefinitionLines.get(variable);
		}
		return -1;
	}

}
