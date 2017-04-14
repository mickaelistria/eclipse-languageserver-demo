package fr.alpesjug.lsp.eclipse;

import java.io.File;
import java.util.Arrays;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class ChamrousseLanguageServerStreamProvider extends ProcessStreamConnectionProvider
		implements StreamConnectionProvider {


	public ChamrousseLanguageServerStreamProvider() {
		super(
			Arrays.asList("/usr/bin/java", "-jar", "/home/mistria/workspaceDemoLSP/Le LanguageServer de Chamrousse/target/chamrousse-languageserver-0.0.1-SNAPSHOT-jar-with-dependencies.jar"),
			new File(".").getAbsolutePath());
	}

}
