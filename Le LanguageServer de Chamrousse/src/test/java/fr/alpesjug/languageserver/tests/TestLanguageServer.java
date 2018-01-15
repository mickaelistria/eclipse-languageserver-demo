package fr.alpesjug.languageserver.tests;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.junit.Assert;
import org.junit.Test;

import fr.alpesjug.languageserver.ChamrousseLanguageServer;
import fr.alpesjug.languageserver.ChamrousseTextDocumentService;
import fr.alpesjug.languageserver.Main;

public class TestLanguageServer {

	public void checkHover(LanguageServer ls) throws IOException, InterruptedException, ExecutionException {
		ls.initialize(new InitializeParams());
		TextDocumentItem doc = new TextDocumentItem();
		File f = File.createTempFile("blah", ".ski");
		f.deleteOnExit();
		doc.setUri(f.toURI().toString());
		doc.setText("Balmette");
		ls.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(doc, doc.getText()));
		
		TextDocumentIdentifier id = new TextDocumentIdentifier(doc.getUri());
		CompletableFuture<Hover> hover = ls.getTextDocumentService().hover(new TextDocumentPositionParams(id, doc.getUri(), new Position(0, 1)));
		Assert.assertEquals("Green", hover.get().getContents().get(0).getLeft());
		
		hover = ls.getTextDocumentService().hover(new TextDocumentPositionParams(id, doc.getUri(), new Position(0, 0)));
		Assert.assertEquals("Green", hover.get().getContents().get(0).getLeft());
	}

	@Test
	public void checkHoverLocal() throws IOException, InterruptedException, ExecutionException {
		checkHover(new ChamrousseLanguageServer());
	}
	
	@Test
	public void testMain() throws Exception {
		PipedInputStream serverInput = new PipedInputStream();
		PipedOutputStream serverOutput = new PipedOutputStream();
		new Thread(() -> {
			try {
				new Main().startServer(serverInput, serverOutput);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}).start();
		Launcher<LanguageServer> createClientLauncher = LSPLauncher.createClientLauncher(new LanguageClient() {
			
			@Override
			public void telemetryEvent(Object object) {
			}
			
			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
				return null;
			}
			
			@Override
			public void showMessage(MessageParams messageParams) {
			}
			
			@Override
			public void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
			}
			
			@Override
			public void logMessage(MessageParams message) {
			}
		}, new PipedInputStream(serverOutput), new PipedOutputStream(serverInput));
		createClientLauncher.startListening();
		LanguageServer server = createClientLauncher.getRemoteProxy();
		checkHover(server);
	}
	
	@Test
	public void testQuickFixes() throws Exception {
		ChamrousseLanguageServer ls = new ChamrousseLanguageServer();
		List<Diagnostic> diagnostics = new ArrayList<>();
		ls.setRemoteProxy(new LanguageClient() {
			@Override
			public void telemetryEvent(Object object) {
			}
			
			@Override
			public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
				return null;
			}
			
			@Override
			public void showMessage(MessageParams messageParams) {
			}
			
			@Override
			public void publishDiagnostics(PublishDiagnosticsParams d) {
				diagnostics.clear();
				diagnostics.addAll(d.getDiagnostics());
			}
			
			@Override
			public void logMessage(MessageParams message) {
			}
		});
		TextDocumentItem doc = new TextDocumentItem();
		File f = File.createTempFile("blah", ".tls");
		f.deleteOnExit();
		doc.setUri(f.toURI().toString());
		doc.setText("choucroute");
		ls.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(doc, doc.getText()));
		Thread.sleep(1000);
		Assert.assertEquals("Missing diagnostic", 1, diagnostics.size());
		Diagnostic diagnostic = diagnostics.get(0);
		List<? extends Command> resolutions = ls.getTextDocumentService().codeAction(new CodeActionParams(new TextDocumentIdentifier(doc.getUri()), diagnostic.getRange(), new CodeActionContext(Collections.singletonList(diagnostic)))).get();
		Assert.assertEquals("Missing resolution", 1, resolutions.size());
		TextDocumentContentChangeEvent change = new TextDocumentContentChangeEvent();
		
		change.setText("\nunknown track");
		ls.getTextDocumentService().didChange(new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(1), doc.getUri(), Collections.singletonList(change)));
		Thread.sleep(1000);
		Assert.assertEquals("Missing diagnostics", 1, diagnostics.size());
		diagnostic = diagnostics.get(0);
		Assert.assertEquals(1, diagnostic.getRange().getStart().getLine());
		Assert.assertEquals(0, diagnostic.getRange().getStart().getCharacter());
	}

	@Test
	public void testReferences() throws Exception {
		ChamrousseLanguageServer ls = new ChamrousseLanguageServer();
		TextDocumentItem doc = new TextDocumentItem();
		File f = File.createTempFile("blah", ".tls");
		f.deleteOnExit();
		doc.setUri(f.toURI().toString());
		doc.setText("${var}\n${var}\n${var}");
		ls.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(doc, doc.getText()));
		
		ReferenceParams params = new ReferenceParams();
		params.setTextDocument(new TextDocumentIdentifier(doc.getUri()));
		params.setPosition(new Position(0,4));
		List<? extends Location> list = ls.getTextDocumentService().references(params).get();
		Assert.assertEquals(3, list.size());
	}
}
