package fr.alpesjug.languageserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import fr.alpesjug.languageserver.ChamrousseDocumentModel.DocumentRoute;

public class ChamrousseTextDocumentService implements TextDocumentService {

	private final Map<String, ChamrousseDocumentModel> docs = Collections.synchronizedMap(new HashMap<>());
	private final ChamrousseLanguageServer chamrousseLanguageServer;

	public ChamrousseTextDocumentService(ChamrousseLanguageServer chamrousseLanguageServer) {
		this.chamrousseLanguageServer = chamrousseLanguageServer;
	}
	
	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
			TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> Either.forLeft(ChamrousseMap.INSTANCE.all.stream()
				.map(word -> {
					CompletionItem item = new CompletionItem();
					item.setLabel(word);
					item.setInsertText(word);
					return item;
				}).collect(Collectors.toList())));
	}

	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		return null;
	}

	@Override
	public CompletableFuture<Hover> hover(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> {
			ChamrousseDocumentModel doc = docs.get(position.getTextDocument().getUri());
			Hover res = new Hover();
			res.setContents(doc.getResolvedRoutes().stream()
				.filter(route -> route.getLine() == position.getPosition().getLine())
				.map(DocumentRoute::getName)
				.map(ChamrousseMap.INSTANCE.type::get)
				.map(this::getHoverContent)
				.collect(Collectors.toList()));
			return res;
		});
	}
	
	private Either<String, MarkedString> getHoverContent(String type) {
		return Either.forLeft(type);
		// EDIT: cosmetic tool improvement
		/*if ("Verte".equals(type)) {
			return Either.forLeft("<font color='green'>Verte</font>");
		} else if ("Bleue".equals(type)) {
			return Either.forLeft("<font color='blue'>Bleue</font>");
		} else if ("Rouge".equals(type)) {
			return Either.forLeft("<font color='red'>Rouge</font>");
		} else if ("Noire".equals(type)) {
			return Either.forLeft("<font color='black'>Noire</font>");
		}
		return Either.forLeft(type);*/
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams position) {
		return CompletableFuture.supplyAsync(() -> {
			ChamrousseDocumentModel doc = docs.get(position.getTextDocument().getUri());
			String variable = doc.getVariable(position.getPosition().getLine(), position.getPosition().getCharacter()); 
			if (variable != null) {
				int variableLine = doc.getDefintionLine(variable);
				if (variableLine == -1) {
					return Collections.emptyList();
				}
				Location location = new Location(position.getTextDocument().getUri(), new Range(
					new Position(variableLine, 0),
					new Position(variableLine, variable.length())
					));
				return Collections.singletonList(location);
			}
			return null;
		});
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return CompletableFuture.supplyAsync(() -> {
			ChamrousseDocumentModel doc = docs.get(params.getTextDocument().getUri());
			String variable = doc.getVariable(params.getPosition().getLine(), params.getPosition().getCharacter()); 
			if (variable != null) {
				return doc.getResolvedRoutes().stream()
					.filter(route -> route.getText().contains("${" + variable + "}") || route.getText().startsWith(variable + "="))
					.map(route -> new Location(params.getTextDocument().getUri(), new Range(
						new Position(route.getLine(), route.getText().indexOf(variable)),
						new Position(route.getLine(), route.getText().indexOf(variable) + variable.length())
					)))
					.collect(Collectors.toList());
			}
			String routeName = doc.getResolvedRoutes().stream()
					.filter(route -> route.getLine() == params.getPosition().getLine())
					.collect(Collectors.toList())
					.get(0)
					.getName();
			return doc.getResolvedRoutes().stream()
					.filter(route -> route.getName().equals(routeName))
					.map(route -> new Location(params.getTextDocument().getUri(), new Range(
							new Position(route.getLine(), 0),
							new Position(route.getLine(), route.getLength()))))
					.collect(Collectors.toList());
		});
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams params) {
		return CompletableFuture.supplyAsync(() ->
			params.getContext().getDiagnostics().stream()
			.map(diagnostic -> {
				List<Command> res = new ArrayList<>();
				res.add(new Command("Enlever ce trocon", "edit", Collections.singletonList(new TextEdit(diagnostic.getRange(), ""))));
				ChamrousseDocumentModel doc = docs.get(params.getTextDocument().getUri());
				// TODO : add insert and replace according to next route
				return res.stream();
			})
			.flatMap(it -> it)
			.collect(Collectors.toList())
		);
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return null;
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		return null;
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return null;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		ChamrousseDocumentModel model = new ChamrousseDocumentModel(params.getTextDocument().getText());
		this.docs.put(params.getTextDocument().getUri(),
				model);
		CompletableFuture.runAsync(() ->
			chamrousseLanguageServer.client.publishDiagnostics(
				new PublishDiagnosticsParams(params.getTextDocument().getUri(), validate(model))
			)
		);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		ChamrousseDocumentModel model = new ChamrousseDocumentModel(params.getContentChanges().get(0).getText());
		this.docs.put(params.getTextDocument().getUri(),
				model);
		// send notification
		CompletableFuture.runAsync(() ->
			chamrousseLanguageServer.client.publishDiagnostics(
				new PublishDiagnosticsParams(params.getTextDocument().getUri(), validate(model))
			)
		);
	}

	private List<Diagnostic> validate(ChamrousseDocumentModel model) {
		List<Diagnostic> res = new ArrayList<>();
		DocumentRoute previousRoute = null;
		for (DocumentRoute route : model.getResolvedRoutes()) {
			if (!ChamrousseMap.INSTANCE.all.contains(route.getName())) {
				Diagnostic diagnostic = new Diagnostic();
				diagnostic.setSeverity(DiagnosticSeverity.Error);
				diagnostic.setMessage("Ca existe pas a Chamrousse ca");
				diagnostic.setRange(new Range(
						new Position(route.getLine(), route.getCharOffset()),
						new Position(route.getLine(), route.getCharOffset() + route.getLength())));
				res.add(diagnostic);
			} else if (previousRoute != null && !ChamrousseMap.INSTANCE.startsFrom.get(route.getName()).contains(previousRoute.getName())) {
				Diagnostic diagnostic = new Diagnostic();
				diagnostic.setSeverity(DiagnosticSeverity.Warning);
				diagnostic.setMessage("Il n'y a pas de passage de '" + previousRoute.getName() + "' a '" + route.getName() + "'");
				diagnostic.setRange(new Range(
						new Position(route.getLine(), route.getCharOffset()),
						new Position(route.getLine(), route.getCharOffset() + route.getLength())));
				res.add(diagnostic);
			}
			previousRoute = route;
		}
		return res;
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		this.docs.remove(params.getTextDocument().getUri());
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
	}

}
