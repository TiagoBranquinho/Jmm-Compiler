package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Objects;

public class Optimization implements JmmOptimization {
    private StringBuilder ollirCode = new StringBuilder();

    private JmmSemanticsResult jmmSemanticsResult;
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        this.jmmSemanticsResult = jmmSemanticsResult;
        new MyVisitor(this).visit(jmmSemanticsResult.getRootNode());
        ollirCode.append("\n\n}\n");
        System.out.print(ollirCode.toString());
        return new OllirResult(ollirCode.toString(), jmmSemanticsResult.getConfig());
    }

    private String typeToOllir(Type type){
        StringBuilder ret = new StringBuilder();
        if(type.isArray())
            ret.append(".array");
        String typeName = type.getName();
        switch (typeName) {
            case "int" -> ret.append(".i32");
            case "void" -> ret.append(".V");
            default -> ret.append(".").append(typeName);
        }

        return ret.toString();
    }

    public void addImports(){
        for(String importString : jmmSemanticsResult.getSymbolTable().getImports()){
            ollirCode.append("import ").append(importString).append(";\n");
        }
    }

    public void addClass(){
        String className = jmmSemanticsResult.getSymbolTable().getClassName();
        String extend = Objects.equals(jmmSemanticsResult.getSymbolTable().getSuper(), null) ? "" : " extends " + jmmSemanticsResult.getSymbolTable().getSuper();
        ollirCode.append(className).append(extend).append(" {\n").append(
        ".construct ").append(className).append("().V {\n").append(
        "invokespecial(this, \"<init>\").V;\n").append(
        "}\n\n");
    }

    public void addMethod(JmmNode jmmNode){
        String name;
        String accessModifier = "public";
        JmmNode instance = jmmNode.getJmmChild(0);
        name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        //accessModifier = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("accessModifier") : "public";
        ollirCode.append(".method ").append(accessModifier).append(" ").append(name).append("(");
        for(Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)){
            ollirCode.append(parameter.getName()).append(typeToOllir(parameter.getType())).append(", ");
        }
        if(!jmmSemanticsResult.getSymbolTable().getParameters(name).isEmpty()){
            ollirCode.delete(ollirCode.length() - 2, ollirCode.length());
        }
        ollirCode.append(")");
        ollirCode.append(typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(name))).append(" {\n");
        ollirCode.append("inside method\n");
        ollirCode.append("}\n");
    }
}
