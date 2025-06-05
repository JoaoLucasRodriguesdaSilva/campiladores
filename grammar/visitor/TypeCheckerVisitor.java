package visitor;

import java.util.*;
import syntaxtree.*;
import symtbl.*; // Importa as classes da tabela de símbolos

public class TypeCheckerVisitor implements TypeVisitor {

    private SymbolTable symbolTable;
    private ClassInfo currentClass;  // Referência à classe que está sendo verificada
    private MethodInfo currentMethod; // Referência ao método que está sendo verificado

    public TypeCheckerVisitor(SymbolTable st) {
        this.symbolTable = st;
        // Não há necessidade de currentClass/Method aqui, eles serão definidos durante as visitas
    }

    // =========================================================================
    // Métodos Auxiliares para Manipulação de Tipos e Erros
    // =========================================================================

    // Método para verificar se dois tipos são compatíveis para atribuição
    // (ex: int = int, boolean = boolean, MyClass = MyClass, SubClass = SuperClass)
    private boolean isAssignable(Type t1, Type t2) {
        if (t1 == null || t2 == null) {
            return false; // Tipos nulos não são atribuíveis (erro semântico)
        }
        if (t1 instanceof IntegerType && t2 instanceof IntegerType) {
            return true;
        }
        if (t1 instanceof BooleanType && t2 instanceof BooleanType) {
            return true;
        }
        if (t1 instanceof IntArrayType && t2 instanceof IntArrayType) {
            return true;
        }
        // Para tipos de identificador (classes):
        if (t1 instanceof IdentifierType && t2 instanceof IdentifierType) {
            String className1 = ((IdentifierType) t1).s;
            String className2 = ((IdentifierType) t2).s;

            // Se forem o mesmo tipo de classe
            if (className1.equals(className2)) {
                return true;
            }

            // Verifica se t2 (subclasse) é um subtipo de t1 (superclasse)
            ClassInfo class2 = symbolTable.getClass(className2);
            while (class2 != null) {
                if (class2.getName().equals(className1)) {
                    return true; // Encontrou t1 na cadeia de herança de t2
                }
                String extendsName = class2.getExtendsClass();
                if (extendsName != null) {
                    class2 = symbolTable.getClass(extendsName);
                } else {
                    class2 = null; // Fim da cadeia de herança
                }
            }
        }
        return false;
    }

    // Método para verificar se um tipo é subtipo de outro (para herança)
    // Usado em chamadas de método para verificar compatibilidade de argumentos
    private boolean isSubtype(Type t1, Type t2) { // t1 subtipo de t2
        return isAssignable(t2, t1); // Reutiliza a lógica de atribuibilidade
    }


    private void semanticError(String message) {
        // Em um compilador real, você coletaria os erros em uma lista,
        // possivelmente com número da linha e coluna.
        System.err.println("Erro Semântico: " + message);
        // Opcional: throw new SemanticException(message); para parar a análise
    }

    // =========================================================================
    // Métodos visit() para as Regras Gramaticais
    // =========================================================================

    @Override
    public Type visit(Program n) {
        // Definir a classe atual para MainClass antes de visitá-la
        this.currentClass = symbolTable.getClass(n.m.i1.s);
        n.m.accept(this);

        for (int i = 0; i < n.cl.size(); i++) {
            ClassDecl cd = n.cl.elementAt(i);
            if (cd instanceof ClassDeclSimple) {
                this.currentClass = symbolTable.getClass(((ClassDeclSimple) cd).i.s);
            } else { // ClassDeclExtends
                this.currentClass = symbolTable.getClass(((ClassDeclExtends) cd).i.s);
            }
            cd.accept(this);
        }
        this.currentClass = null; // Limpa a classe atual
        return null; // Program não tem tipo de retorno
    }

    @Override
    public Type visit(MainClass n) {
        // A main class não tem tipo de retorno para o método visit
        // O método main é especial e seu tipo não é avaliado como uma expressão.
        this.currentMethod = symbolTable.getMethod(n.i1.s, "main");
        n.s.accept(this); // Visita o Statement do corpo do main
        this.currentMethod = null;
        return null;
    }

