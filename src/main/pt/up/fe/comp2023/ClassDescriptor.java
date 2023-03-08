package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class ClassDescriptor implements Table{
    private Map<String, FieldDescriptor> fieldDescriptor;
    private Map<String, MethodDescriptor> methodDescriptor;
    private String className;
    private String extendedClassName;

    public ClassDescriptor(JmmNode node) {
        fieldDescriptor = new HashMap<>();
        methodDescriptor = new HashMap<>();
        className = node.get("name");
        extendedClassName = node.hasAttribute("superclass") ? node.get("superclass") : null;
        buildTable(node);
    }

    public String getClassName() {
        return className;
    }

    public String getExtendedClassName(){
        return extendedClassName;
    }

    public Map<String, FieldDescriptor> getFieldDescriptor(){
        return fieldDescriptor;
    }

    public Map<String, MethodDescriptor> getMethodDescriptor(){
        return methodDescriptor;
    }



    public void setExtendedClassName(String name){
        this.extendedClassName = name;
    }

    @Override
    public void buildTable(JmmNode root) {
        for(JmmNode node : root.getChildren()){
            if(Objects.equals(node.getKind(), "MethodDeclaration")){
                JmmNode method = node.getJmmChild(0);
                if(Objects.equals(method.getKind(), "MainDeclaration")){
                    methodDescriptor.put("main", new MethodDescriptor(method));
                }
                else{
                    methodDescriptor.put(method.get("instance"), new MethodDescriptor(method));
                }
            }
            else if(Objects.equals(node.getKind(), "FieldDeclaration")){
                fieldDescriptor.put(node.get("var"), new FieldDescriptor(node));
            }
        }
    }
}
