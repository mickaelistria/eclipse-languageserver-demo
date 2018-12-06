package fr.alpesjug.languageserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
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
import org.eclipse.lsp4j.DocumentSymbol;
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
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import fr.alpesjug.languageserver.ChamrousseDocumentModel.Route;
import fr.alpesjug.languageserver.ChamrousseDocumentModel.VariableDefinition;

public class ChamrousseTextDocumentService implements TextDocumentService {

	private final Map<String, ChamrousseDocumentModel> docs = Collections.synchronizedMap(new HashMap<>());
	private final ChamrousseLanguageServer chamrousseLanguageServer;
	private final Map<String, Integer> documentVersions = new HashMap<>();

	public ChamrousseTextDocumentService(ChamrousseLanguageServer chamrousseLanguageServer) {
		this.chamrousseLanguageServer = chamrousseLanguageServer;
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		return CompletableFuture.supplyAsync(() -> Either.forRight(new CompletionList(false, ChamrousseMap.INSTANCE.all.stream()
				.map(word -> {
					CompletionItem item = new CompletionItem();
					item.setLabel(word);
					item.setInsertText(word);
					return item;
				}).collect(Collectors.toList()))));
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
			List<Either<String, MarkedString>> content = doc.getResolvedRoutes().stream()
				.filter(route -> route.line == position.getPosition().getLine())
				.map(route -> route.name)
				.map(ChamrousseMap.INSTANCE.type::get)
				.map(this::getHoverContent)
				.collect(Collectors.toList());
			res.setContents(content);
			return res;
		});
	}

	private Either<String, MarkedString> getHoverContent(String type) {
		return Either.forLeft(type);
		// TODO: cosmetic tool improvement, show colors
		// return Either.forLeft("<font color='" + type.toLowerCase() + "'>" + type + "</font>");
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
					.filter(route -> route.text.contains("${" + variable + "}") || route.text.startsWith(variable + "="))
					.map(route -> new Location(params.getTextDocument().getUri(), new Range(
						new Position(route.line, route.text.indexOf(variable)),
						new Position(route.line, route.text.indexOf(variable) + variable.length())
					)))
					.collect(Collectors.toList());
			}
			String routeName = doc.getResolvedRoutes().stream()
					.filter(route -> route.line == params.getPosition().getLine())
					.collect(Collectors.toList())
					.get(0)
					.name;
			return doc.getResolvedRoutes().stream()
					.filter(route -> route.name.equals(routeName))
					.map(route -> new Location(params.getTextDocument().getUri(), new Range(
							new Position(route.line, 0),
							new Position(route.line, route.text.length()))))
					.collect(Collectors.toList());
		});
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams position) {
		return null;
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(DocumentSymbolParams params) {
		return CompletableFuture.supplyAsync(() ->
			docs.get(params.getTextDocument().getUri()).getResolvedLines().stream().map(line -> {
				SymbolInformation symbol = new SymbolInformation();
				symbol.setLocation(new Location(params.getTextDocument().getUri(), new Range(
						new Position(line.line, line.charOffset),
						new Position(line.line, line.charOffset + line.text.length()))));
				if (line instanceof VariableDefinition) {
					symbol.setKind(SymbolKind.Variable);
					symbol.setName(((VariableDefinition) line).variableName);
				} else if (line instanceof Route) {
					symbol.setKind(SymbolKind.String);
					symbol.setName(((Route) line).name);
				}
				Either<SymbolInformation, DocumentSymbol> res = Either.forLeft(symbol);
				return res;
			}).collect(Collectors.toList())
		);
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		String docUri = params.getTextDocument().getUri();
		return CompletableFuture.supplyAsync(() ->
			params.getContext().getDiagnostics().stream()
			.map(diagnostic -> {
				List<CodeAction> res = new ArrayList<>();
				CodeAction removeAction = new CodeAction("Enlever ce troncon");
				removeAction.setKind(CodeActionKind.QuickFix);
				Integer docVersion = this.documentVersions.get(docUri);
				removeAction.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
						new TextDocumentEdit(new VersionedTextDocumentIdentifier(docUri, docVersion),
								Collections.singletonList(
										new TextEdit(diagnostic.getRange(), "")))))));
				removeAction.setDiagnostics(Collections.singletonList(diagnostic));
				res.add(removeAction);
				// TODO syntaxic change: support comment (Tool part of syntax/parser change)
//				CodeAction commentAction = new CodeAction("Commenter ce troncon");
//				commentAction.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
//						new TextDocumentEdit(new VersionedTextDocumentIdentifier(docUri, docVersion),
//								Collections.singletonList(
//										new TextEdit(new Range(diagnostic.getRange().getStart(), diagnostic.getRange().getStart()), "#")))))));
//				commentAction.setDiagnostics(Collections.singletonList(diagnostic));
//				res.add(commentAction);
				// TODO Functional change: Add a nice productive feature
//				 ChamrousseDocumentModel doc = docs.get(params.getTextDocument().getUri());
//				Route route = doc.getRoute(params.getRange().getStart().getLine());
//				if (route != null) {
//					int index = doc.getResolvedRoutes().indexOf(route);
//					if (index >= 0)
//						if (index > 0) {
//							Route previousRoute = doc.getResolvedRoutes().get(doc.getResolvedRoutes().indexOf(route) - 1);
//							for (String way : ChamrousseMap.INSTANCE.findWaysBetween(previousRoute.name, route.name)) {
//								CodeAction insertCodeAction = new CodeAction("Inserer '" + way + "' pour rejoindre les 2 pistes");
//								insertCodeAction.setKind(CodeActionKind.QuickFix);
//								insertCodeAction.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
//										new TextDocumentEdit(new VersionedTextDocumentIdentifier(docUri, docVersion), Collections.singletonList(
//										new TextEdit(new Range(diagnostic.getRange().getStart(), diagnostic.getRange().getStart()), way + "\n")))))));
//								insertCodeAction.setDiagnostics(Collections.singletonList(diagnostic));
//								res.add(insertCodeAction);
//							}
//							if (index + 1 < doc.getResolvedRoutes().size()) {
//								Route nextRoute = doc.getResolvedRoutes().get(index + 1);
//								for (String way : ChamrousseMap.INSTANCE.findWaysBetween(previousRoute.name, nextRoute.name)) {
//									CodeAction replaceCodeAction = new CodeAction("Remplacer par '" + way + "' pour rejoindre les 2 pistes");
//									replaceCodeAction.setKind(CodeActionKind.QuickFix);
//									replaceCodeAction.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
//											new TextDocumentEdit(new VersionedTextDocumentIdentifier(docUri, docVersion), Collections.singletonList(
//											new TextEdit(diagnostic.getRange(), way)))))));
//									replaceCodeAction.setDiagnostics(Collections.singletonList(diagnostic));
//									res.add(replaceCodeAction);
//								}
//							}
//						}
//				}
				return res;
			})
			.flatMap(Collection::stream)
			.map(codeAction -> {
				Either<Command, CodeAction> either = Either.forRight(codeAction);
				return either;
			}).collect(Collectors.toList())
		);
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return CompletableFuture.completedFuture(Collections.emptyList());
		// TODO IDE feature change