    @Override
    public Type visit(ClassDeclSimple n) {
        // this.currentClass já foi definido no visit(Program)
        // Visitar variáveis de instância e métodos para checar seus tipos
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this); // Visita para garantir que tipos de VarDecl sejam resolvidos (se necessário)
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        return null;
    }

    @Override
    public Type visit(ClassDeclExtends n) {
        // this.currentClass já foi definido no visit(Program)
        // Primeiro, verificar se a classe estendida existe
        if (symbolTable.getClass(n.j.s) == null) {
            semanticError("Classe estendida '" + n.j.s + "' não declarada.");
        }

        // Visitar variáveis de instância e métodos
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        return null;
    }

    @Override
    public Type visit(VarDecl n) {
        // Apenas para travessia, a informação de tipo já está na tabela de símbolos
        // O tipo da variável é definido pela declaração, não por uma expressão.
        n.t.accept(this); // Apenas para visita recursiva, não retorna tipo
        n.i.accept(this); // Apenas para visita recursiva
        return null;
    }

    @Override
    public Type visit(MethodDecl n) {
        this.currentMethod = currentClass.getMethod(n.i.s);
        
        // Visita o tipo de retorno do método
        n.t.accept(this); // Não retorna tipo aqui, apenas garante que o Type é um nó válido

        // Visita parâmetros e variáveis locais
        for (int i = 0; i < n.fl.size(); i++) {
            n.fl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }

        // Visita statements para verificação de tipos
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }

        // Verifica o tipo da expressão de retorno
        Type returnExpType = n.e.accept(this);
        if (!isAssignable(n.t, returnExpType)) { // n.t é o tipo declarado do método
            semanticError("Tipo de retorno inválido no método '" + n.i.s + "'. Esperado: " + n.t.toString() + ", Obtido: " + returnExpType.toString());
        }

        this.currentMethod = null;
        return null; // MethodDecl não é uma expressão, não retorna um tipo
    }

    @Override
    public Type visit(Formal n) {
        // Apenas para travessia, informações já estão na tabela de símbolos
        n.t.accept(this);
        n.i.accept(this);
        return null;
    }

    // =========================================================================
    // Métodos visit() para Nodos de Tipo (apenas retorna o próprio tipo)
    // =========================================================================

    @Override
    public Type visit(IntArrayType n) {
        return n; // Retorna o próprio objeto Type da AST
    }

    @Override
    public Type visit(BooleanType n) {
        return n;
    }

    @Override
    public Type visit(IntegerType n) {
        return n;
    }

    @Override
    public Type visit(IdentifierType n) {
        // Para IdentifierType, verifique se a classe referenciada existe na tabela de símbolos
        if (symbolTable.getClass(n.s) == null) {
            semanticError("Tipo de classe '" + n.s + "' não declarado.");
            return null; // Retorna null para indicar um tipo inválido/desconhecido
        }
        return n; // Retorna o próprio objeto Type da AST
    }

    // =========================================================================
    // Métodos visit() para Statements (verificam consistência interna)
    // =========================================================================

    @Override
    public Type visit(Block n) {
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this); // Continua a verificação de tipos nos statements
        }
        return null;
    }

    @Override
    public Type visit(If n) {
        Type expType = n.e.accept(this); // Avalia o tipo da condição
        if (!(expType instanceof BooleanType)) {
            semanticError("Condição 'if' deve ser do tipo 'boolean'. Obtido: " + expType.toString());
        }
        n.s1.accept(this); // Visita o ramo 'then'
        n.s2.accept(this); // Visita o ramo 'else'
        return null;
    }

    @Override
    public Type visit(While n) {
        Type expType = n.e.accept(this); // Avalia o tipo da condição
        if (!(expType instanceof BooleanType)) {
            semanticError("Condição 'while' deve ser do tipo 'boolean'. Obtido: " + expType.toString());
        }
        n.s.accept(this); // Visita o corpo do loop
        return null;
    }

    @Override
    public Type visit(Print n) {
        // O System.out.println no MiniJava aceita apenas inteiros ou booleanos (depende da especificação)
        // Assumindo que aceita IntegerType
        Type expType = n.e.accept(this);
        if (!(expType instanceof IntegerType || expType instanceof BooleanType)) {
            semanticError("Argumento para 'System.out.println' deve ser do tipo 'int' ou 'boolean'. Obtido: " + expType.toString());
        }
        return null;
    }

    @Override
    public Type visit(Assign n) {
        // Pega o tipo do identificador (variável) do lado esquerdo
        Type idType = n.i.accept(this); // Identifier é um nó AST, retorna seu tipo como IdentifierType
        
        // Resolve o tipo real da variável na tabela de símbolos
        VarInfo varInfo = symbolTable.getVariable(currentClass.getName(), 
                                                 currentMethod != null ? currentMethod.getName() : null, 
                                                 n.i.s);
        if (varInfo == null) {
            semanticError("Variável '" + n.i.s + "' não declarada.");
            return null;
        }
        Type varDeclaredType = varInfo.getType(); // Tipo declarado para a variável

        // Pega o tipo da expressão do lado direito
        Type expType = n.e.accept(this);

        // Verifica compatibilidade de atribuição
        if (!isAssignable(varDeclaredType, expType)) {
            semanticError("Atribuição inválida para '" + n.i.s + "'. Esperado: " + varDeclaredType.toString() + ", Obtido: " + expType.toString());
        }
        return null;
    }

    @Override
    public Type visit(ArrayAssign n) {
        // Verifica se o identificador é um array
        VarInfo arrayVarInfo = symbolTable.getVariable(currentClass.getName(), 
                                                      currentMethod != null ? currentMethod.getName() : null, 
                                                      n.i.s);
        if (arrayVarInfo == null) {
            semanticError("Variável array '" + n.i.s + "' não declarada.");
            return null;
        }
        if (!(arrayVarInfo.getType() instanceof IntArrayType)) {
            semanticError("Variável '" + n.i.s + "' não é do tipo 'int[]'.");
        }

        // Verifica se o índice é um inteiro
        Type indexType = n.e1.accept(this);
        if (!(indexType instanceof IntegerType)) {
            semanticError("Índice de array deve ser do tipo 'int'. Obtido: " + indexType.toString());
        }

        // Verifica se o valor atribuído é um inteiro
        Type valueType = n.e2.accept(this);
        if (!(valueType instanceof IntegerType)) {
            semanticError("Valor atribuído a elemento de array deve ser do tipo 'int'. Obtido: " + valueType.toString());
        }
        return null;
    }

    // =========================================================================
    // Métodos visit() para Expressões (retornam o tipo da expressão)
    // =========================================================================

    @Override
    public Type visit(And n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);
        if (!(e1Type instanceof BooleanType && e2Type instanceof BooleanType)) {
            semanticError("Operadores '&&' devem ser do tipo 'boolean'. Obtidos: " + e1Type.toString() + " e " + e2Type.toString());
            return null; // Indica um erro de tipo
        }
        return new BooleanType(); // O resultado de '&&' é um booleano
    }

    @Override
    public Type visit(LessThan n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);
        if (!(e1Type instanceof IntegerType && e2Type instanceof IntegerType)) {
            semanticError("Operadores '<' devem ser do tipo 'int'. Obtidos: " + e1Type.toString() + " e " + e2Type.toString());
            return null;
        }
        return new BooleanType(); // O resultado de '<' é um booleano
    }

    @Override
    public Type visit(Plus n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);
        if (!(e1Type instanceof IntegerType && e2Type instanceof IntegerType)) {
            semanticError("Operadores '+' devem ser do tipo 'int'. Obtidos: " + e1Type.toString() + " e " + e2Type.toString());
            return null;
        }
        return new IntegerType(); // O resultado de '+' é um inteiro
    }

    @Override
    public Type visit(Minus n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);
        if (!(e1Type instanceof IntegerType && e2Type instanceof IntegerType)) {
            semanticError("Operadores '-' devem ser do tipo 'int'. Obtidos: " + e1Type.toString() + " e " + e2Type.toString());
            return null;
        }
        return new IntegerType();
    }

    @Override
    public Type visit(Times n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);
        if (!(e1Type instanceof IntegerType && e2Type instanceof IntegerType)) {
            semanticError("Operadores '*' devem ser do tipo 'int'. Obtidos: " + e1Type.toString() + " e " + e2Type.toString());
            return null;
        }
        return new IntegerType();
    }

    @Override
    public Type visit(ArrayLookup n) {
        Type arrayType = n.e1.accept(this);
        if (!(arrayType instanceof IntArrayType)) {
            semanticError("Expressão em 'array lookup' deve ser do tipo 'int[]'. Obtido: " + arrayType.toString());
            return null;
        }
        Type indexType = n.e2.accept(this);
        if (!(indexType instanceof IntegerType)) {
            semanticError("Índice de array deve ser do tipo 'int'. Obtido: " + indexType.toString());
            return null;
        }
        return new IntegerType(); // Um elemento de array int[] é um int
    }

    @Override
    public Type visit(ArrayLength n) {
        Type arrayType = n.e.accept(this);
        if (!(arrayType instanceof IntArrayType)) {
            semanticError("Expressão '.length' deve ser do tipo 'int[]'. Obtido: " + arrayType.toString());
            return null;
        }
        return new IntegerType(); // length retorna um inteiro
    }

    @Override
    public Type visit(Call n) {
        Type expType = n.e.accept(this); // Tipo do objeto no qual o método é chamado
        if (!(expType instanceof IdentifierType)) {
            semanticError("Chamada de método deve ser em um objeto. Obtido: " + expType.toString());
            return null;
        }
        String className = ((IdentifierType) expType).s;
        String methodName = n.i.s;

        // Recupera o método da tabela de símbolos
        MethodInfo methodInfo = symbolTable.getMethod(className, methodName);
        if (methodInfo == null) {
            semanticError("Método '" + methodName + "' não encontrado na classe '" + className + "'.");
            return null;
        }

	List<VarInfo> methodParametersList = new ArrayList<>(methodInfo.getParameters().values());

        // Verifica o número de argumentos
        if (n.el.size() != methodInfo.getParameters().size()) { // Acesso direto ao map de parâmetros de MethodInfo
            semanticError("Número incorreto de argumentos para o método '" + methodName + "'. Esperado: " + methodInfo.getParameters().size() + ", Obtido: " + n.el.size());
            return null;
        }

        // Verifica os tipos dos argumentos
        for (int i = 0; i < n.el.size(); i++) {
            Type argExpType = n.el.elementAt(i).accept(this);
            // Recupera o tipo do i-ésimo parâmetro do método
            // Isso requer acesso por índice ou uma lista de parâmetros no MethodInfo
            // Assumindo que MethodInfo tem um método para pegar o tipo do parâmetro pelo índice
            Type paramType = methodParametersList.get(i).getType();
            
            if (!isAssignable(paramType, argExpType)) {
                semanticError("Tipo de argumento incompatível na chamada do método '" + methodName + "'. Parâmetro " + (i+1) + ": Esperado: " + paramType.toString() + ", Obtido: " + argExpType.toString());
            }
        }

        return methodInfo.getReturnType(); // Retorna o tipo de retorno do método
    }

    @Override
    public Type visit(IntegerLiteral n) {
        return new IntegerType();
    }

    @Override
    public Type visit(True n) {
        return new BooleanType();
    }

    @Override
    public Type visit(False n) {
        return new BooleanType();
    }

    @Override
    public Type visit(IdentifierExp n) {
        // Resolve o tipo do identificador usando a tabela de símbolos
        VarInfo varInfo = symbolTable.getVariable(currentClass.getName(), 
                                                 currentMethod != null ? currentMethod.getName() : null, 
                                                 n.s); // n.s é o nome do identificador
        if (varInfo == null) {
            semanticError("Variável '" + n.s + "' não declarada ou fora do escopo.");
            return null; // Indica tipo desconhecido devido a erro
        }
        return varInfo.getType(); // Retorna o tipo da variável
    }

    @Override
    public Type visit(This n) {
        // 'this' refere-se à instância da classe atual
        return new IdentifierType(currentClass.getName());
    }

    @Override
    public Type visit(NewArray n) {
        Type sizeExpType = n.e.accept(this);
        if (!(sizeExpType instanceof IntegerType)) {
            semanticError("Tamanho do array deve ser do tipo 'int'. Obtido: " + sizeExpType.toString());
            return null;
        }
        return new IntArrayType();
    }

    @Override
    public Type visit(NewObject n) {
        String className = n.i.s;
        if (symbolTable.getClass(className) == null) {
            semanticError("Classe '" + className + "' não declarada.");
            return null;
        }
        return new IdentifierType(className); // Retorna o tipo do novo objeto (ex: new MyClass() -> tipo MyClass)
    }

    @Override
    public Type visit(Not n) {
        Type expType = n.e.accept(this);
        if (!(expType instanceof BooleanType)) {
            semanticError("Operador '!' deve ser aplicado a uma expressão booleana. Obtido: " + expType.toString());
            return null;
        }
        return new BooleanType();
    }

    @Override
    public Type visit(Identifier n) {
        // Nodos Identifier simples não devem ter um tipo diretamente.
        // Eles são resolvidos em IdentifierExp ou nas declarações.
        // Este método pode ser usado para depuração ou pode retornar null.
        return null; // Ou um tipo especial "UnknownType" se você tiver um.
    }
}