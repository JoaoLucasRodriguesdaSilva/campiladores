package symtbl;

import symtbl.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTable {
    private Map<String, ClassInfo> classes;
    private Stack<Scope> scopeStack; // Pilha para gerenciar escopos

    public SymbolTable() {
        this.classes = new LinkedHashMap<>();
        this.scopeStack = new Stack<>();
        // O escopo global é o primeiro a ser empilhado
        this.enterScope(new Scope("GlobalScope")); // Um escopo genérico para o nível global
    }

    public void enterScope(Scope newScope) {
        scopeStack.push(newScope);
    }

    public void exitScope() {
        if (!scopeStack.empty()) {
            scopeStack.pop();
        }
    }

    public Scope getCurrentScope() {
        if (scopeStack.empty()) {
            System.err.println("Erro interno: Tentativa de acessar escopo vazio.");
            return null;
        }
        return scopeStack.peek();
    }

    public void addClass(String name, ClassInfo classInfo) {
        if (classes.containsKey(name)) {
            System.err.println("Erro Semântico: Classe '" + name + "' já declarada.");
        } else {
            classes.put(name, classInfo);
        }
    }

    public ClassInfo getClass(String name) {
        return classes.get(name);
    }

    /**
     * Procura um método em uma classe (e sua cadeia de herança).
     */
    public MethodInfo getMethod(String className, String methodName) {
        ClassInfo currentClass = getClass(className);
        while (currentClass != null) {
            MethodInfo method = currentClass.getMethod(methodName);
            if (method != null) {
                return method;
            }
            // Se o método não foi encontrado na classe atual, tenta na classe pai
            String extendsClassName = currentClass.getExtendsClass();
            if (extendsClassName != null) {
                currentClass = getClass(extendsClassName);
            } else {
                currentClass = null; // Fim da cadeia de herança
            }
        }
        return null;
    }

    /**
     * Procura uma variável (local, parâmetro, campo da classe, campo herdado).
     */
    public VarInfo getVariable(String className, String methodName, String varName) {
        // 1. Procura no escopo do método (variáveis locais e parâmetros)
        if (methodName != null) {
            MethodInfo method = getMethod(className, methodName);
            if (method != null) {
                VarInfo var = method.getVar(varName); // getVar na Scope já sobe a hierarquia
                if (var != null) return var;
            }
        }

        // 2. Procura nos campos da classe e na cadeia de herança
        ClassInfo currentClass = getClass(className);
        while (currentClass != null) {
            VarInfo var = currentClass.getField(varName);
            if (var != null) {
                return var;
            }
            String extendsClassName = currentClass.getExtendsClass();
            if (extendsClassName != null) {
                currentClass = getClass(extendsClassName);
            } else {
                currentClass = null; // Fim da cadeia de herança
            }
        }
        return null;
    }


    public void printSymbolTable() {
        System.out.println("--- Tabela de Símbolos ---");
        for (Map.Entry<String, ClassInfo> entry : classes.entrySet()) {
            System.out.println("Classe: " + entry.getKey());
            entry.getValue().printClassInfo("  ");
        }
        System.out.println("--------------------------");
    }
}