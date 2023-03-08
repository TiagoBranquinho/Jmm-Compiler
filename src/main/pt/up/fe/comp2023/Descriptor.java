package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class Descriptor implements SymbolTable, Table {
    private HashMap<String, ClassDescriptor> classDescriptorMap;

    private ArrayList<String> imports;
    private ClassDescriptor mainClass;

    public Descriptor(JmmNode root){
        classDescriptorMap = new HashMap<>();
        imports = new ArrayList<>();
        buildTable(root);
    }
    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return mainClass.getClassName();
    }

    @Override
    public String getSuper() {
        return mainClass.getExtendedClassName();
    }

    @Override
    public List<Symbol> getFields() {
        List<Symbol> symbols = new ArrayList<>();
        for (FieldDescriptor fieldDescriptor : mainClass.getFieldDescriptor().values()){
            symbols.add(fieldDescriptor.getSymbol());
        }
        return symbols;
    }

    @Override
    public List<String> getMethods() {
        List<String> methods = new ArrayList<>();
        for (String methodName : mainClass.getMethodDescriptor().keySet()){
            methods.add(methodName);
        }
        return methods;
    }

    @Override
    public Type getReturnType(String s) {
        return mainClass.getMethodDescriptor().get(s).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return mainClass.getMethodDescriptor().get(s).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return mainClass.getMethodDescriptor().get(s).getLocalVariables();
    }

    public void setMainClass(ClassDescriptor mainClass) {
        this.mainClass = mainClass;
    }

    @Override
    public String toString() {
        String print = "\n Symbol table:\n";
        print += "imports: \n";
        for (String lib : imports){
            print += "lib: " + lib + " ";
        }
        print += "\nmain class: " + mainClass.toString();

        for (ClassDescriptor classDescriptor : classDescriptorMap.values()){
            print += classDescriptor.toString();
            print += "\n";
        }
        return print;
    }

    @Override
    public void buildTable(JmmNode root) {
        for (JmmNode node : root.getChildren()){
            if(Objects.equals(node.getKind(), "ImportDeclaration")){
                imports.add(node.get("library"));
            }
            else if(Objects.equals(node.getKind(), "ClassDeclaration")){
                mainClass = new ClassDescriptor(node);
                classDescriptorMap.put(node.get("name"), mainClass);

            }
        }
    }
}