//		return CompletableFuture.supplyAsync(() -> docs.get(params.getTextDocument().getUri()).getResolvedRoutes().stream().map(route ->
//			new CodeLens(new Range(new Position(route.line, 0), new Position(route.line, 1)), new Command(ChamrousseMap.INSTANCE.isLift(route.name) ? "🚡up" : "⛷️down", "kikoo"), null)
//		).collect(Collectors.toList()));
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return null;
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
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
		// modify internal state
		this.documentVersions.put(params.getTextDocument().getUri(), params.getTextDocument().getVersion() + 1);
		ChamrousseDocumentModel model = new ChamrousseDocumentModel(params.getContentChanges().get(0).getText());
		this.docs.put(params.getTextDocument().getUri(),
				model);
		// send notification
		CompletableFuture.runAsync(() -> {
				List<Diagnostic> diagnostics = validate(model);
				chamrousseLanguageServer.client.publishDiagnostics(
					new PublishDiagnosticsParams(params.getTextDocument().getUri(), diagnostics)
				);
			}
		);
	}

	private List<Diagnostic> validate(ChamrousseDocumentModel model) {
		List<Diagnostic> res = new ArrayList<>();
		Route previousRoute = null;
		for (Route route : model.getResolvedRoutes()) {
			if (!ChamrousseMap.INSTANCE.all.contains(route.name)) {
				Diagnostic diagnostic = new Diagnostic();
				diagnostic.setSeverity(DiagnosticSeverity.Error);
				diagnostic.setMessage("Ca existe pas a Chamrousse ca");
				diagnostic.setRange(new Range(
						new Position(route.line, route.charOffset),
						new Position(route.line, route.charOffset + route.text.length())));
				res.add(diagnostic);
			} else if (previousRoute != null && !ChamrousseMap.INSTANCE.startsFrom(route.name, previousRoute.name)) {
				Diagnostic diagnostic = new Diagnostic();
				diagnostic.setSeverity(DiagnosticSeverity.Warning);
				diagnostic.setMessage("Il n'y a pas de passage de '" + previousRoute.name + "' a '" + route.name + "'");
				diagnostic.setRange(new Range(
						new Position(route.line, route.charOffset),
						new Position(route.line, route.charOffset + route.text.length())));
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
