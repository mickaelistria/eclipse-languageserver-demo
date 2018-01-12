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
package org.eclipse.che.plugin.chamrousse.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.plugin.chamrousse.inject.ChamrousseModule;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * @author Florent benoit
 */
@Singleton
public class ChamrousseLanguageServerLauncher extends LanguageServerLauncherTemplate {


    private static final LanguageServerDescription DESCRIPTION = createServerDescription();

    private final Path launchScript;

    private static final String REGEX = ".*\\.ski";


    @Inject
    public ChamrousseLanguageServerLauncher() {
        launchScript = Paths.get(System.getenv("HOME"), "ls-chamrousse/launch.sh");
    }

    /**
     * Gets the language server description
     */
    @Override
    public LanguageServerDescription getDescription() {
        return DESCRIPTION;
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
            throw new LanguageServerException("Cannot start Chamrousse language server", e);
        }
    }

    @Override
    protected LanguageServer connectToLanguageServer(final Process languageServerProcess, LanguageClient client) {
        Launcher<LanguageServer> launcher = Launcher.createLauncher(client, LanguageServer.class,
                                                                    languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        launcher.startListening();
        return launcher.getRemoteProxy();
    }


    private static LanguageServerDescription createServerDescription() {
        LanguageServerDescription description =
            new LanguageServerDescription(
                "org.eclipse.che.plugin.chamrousse.languageserver",
                null,
                Arrays.asList(new DocumentFilter(ChamrousseModule.LANGUAGE_ID, REGEX, null)));
        return description;
    }
}
