package symtbl;

import syntaxtree.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MethodInfo extends Scope {
    private Type returnType;
    private Map<String, VarInfo> parameters;

    public MethodInfo(String name, Type returnType, Scope parentScope) {
        super(name, parentScope); // Um método tem o escopo da classe como pai
        this.returnType = returnType;
        this.parameters = new LinkedHashMap<>();
    }

    public Type getReturnType() {
        return returnType;
    }

    public void addParameter(String name, VarInfo paramInfo) {
        if (parameters.containsKey(name) || variables.containsKey(name)) { // Parâmetros não podem duplicar locals
            System.err.println("Erro Semântico: Parâmetro '" + name + "' já declarado no método '" + getName() + "'.");
        } else {
            parameters.put(name, paramInfo);
            // Parâmetros também são considerados variáveis no escopo do método
            super.addVar(name, paramInfo);
        }
    }

    public Map<String, VarInfo> getParameters(){
        return this.parameters;
    }

    public VarInfo getParameter(String name) {
        return parameters.get(name);
    }
    
    // Sobrescreve getVar para procurar em parâmetros e depois em variáveis locais
    @Override
    public VarInfo getVar(String name) {
        VarInfo var = parameters.get(name);
        if (var != null) return var;
        
        // Se não for parâmetro, procura nas variáveis locais (herdado de Scope)
        return super.getVar(name);
    }

    public void printMethodInfo(String indent) {
        System.out.println(indent + "Tipo de Retorno: " + (returnType != null ? returnType.toString() : "void")); // Tratamento para Main
        System.out.println(indent + "  Parâmetros:");
        if (parameters.isEmpty()) {
            System.out.println(indent + "    Nenhum");
        } else {
            for (Map.Entry<String, VarInfo> entry : parameters.entrySet()) {
                entry.getValue().printVarInfo(indent + "    ");
            }
        }
        System.out.println(indent + "  Variáveis Locais:");
        // Imprimir apenas variáveis que são locais e não parâmetros
        if (variables.isEmpty() || variables.size() == parameters.size()) { // se só tem parâmetros, não tem locals adicionais
             System.out.println(indent + "    Nenhum");
        } else {
            for (Map.Entry<String, VarInfo> entry : variables.entrySet()) {
                if (!parameters.containsKey(entry.getKey())) { // Verifica se não é um parâmetro
                    entry.getValue().printVarInfo(indent + "    ");
                }
            }
        }
    }
}