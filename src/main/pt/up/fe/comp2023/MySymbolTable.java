package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class MySymbolTable implements SymbolTable {
    private HashMap<String, ClassDescriptor> classDescriptorMap;
    private Map<String, FieldDescriptor> fieldDescriptor;
    private Map<String, MethodDescriptor> methodDescriptor;

    private ArrayList<String> imports;
    private ClassDescriptor mainClass;

    private Symbol symbol;

    private String accessModifier = "";

    public MySymbolTable(JmmNode root){
        classDescriptorMap = new HashMap<>();
        fieldDescriptor = new HashMap<>();
        methodDescriptor = new HashMap<>();
        imports = new ArrayList<>();
        new MyVisitor(this).visit(root);
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
        for (FieldDescriptor fieldDescriptor : fieldDescriptor.values()){
            symbols.add(fieldDescriptor.getSymbol());
        }
        return symbols;
    }

    @Override
    public List<String> getMethods() {
        List<String> methods = new ArrayList<>();
        for (String methodName : methodDescriptor.keySet()){
            methods.add(methodName);
        }
        return methods;
    }

    @Override
    public Type getReturnType(String s) {
        return methodDescriptor.get(s).getReturnType();
    }

    @Override
    public List<Symbol> getParameters(String s) {
        return methodDescriptor.get(s).getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        return methodDescriptor.get(s).getLocalVariables();
    }

    public void setMainClass(ClassDescriptor mainClass) {
        this.mainClass = mainClass;
    }

    public ClassDescriptor getMainClass(){
        return mainClass;
    }

    //Add Nodes

    public void addImport(JmmNode node){
        imports.add(node.get("library"));
    }

    public void addClass(JmmNode node){
        ClassDescriptor classDescriptor = new ClassDescriptor(node);
        if(mainClass == null){
            mainClass = classDescriptor;
        }
        classDescriptorMap.put(node.get("name"), classDescriptor);
    }

    public void addMethod(JmmNode node){
        JmmNode instanceNode = node.getJmmChild(0);
        if(Objects.equals(instanceNode.getKind(), "MainDeclaration")){
            methodDescriptor.put("main", new MethodDescriptor(instanceNode));
        }
        else{
            methodDescriptor.put(instanceNode.get("instance"), new MethodDescriptor(instanceNode));
        }
    }

    public void addField(JmmNode node){
        fieldDescriptor.put(node.get("var"), new FieldDescriptor(node));
    }

    public void addLocalVar(String methodName, JmmNode varNode){
        methodDescriptor.get(methodName).addVar(varNode);
    }
}
