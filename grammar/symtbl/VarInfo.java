package symtbl;

import syntaxtree.*;

public class VarInfo {
    private String name;
    private Type type;

    public VarInfo(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void printVarInfo(String indent) {
        System.out.println(indent + "Nome: " + name + ", Tipo: " + (type != null ? type.toString() : "Desconhecido"));
    }
}