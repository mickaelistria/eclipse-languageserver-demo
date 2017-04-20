/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.alpesjug.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.Arrays.asList;

/**
 * @author Florent benoit
 */
@Singleton
public class AlpesJugLanguageServerLauncher extends LanguageServerLauncherTemplate {

    private static final String   LANGUAGE_ID = "alpesjug";
    private static final String[] EXTENSIONS  = new String[] {"tls", "gnb", "ski"};
    private static final String[] MIME_TYPES  = new String[] {"text/x-alpesjug"};
    private static final LanguageDescription description;

    private final Path launchScript;

    static {
        description = new LanguageDescription();
        description.setFileExtensions(asList(EXTENSIONS));
        description.setLanguageId(LANGUAGE_ID);
        description.setMimeTypes(Arrays.asList(MIME_TYPES));
    }

    @Inject
    public AlpesJugLanguageServerLauncher() {
        launchScript = Paths.get(System.getenv("HOME"), "che/ls-alpesjug/launch.sh");
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        return description;
    }

    @Override
    public boolean isAbleToLaunch() {
        return launchScript.toFile().exists();
    }

    @Override
    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start AlpesJug language server", e);
        }
    }

    @Override
    protected LanguageServer connectToLanguageServer(final Process languageServerProcess, LanguageClient client) {
        Launcher<LanguageServer> launcher = Launcher.createLauncher(client, LanguageServer.class,
                                                                    languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        launcher.startListening();
        return launcher.getRemoteProxy();
    }
}
