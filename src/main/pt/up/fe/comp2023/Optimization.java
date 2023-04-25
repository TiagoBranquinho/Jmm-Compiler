package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Optimization implements JmmOptimization {
    private StringBuilder ollirCode = new StringBuilder();

    private JmmSemanticsResult jmmSemanticsResult;

    private int tempNumber = 0;

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        this.jmmSemanticsResult = jmmSemanticsResult;
        new OllirGenerator(this).visit(jmmSemanticsResult.getRootNode());
        System.out.print(ollirCode.toString());
        return new OllirResult(ollirCode.toString(), jmmSemanticsResult.getConfig());
    }

    public void appendToOllir(String code){
        ollirCode.append(code);
    }

    public int getTempNumber(){
        return tempNumber++;
    }

    public void decreaseTempNumber(){tempNumber--;}

    public String intToOllir(JmmNode integer){
        return integer.get("value") + typeToOllir(new Type("int", false));
    }

    public String initObjectDeclaration(JmmNode declaration, JmmNode assignment, JmmNode instance){
        StringBuilder ret = new StringBuilder();
        String objClass = declaration.get("objClass");
        String type = typeToOllir(new Type(objClass, false));
        int tempNumber = this.getTempNumber();
        ollirCode.append("temp_").append(tempNumber).append(type).append(" :=").append(type).append(" new(").append(objClass).append(")").append(type);
        ollirCode.append(";\n");
        ollirCode.append("invokespecial(temp_").append(tempNumber).append(type).append(",\"<init>\").V");
        ollirCode.append(";\n");
        ret.append("temp_").append(tempNumber).append(type);
        return ret.toString();
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

    public String getMethodRetType(JmmNode instance){
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        return typeToOllir(jmmSemanticsResult.getSymbolTable().getReturnType(name));
    }


    public void addImport(JmmNode node){
        String library = node.get("library");
        library = library.substring(1, library.length() - 1);
        ollirCode.append("import ");
        for(String item : library.split(", ")){
            ollirCode.append(item).append(".");

        }
        ollirCode.deleteCharAt(ollirCode.length() - 1);
        ollirCode.append(";\n");
    }

    public void addClass(){
        if(!ollirCode.isEmpty())
            ollirCode.append("\n");
        String className = jmmSemanticsResult.getSymbolTable().getClassName();
        String extend = Objects.equals(jmmSemanticsResult.getSymbolTable().getSuper(), null) ? "" : " extends " + jmmSemanticsResult.getSymbolTable().getSuper();
        ollirCode.append(className).append(extend).append(" {\n");

    }

    public void addConstructor(){
        String className = jmmSemanticsResult.getSymbolTable().getClassName();
        ollirCode.append("\n").append(
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
                    ollirCode.append(";\n");
                    return;
                }
            }
            int i = 1;
            for(Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)){
                if(Objects.equals(parameter.getName(), retStmt.getJmmChild(0).get("value"))){
                    ollirCode.append(" ").append("$").append(i).append(".").append(parameter.getName()).append(typeToOllir(parameter.getType()));
                    ollirCode.append(";\n");
                    return;
                }
                i++;
            }
            if(isNumeric(retStmt.getJmmChild(0).get("value"))){
                ollirCode.append(" ").append(retStmt.getJmmChild(0).get("value")).append(typeToOllir(new Type("int", false)));
            }
            if(Objects.equals(retStmt.getJmmChild(0).get("value"), "true") || Objects.equals(retStmt.getJmmChild(0).get("value"), "false")){
                ollirCode.append(" ").append(retStmt.getJmmChild(0).get("value")).append(typeToOllir(new Type("bool", false)));
            }

        }
        ollirCode.append(";\n");

    }

    public void addField(JmmNode field){
        String accessModifier = field.getNumChildren() == 2 ? field.getJmmChild(0).get("value") : "private";
        ollirCode.append(".field ").append(accessModifier);

        for(Symbol f : jmmSemanticsResult.getSymbolTable().getFields()){
            if(Objects.equals(f.getName(), field.get("var"))){
                ollirCode.append(" ").append(f.getName()).append(typeToOllir(f.getType())).append(";\n");
                break;
            }
        }
    }

    public String getVarOrType(JmmNode node, JmmNode instance, String condition){
        StringBuilder retString = new StringBuilder();

        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        String var = Objects.equals(node.getKind(), "Assignment") ? node.get("var") : node.get("value");
        if(isNumeric(var)){
            return this.intToOllir(node);
        }
        for(Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)){
            if(Objects.equals(localVar.getName(), var)){
                if(Objects.equals(condition, "var"))
                    retString.append(var);
                retString.append(typeToOllir(localVar.getType()));
                return retString.toString();
            }
        }
        for(Symbol localVar : jmmSemanticsResult.getSymbolTable().getFields()){
            if(Objects.equals(localVar.getName(), var)){
                //int tempNumber = this.getTempNumber();
                //ollirCode.append()
                if(Objects.equals(condition, "var")){
                    retString.append(var);
                }
                retString.append(typeToOllir(localVar.getType()));
                return retString.toString();
            }
        }
        int i = 1;
        for(Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)){
            if(Objects.equals(parameter.getName(), var)){
                if(Objects.equals(condition, "var"))
                    retString.append("$").append(i).append(".").append(var);
                retString.append(typeToOllir(parameter.getType()));
                return retString.toString();
            }
            i++;
        }
        return node.get("value");
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public String getInvoke(JmmNode dotOp, JmmNode instance) {
        JmmNode left = dotOp.getJmmChild(0);
        while(!left.hasAttribute("value")){
            left = left.getJmmChild(0);
        }
        if(Objects.equals(left.get("value"), "this")){
            return "invokevirtual(";
        }
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";
        for(Symbol localVar : jmmSemanticsResult.getSymbolTable().getLocalVariables(name)){
            if(Objects.equals(localVar.getName(), left.get("value"))){
                return "invokevirtual(";
            }
        }
        for(Symbol parameter : jmmSemanticsResult.getSymbolTable().getParameters(name)){
            if(Objects.equals(parameter.getName(), left.get("value"))){
                return "invokevirtual(";
            }
        }

        return "invokestatic(";
    }

    public void checkVoidMethod(JmmNode instance){
        String name = Objects.equals(instance.getKind(), "InstanceDeclaration") ? instance.get("instance") : "main";

        if(Objects.equals(jmmSemanticsResult.getSymbolTable().getReturnType(name).getName(), "void"))
            ollirCode.append("ret.V;\n");
    }

    public boolean isField(JmmNode node){
        String var = Objects.equals(node.getKind(), "Assignment") ? node.get("var") : node.get("value");
        for(Symbol field : jmmSemanticsResult.getSymbolTable().getFields()){
            System.out.print("field is ");
            System.out.println(field.getName());
            if(Objects.equals(field.getName(), var)){
                return true;
            }
        }
        return false;
    }

    public int addGetField(JmmNode node, String s){
        String value = node.get("value");
        int tempNumber = this.getTempNumber();
        ollirCode.append("temp_").append(tempNumber).append(s).append(" :=").append(s).append(" getfield(this, ").append(value).append(s).append(")").append(s);
        ollirCode.append(";\n");
        return tempNumber;
    }


}
