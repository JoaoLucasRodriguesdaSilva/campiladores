package visitor;

import syntaxtree.*;
import symtbl.*; // Importa todas as classes da sua tabela de símbolos

public class SymbolTableBuilderVisitor implements Visitor {

    private SymbolTable symbolTable;
    private ClassInfo currentClass;  // Mantém a referência à classe que está sendo processada
    private MethodInfo currentMethod; // Mantém a referência ao método que está sendo processado

    public SymbolTableBuilderVisitor() {
        this.symbolTable = new SymbolTable();
        // Inicializa o escopo global no construtor da SymbolTable
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    // =========================================================================
    // Métodos visit() para Declarações (onde a tabela de símbolos é preenchida)
    // =========================================================================

    @Override
    public void visit(Program n) {
        // O escopo global já foi entrado no construtor da SymbolTable
        
        // Visita a MainClass
        n.m.accept(this);

        // Visita as outras declarações de classe
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }
        
        // Não é necessário sair do escopo global explicitamente aqui,
        // pois o programa termina.
    }

    @Override
    public void visit(MainClass n) {
        // MainClass é tratada como uma classe simples para fins de tabela de símbolos
        String className = n.i1.s;
        ClassInfo classInfo = new ClassInfo(className, null, symbolTable.getCurrentScope());
        symbolTable.addClass(className, classInfo);
        symbolTable.enterScope(classInfo); // Entra no escopo da MainClass
        this.currentClass = classInfo; // Define a classe atual

        // O método main é especial (public static void main)
        // Para MiniJava, o tipo de retorno é 'void' (null para Type na AST)
        // E o parâmetro String[] é String[].
        // O visitor não processa 'String[]' como um nó Type, então o tipo é hardcoded.
        MethodInfo mainMethodInfo = new MethodInfo("main", null, currentClass); // ReturnType null para void
        VarInfo mainArgVar = new VarInfo(n.i2.s, new syntaxtree.IntArrayType()); // String[] como IntArrayType para simplicidade (adaptar se tiver String[] real)
        mainMethodInfo.addParameter(n.i2.s, mainArgVar);

        currentClass.addMethod("main", mainMethodInfo);
        symbolTable.enterScope(mainMethodInfo); // Entra no escopo do método main
        this.currentMethod = mainMethodInfo; // Define o método atual

        n.s.accept(this); // Visita o corpo do método main para encontrar variáveis locais

        symbolTable.exitScope(); // Sai do escopo do método main
        this.currentMethod = null; // Limpa o método atual

        symbolTable.exitScope(); // Sai do escopo da MainClass
        this.currentClass = null; // Limpa a classe atual
    }

    @Override
    public void visit(ClassDeclSimple n) {
        String className = n.i.s;
        ClassInfo classInfo = new ClassInfo(className, null, symbolTable.getCurrentScope());
        symbolTable.addClass(className, classInfo);
        symbolTable.enterScope(classInfo); // Entra no escopo da classe
        this.currentClass = classInfo; // Define a classe atual

        // Visita variáveis de instância
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        // Visita métodos
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }

