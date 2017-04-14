# Language Server Protocol demo

This repository contains several projects to demonstrate the Language Server Protocol in action together with some Eclipse technologies:

## Content

### Le LanguageServer de Chamrousse

This is a Maven module demonstrating usage of [Eclipse LSP4J](https://github.com/eclipse/lsp4j) to develop language servers in Java just by implementing the right classes.

This Maven module produces a jar-with-dependencies that is the language server delivery, and could produce additional artifacts (such as a Docker image).

Worth mentioning:
* The dependency to lsp4j in `pom.xml`
* ChamrouseLanguageServer, ChamrousseTextDocumentService simply implementing LSP4J's interfaces to provide operations
* The Main class simply starting  the language server and binding it to the stdio streams of the process

### fr.alpesjug.lsp.eclipse

This is an Eclipse plugin that relies on [Eclipse LSP4E](https://projects.eclipse.org/projects/technology.lsp4e) to interact with the language server above.

Demo with *Right-click > Run As > Eclipse plugin*:
* Diagnostic -on code and trees- + quickfixes
* Completion
* Hover
* References
* Jump to definition (F3)

Worth mentioning simplicity (on code in dev IDE, not demo one):
* entries in `plugin.xml` to define the server and bind it to content-type
* Show the class only spawns a process and connects to its stdio streams

### vscode-chamrousse

(copied and adapted from Eclipse JDT-based [vscode-java](https://github.com/redhat-developer/vscode-java))

Demo from VSCode, open `vscode-chamrousse` and press *F5* to Debug:
* show diagnostics
* completion
* hover
* ...

Show the code (from dev VSCode, not debug instance):
* open `src/extension.ts`, line 25
* see it's spawning a process and connecting to stdio streams

### in Eclipse Che

TODO

## Script

. Show and demo the modules individually
. When LS is shown working in all IDEs, modify it (look for `EDIT` in the code of the language server). Possible changes:
.. Language > Syntaxic change: allow a new syntax in the language (such as comments) in ChamrousseDocumentModel
.. Language > Lexical change : allow new words in the language in `chamrousseMap.properties`
.. Tool > Functional change: improve completion or quickfix to show possible values that don't produce errors
.. Tool > Cosmetic change: improve the hover to show the color
. Rebuild server
. Restart the various IDEs to show that the changes are taken into account **without need to change any functional code**

Conclusion: good separation of concerns makes it much easier to maintain the language support in all IDEs at once.
