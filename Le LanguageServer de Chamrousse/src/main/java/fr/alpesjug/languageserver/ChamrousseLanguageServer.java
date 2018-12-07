package fr.alpesjug.languageserver;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeActionOptions;
import org.eclipse.lsp4j.CodeLensOptions;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class ChamrousseLanguageServer implements LanguageServer {

	private TextDocumentService textService;
	private WorkspaceService workspaceService;
	LanguageClient client;

	public ChamrousseLanguageServer() {
		textService = new ChamrousseTextDocumentService(this);
		workspaceService = new ChamrousseWorkspaceService();
	}
	
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		final InitializeResult res = new InitializeResult(new ServerCapabilities());
		res.getCapabilities().setCodeActionProvider(new CodeActionOptions());
		res.getCapabilities().setCompletionProvider(new CompletionOptions(false, ChamrousseMap.INSTANCE.getAllPossibleChars()));
		res.getCapabilities().setDefinitionProvider(Boolean.TRUE);
		res.getCapabilities().setHoverProvider(Boolean.TRUE);
		res.getCapabilities().setReferencesProvider(Boolean.TRUE);
		res.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
		res.getCapabilities().setDocumentSymbolProvider(Boolean.TRUE);
		res.getCapabilities().setCodeLensProvider(new CodeLensOptions(true));
		
		return CompletableFuture.completedFuture(res);
	}

	public CompletableFuture<Object> shutdown() {
		return CompletableFuture.supplyAsync(() -> Boolean.TRUE);
	}

	public void exit() {
	}

	public TextDocumentService getTextDocumentService() {
		return this.textService;
	}

	public WorkspaceService getWorkspaceService() {
		return this.workspaceService;
	}

	public void setRemoteProxy(LanguageClient remoteProxy) {
		this.client = remoteProxy;
	}
	
}
