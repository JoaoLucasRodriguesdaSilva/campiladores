import java.io.*;
import syntaxtree.*;
import visitor.*;
import symtbl.*;
import main.*;

// Certifique-se de que a classe MiniJavaParser esteja acessível
// Se MiniJavaParser estiver em um pacote, adicione a importação:
// import seu.pacote.MiniJavaParser;

public class Main { // Ou o nome que você escolher para sua classe principal

    public static void main(String[] args) {
        System.out.println("Analisador MiniJava iniciado.");
        MiniJavaParser parser;
        if (args.length == 0) {
            System.out.println("Lendo da entrada padrão...");
            parser = new MiniJavaParser(System.in);
        } else if (args.length == 1) {
            try {
                System.out.println("Lendo do arquivo: " + args[0]);
                parser = new MiniJavaParser(new FileInputStream(args[0]));
            } catch (FileNotFoundException e) {
                System.err.println("Erro: Arquivo não encontrado: " + args[0]);
                return;
            }
        } else {
            System.err.println("Uso: java Main [arquivo]"); // Atualize a mensagem de uso
            return;
        }

        try {
            Program p = parser.Program(); // Chama o método inicial da sua gramática
            System.out.println("\nAnálise sintática concluída com sucesso!");

            // 2. Usar o PrettyPrintVisitor para imprimir a AST
            System.out.println("\n--- Pretty Printing da AST ---");
            PrettyPrintVisitor ppVisitor = new PrettyPrintVisitor();
            p.accept(ppVisitor); // Inicia a travessia da AST
            System.out.println("\n--- Fim do Pretty Printing ---");

	    System.out.println("\n--- Construindo Tabela de Símbolos ---");
 	    SymbolTableBuilderVisitor sbVisitor = new SymbolTableBuilderVisitor();
	    p.accept(sbVisitor); // Inicia a travessia da AST para construir a tabela
   	    SymbolTable symbolTable = sbVisitor.getSymbolTable(); // Pega a tabela construída
	    symbolTable.printSymbolTable();
    	    System.out.println("Tabela de Símbolos construída!");

	    System.out.println("\n--- Iniciando Verificação de Tipos ---");
  	    TypeCheckerVisitor tcVisitor = new TypeCheckerVisitor(symbolTable);
 	    p.accept(tcVisitor); // Inicia a travessia para verificação
	    System.out.println("Verificação de Tipos concluída!");

        } catch (ParseException e) { // Certifique-se de importar ParseException
            System.err.println("\nErro de Sintaxe:");
            System.err.println(e.getMessage());
        } catch (TokenMgrError e) { // Certifique-se de importar TokenMgrError
            System.err.println("\nErro Léxico:");
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("\nErro inesperado:");
            e.printStackTrace();
        }
    }
}