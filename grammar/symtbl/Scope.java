package symtbl;

import syntaxtree.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Scope {
    private String name;
    private Scope parentScope;
    protected Map<String, VarInfo> variables; // Variáveis diretas neste escopo (locais, parâmetros)

    public Scope(String name) {
        this.name = name;
        this.variables = new LinkedHashMap<>();
    }

    public Scope(String name, Scope parentScope) {
        this(name);
        this.parentScope = parentScope;
    }

    public String getName() {
        return name;
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public void addVar(String name, VarInfo varInfo) {
        if (variables.containsKey(name)) {
            System.err.println("Erro Semântico: Variável '" + name + "' já declarada neste escopo.");
        } else {
            variables.put(name, varInfo);
        }
    }

    // Busca uma variável neste escopo ou em seus pais diretos (não herança de classe)
    public VarInfo getVar(String name) {
        VarInfo var = variables.get(name);
        if (var == null && parentScope != null) {
            // Se não encontrar aqui, tenta no escopo pai (ex: de método para classe, de bloco para método)
            return parentScope.getVar(name);
        }
        return var;
    }

    public void printScopeInfo(String indent) {
        System.out.println(indent + "Escopo: " + name);
        System.out.println(indent + "  Variáveis:");
        if (variables.isEmpty()) {
            System.out.println(indent + "    Nenhum");
        } else {
            for (Map.Entry<String, VarInfo> entry : variables.entrySet()) {
                entry.getValue().printVarInfo(indent + "    ");
            }
        }
    }
}