        symbolTable.exitScope(); // Sai do escopo da classe
        this.currentClass = null; // Limpa a classe atual
    }

    @Override
    public void visit(ClassDeclExtends n) {
        String className = n.i.s;
        String extendsClassName = n.j.s;
        ClassInfo classInfo = new ClassInfo(className, extendsClassName, symbolTable.getCurrentScope());
        symbolTable.addClass(className, classInfo);
        symbolTable.enterScope(classInfo); // Entra no escopo da classe
        this.currentClass = classInfo; // Define a classe atual

        // Visita variáveis de instância
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        // Visita métodos
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }

        symbolTable.exitScope(); // Sai do escopo da classe
        this.currentClass = null; // Limpa a classe atual
    }

    @Override
    public void visit(VarDecl n) {
        // Assumimos que n.t já é um objeto Type válido da sua syntaxtree
        Type varType = n.t;
        String varName = n.i.s;
        VarInfo varInfo = new VarInfo(varName, varType);

        // Adiciona a variável ao escopo atual (classe ou método)
        if (currentMethod != null) {
            // Se estivermos dentro de um método, é uma variável local
            currentMethod.addVar(varName, varInfo);
        } else if (currentClass != null) {
            // Se estivermos dentro de uma classe (mas não de um método), é um campo
            currentClass.addField(varName, varInfo);
        } else {
            // Isso não deve acontecer em MiniJava, mas é uma segurança
            System.err.println("Erro interno: VarDecl fora de um escopo de classe ou método.");
        }
        
        // Não é necessário visitar n.t nem n.i novamente, pois já extraímos as informações
    }

    @Override
    public void visit(MethodDecl n) {
        String methodName = n.i.s;
        Type returnType = n.t; // O nó AST Type já é o objeto Type que queremos
        
        MethodInfo methodInfo = new MethodInfo(methodName, returnType, currentClass); // Pai é a classe atual
        currentClass.addMethod(methodName, methodInfo); // Adiciona ao mapa de métodos da classe
        
        symbolTable.enterScope(methodInfo); // Entra no escopo do método
        this.currentMethod = methodInfo; // Define o método atual

        // Visita os parâmetros
        for (int i = 0; i < n.fl.size(); i++) {
            n.fl.elementAt(i).accept(this); // FormalParameters são adicionados ao escopo do método aqui
        }
        // Visita variáveis locais
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this); // VarDecls locais são adicionados ao escopo do método
        }
        // Visita statements e expressão de retorno (apenas para travessia, sem adicionar símbolos)
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
        n.e.accept(this);

        symbolTable.exitScope(); // Sai do escopo do método
        this.currentMethod = null; // Limpa o método atual
    }

    @Override
    public void visit(Formal n) {
        // Formal é um parâmetro, adicionado ao escopo do método atual
        Type paramType = n.t;
        String paramName = n.i.s;
        VarInfo paramInfo = new VarInfo(paramName, paramType);
        
        if (currentMethod != null) {
            currentMethod.addParameter(paramName, paramInfo);
        } else {
            System.err.println("Erro interno: Parâmetro declarado fora de um método.");
        }
        // Não é necessário visitar n.t nem n.i novamente
    }

    // =========================================================================
    // Métodos visit() para Tipos (apenas para travessia, não adicionam símbolos)
    // =========================================================================

    @Override
    public void visit(IntArrayType n) {
        // Este visitor não precisa fazer nada aqui, pois o objeto Type já é o próprio nó
        // e é passado diretamente para VarInfo/MethodInfo.
    }

    @Override
    public void visit(BooleanType n) {
        // Similar ao IntArrayType
    }

    @Override
    public void visit(IntegerType n) {
        // Similar ao IntArrayType
    }

    @Override
    public void visit(IdentifierType n) {
        // Similar ao IntArrayType
        // O nome do tipo (n.s) será usado na verificação de tipos posteriormente
    }

    // =========================================================================
    // Métodos visit() para Statements (apenas para travessia, não adicionam símbolos)
    // =========================================================================
    // Estes métodos simplesmente chamam 'accept' nos seus filhos para garantir
    // que quaisquer VarDecl aninhados (se a linguagem permitisse blocos com decls)
    // ou chamadas de método sejam alcançados.
    // Em MiniJava, VarDecls só ocorrem em classes e no início de métodos.

    @Override
    public void visit(Block n) {
        // Em MiniJava, blocos não introduzem novos escopos para VarDecls,
        // mas é bom percorrer os statements.
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
    }

    @Override
    public void visit(If n) {
        n.e.accept(this);
        n.s1.accept(this);
        n.s2.accept(this);
    }

    @Override
    public void visit(While n) {
        n.e.accept(this);
        n.s.accept(this);
    }

    @Override
    public void visit(Print n) {
        n.e.accept(this);
    }
    
    @Override
    public void visit(Assign n) {
        n.i.accept(this); // Visita o identificador para travessia, não para adicionar
        n.e.accept(this);
    }

    @Override
    public void visit(ArrayAssign n) {
        n.i.accept(this);
        n.e1.accept(this);
        n.e2.accept(this);
    }

    // =========================================================================
    // Métodos visit() para Expressões (apenas para travessia, não adicionam símbolos)
    // =========================================================================
    // Estes métodos simplesmente chamam 'accept' nos seus filhos para garantir
    // que quaisquer identificadores ou chamadas de método aninhados sejam alcançados.

    @Override
    public void visit(And n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    @Override
    public void visit(LessThan n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    @Override
    public void visit(Plus n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    @Override
    public void visit(Minus n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    @Override
    public void visit(Times n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    @Override
    public void visit(ArrayLookup n) {
        n.e1.accept(this);
        n.e2.accept(this);
    }

    @Override
    public void visit(ArrayLength n) {
        n.e.accept(this);
    }

    @Override
    public void visit(Call n) {
        n.e.accept(this);
        n.i.accept(this);
        for (int i = 0; i < n.el.size(); i++) {
            n.el.elementAt(i).accept(this);
        }
    }

    @Override
    public void visit(IntegerLiteral n) {
        // Nada a fazer, é um literal
    }

    @Override
    public void visit(True n) {
        // Nada a fazer, é um literal
    }

    @Override
    public void visit(False n) {
        // Nada a fazer, é um literal
    }

    @Override
    public void visit(IdentifierExp n) {
        // Nada a fazer, o identificador já foi processado na declaração
    }

    @Override
    public void visit(This n) {
        // Nada a fazer
    }

    @Override
    public void visit(NewArray n) {
        n.e.accept(this);
    }

    @Override
    public void visit(NewObject n) {
        n.i.accept(this); // Visita o identificador para travessia
    }

    @Override
    public void visit(Not n) {
        n.e.accept(this);
    }

    @Override
    public void visit(Identifier n) {
        // Identifiers são processados no contexto de suas declarações (VarDecl, Formal, etc.)
        // ou quando são referenciados (IdentifierExp). Este visit é principalmente para travessia.
    }
}