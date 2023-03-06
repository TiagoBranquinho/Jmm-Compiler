package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MethodDescriptor implements Table{
    private List<Symbol> parameters;
    private Type returnType;
    private List<Symbol> localVariables;
    public MethodDescriptor(JmmNode root){
        buildTable(root);
        parameters = new ArrayList<>();
        localVariables = new ArrayList<>();
        generateArgs(root);
        generateVars(root);
        setReturnType(root);

    }

    private void setReturnType(JmmNode root){
        String retString = root.getJmmChild(0).get("value");
        boolean isArray = retString.endsWith("[]");
        if(isArray){
            retString = retString.substring(0, retString.length() - 2);
        }
        returnType = new Type(retString, isArray);
    }

    private void generateArgs(JmmNode root){
        List<String> args = new ArrayList<>();
        String argString = root.get("parameter");
        args = List.of(argString.split(", "));

        int iMax = args.size();

        if(Objects.equals(root.getKind(), "MainDeclaration")){
            iMax--;
        }

        for(int i = 1; i < iMax; i++){
            String retString = root.getJmmChild(i).get("value");
            boolean isArray = retString.endsWith("[]");
            if(isArray){
                retString = retString.substring(0, retString.length() - 2);
            }
            parameters.add(new Symbol(new Type(retString, isArray), args.get(i - 1)));
        }
    }

    private void generateVars(JmmNode root){
        //for(JmmNode node : root.getChildren()){
          //  if()
        //}
        localVariables.add(new Symbol(new Type("int", false), root.get("parameter")));
    }


    public Type getReturnType() {
        return new Type("int", false);
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return localVariables;
    }

    @Override
    public void buildTable(JmmNode node) {

    }
}
