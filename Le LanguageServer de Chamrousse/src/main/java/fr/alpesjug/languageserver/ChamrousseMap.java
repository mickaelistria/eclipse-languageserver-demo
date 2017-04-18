package fr.alpesjug.languageserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class ChamrousseMap {

	public static final ChamrousseMap INSTANCE = new ChamrousseMap();
	
	final Properties props = new Properties();
	final Set<String> all;
	final Map<String, Collection<String>> startsFrom;
	final Map<String, String> type;
	
	private ChamrousseMap() {
		InputStream propertiesStream = ChamrousseMap.class.getResourceAsStream("/" + ChamrousseMap.class.getPackage().getName().replace(".", "/") + "/chamrousseMap.properties");
		try {
			props.load(propertiesStream);
			propertiesStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.all = props.keySet().stream()
			.map(key -> ((String)key).split("\\.")[0])
			.collect(Collectors.toSet());
		this.startsFrom = props.entrySet().stream()
			.filter(entry -> ((String)entry.getKey()).endsWith(".startsFrom"))
			.collect(Collectors.toMap(
					entry -> ((String)entry.getKey()).split("\\.")[0],
					entry -> Arrays.asList(((String)entry.getValue()).split(","))));
		this.type = props.entrySet().stream()
			.filter(entry -> ((String)entry.getKey()).endsWith(".type"))
			.collect(Collectors.toMap(
					entry -> ((String)entry.getKey()).split("\\.")[0],
					entry -> (String)entry.getValue()));
	}

}
