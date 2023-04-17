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
        System.out.print(ollirCode.toString());
        return new OllirResult(ollirCode.toString(), jmmSemanticsResult.getConfig());
    }

    public void appendToOllir(String code){
        ollirCode.append(code);
    }

    private String typeToOllir(Type type){
        StringBuilder ret = new StringBuilder();
        if(type.isArray())
            ret.append(".array");
        String typeName = type.getName();
        switch (typeName) {
            case "int" -> ret.append(".i32");
            case "void" -> ret.append(".V");
            case "boolean" -> ret.append(".bool");
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
        accessModifier = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.getJmmChild(0).get("value") : "public static";
        ollirCode.append(".method ").append(accessModifier).append(" ").append(name).append("(");
        for(Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)){
            ollirCode.append(parameter.getName()).append(typeToOllir(parameter.getType())).append(", ");
        }
        if(!jmmSemanticsResult.getSymbolTable().getParameters(name).isEmpty()){
            ollirCode.delete(ollirCode.length() - 2, ollirCode.length());
        }
        ollirCode.append(")");
        ollirCode.append(typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(name))).append(" {\n");
    }

    public void addMethodRetType(JmmNode instance, JmmNode retStmt){
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        String retType = typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(name));

        ollirCode.append("ret").append(retType);

        if(!retType.equals(".V")){
            for(Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)){
                if(Objects.equals(localVar.getName(), retStmt.getJmmChild(0).get("value"))){
                    ollirCode.append(" ").append(localVar.getName()).append(typeToOllir(localVar.getType()));
                    return;
                }
            }
            int i = 0;
            for(Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)){
                if(Objects.equals(parameter.getName(), retStmt.getJmmChild(0).get("value"))){
                    ollirCode.append(" ").append("$").append(i).append(".").append(parameter.getName()).append(typeToOllir(parameter.getType()));
                    ollirCode.append(";\n");
                    return;
                }
                i++;
            }

        }
        ollirCode.append(";\n");

    }

    public void addField(JmmNode field){
        String accessModifier = field.getNumChildren() == 2 ? field.getJmmChild(0).get("value") : "private";
        ollirCode.append(".field ").append(accessModifier);

        for(Symbol f : jmmSemanticsResult.getSymbolTable().getFields()){
            if(Objects.equals(f.getName(), field.get("var"))){
                ollirCode.append(" ").append(f.getName()).append(typeToOllir(f.getType())).append(";\n\n");
                break;
            }
        }
    }

    public void addVar(JmmNode instance, JmmNode var){
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        for(Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)){
            if(Objects.equals(localVar.getName(), var.get("var"))){
                ollirCode.append(localVar.getName()).append(typeToOllir(localVar.getType()));
                return;
            }
        }
    }




}
