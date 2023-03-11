package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MethodDescriptor {
    private List<Symbol> parameters;
    private Type returnType;
    private List<Symbol> localVariables;

    private int voidFunc;
    public MethodDescriptor(JmmNode root){
        parameters = new ArrayList<>();
        localVariables = new ArrayList<>();
        voidFunc = checkReturn(root);
        generateArgs(root);
        generateVars(root);
        setReturnType(root);

    }

    private int checkReturn(JmmNode root){
        for (JmmNode node : root.getChildren()){
            if(Objects.equals(node.getKind(), "ReturnStmt")){
                return 1;
            }
        }
        return 0;
    }
    private void setReturnType(JmmNode root){
        if(voidFunc == 1){
            String retString = root.getJmmChild(0).get("value");
            boolean isArray = retString.endsWith("[]");
            if(isArray){
                retString = retString.substring(0, retString.length() - 2);
            }
            returnType = new Type(retString, isArray);
        }
        else{
            returnType = new Type("void", false);
        }
    }

    private void generateArgs(JmmNode root){
        List<String> args;
        String argString = root.get("parameter");
        if(argString.charAt(0) == '[' && argString.endsWith("]")){
            argString = argString.substring(1, argString.length() - 1);
        }

        args = List.of(argString.split(", "));
        if (args.size() == 1 && Objects.equals(args.get(0), "")){
            args = new ArrayList<String>();
        }
        for(int i = 0; i < args.size(); i++){
            String retString = root.getJmmChild(i + voidFunc).get("value");
            boolean isArray = retString.endsWith("[]");
            if(isArray){
                retString = retString.substring(0, retString.length() - 2);
            }
            parameters.add(new Symbol(new Type(retString, isArray), args.get(i)));
        }
    }

    private void generateVars(JmmNode root){
        String type;
        boolean isArray;
        for(JmmNode node : root.getChildren()){
            if(Objects.equals(node.getKind(), "VarDeclarationStmt")){
                type = node.getJmmChild(0).get("value");
                isArray = type.endsWith("[]");
                if(isArray){
                    type = type.substring(0, type.length() - 2);
                }
                localVariables.add(new Symbol(new Type(type, isArray), node.get("var")));
            }
        }
    }

    public void addVar(JmmNode var){
        String type;
        boolean isArray;
        if(Objects.equals(var.getKind(), "VarDeclarationStmt")){
            type = var.getJmmChild(0).get("value");
            isArray = type.endsWith("[]");
            if(isArray){
                type = type.substring(0, type.length() - 2);
            }
            localVariables.add(new Symbol(new Type(type, isArray), var.get("var")));
        }
    }


    public Type getReturnType() {
        return returnType;
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return localVariables;
    }
}
