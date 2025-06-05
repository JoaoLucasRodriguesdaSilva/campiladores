package symtbl;

import syntaxtree.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassInfo extends Scope {
    private String extendsClass; // Nome da classe que esta estende

    private Map<String, VarInfo> fields;   // Variáveis de instância (campos)
    private Map<String, MethodInfo> methods; // Métodos declarados na classe

    public ClassInfo(String name, String extendsClass, Scope parentScope) {
        super(name, parentScope); // Uma classe tem o escopo global como pai
        this.extendsClass = extendsClass;
        this.fields = new LinkedHashMap<>();
        this.methods = new LinkedHashMap<>();
    }

    public String getExtendsClass() {
        return extendsClass;
    }

    public void addField(String name, VarInfo varInfo) {
        if (fields.containsKey(name)) {
            System.err.println("Erro Semântico: Campo '" + name + "' já declarado na classe '" + getName() + "'.");
        } else {
            fields.put(name, varInfo);
        }
    }

    public VarInfo getField(String name) {
        return fields.get(name);
    }

    public void addMethod(String name, MethodInfo methodInfo) {
        if (methods.containsKey(name)) {
            System.err.println("Erro Semântico: Método '" + name + "' já declarado na classe '" + getName() + "'.");
        } else {
            methods.put(name, methodInfo);
        }
    }

    public MethodInfo getMethod(String name) {
        return methods.get(name);
    }

    public void printClassInfo(String indent) {
        System.out.println(indent + "Nome da Classe: " + getName());
        if (extendsClass != null) {
            System.out.println(indent + "  Estende: " + extendsClass);
        }
        System.out.println(indent + "  Campos:");
        if (fields.isEmpty()) {
            System.out.println(indent + "    Nenhum");
        } else {
            for (Map.Entry<String, VarInfo> entry : fields.entrySet()) {
                entry.getValue().printVarInfo(indent + "    ");
            }
        }
        System.out.println(indent + "  Métodos:");
        if (methods.isEmpty()) {
            System.out.println(indent + "    Nenhum");
        } else {
            for (Map.Entry<String, MethodInfo> entry : methods.entrySet()) {
                System.out.println(indent + "    Nome do Método: " + entry.getKey());
                entry.getValue().printMethodInfo(indent + "      ");
            }
        }
    }
}