package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.Objects;

public class OllirGenerator extends AJmmVisitor <String , String > {

    private Optimization optimization;

    public OllirGenerator(Optimization optimization) {
        this.optimization = optimization;
    }


    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgram);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("FieldDeclaration", this::dealWithFieldDeclaration);
        addVisit("MethodDeclaration", this::dealWithMethodDeclaration);
        addVisit("InstanceDeclaration", this::dealWithInstanceDeclaration);
        addVisit("MainDeclaration", this::dealWithInstanceDeclaration);
        addVisit("VarDeclarationStmt", this::dealWithVarDeclarationStmt);
        addVisit("Identifier", this::dealWithLiteral);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ExprStmt", this::dealWithExprStmt);
        addVisit("Type", this::dealWithType);
        addVisit("CondicionalStmt", this::dealWithConditionalStatement);
        addVisit("LoopStmt", this::dealWithLoopStatement);
        addVisit("Integer", this::dealWithInteger);
        addVisit("ReservedExpr", this::dealWithReservedExpr);
        addVisit("Stmt", this::dealWithStmt);
        addVisit("ReturnStmt", this::dealWithReturnStmt);
        addVisit("ReturnType", this::dealWithReturnType);
        addVisit("ParameterType", this::dealWithParameterType);
        addVisit("Assignment", this::dealWithAssignment);
        addVisit("DotOp", this::dealWithDotOp);
        addVisit("AccessModifier", this::dealWithAccessModifier);
        addVisit("ObjectDeclaration", this::dealWithObjectDeclaration);
        addVisit("ArrayDeclaration", this::dealWithArrayDeclaration);
        addVisit("ArrayAssignment", this::dealWithArrayAssignment);
        addVisit("SubscriptOp", this::dealWithSubscriptOp);
        addVisit("LengthOp", this::dealWithLengthOp);

        this.setDefaultVisit(this::defaultVisitor);


    }


    private String defaultVisitor(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithProgram(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        optimization.appendToOllir("\n\n}");
        return "";
    }

    private String dealWithClassDeclaration(JmmNode jmmNode, String s) {
        optimization.addClass();
        for(JmmNode child : jmmNode.getChildren()){
            if(Objects.equals(child.getKind(), "FieldDeclaration")){
                optimization.addField(child);
            }
        }
        optimization.addConstructor();
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";

    }

    private String dealWithImportDeclaration(JmmNode jmmNode, String s) {
        this.optimization.addImport(jmmNode);
        return "";
    }

    private String dealWithFieldDeclaration(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithLiteral(JmmNode jmmNode, String s) {
        JmmNode parent = jmmNode.getJmmParent();
        while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
            parent = parent.getJmmParent();
        }
        JmmNode instance = parent.getJmmChild(0);
        if(optimization.isField(jmmNode, instance)){
            s = optimization.getVarOrType(jmmNode, instance, "type");
            int number = optimization.addGetField(jmmNode, s);
            return "temp_" + number + s;
        }
        else{
            return optimization.getVarOrType(jmmNode, instance, "var");
        }
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {

        s = ".i32";



        StringBuilder ret = new StringBuilder();
        StringBuilder code = new StringBuilder();
        for (JmmNode node : jmmNode.getChildren()){
            if(Objects.equals(node.getKind(), "BinaryOp")){
                String retString = visit(node, s);
                int tempNumber = optimization.getTempNumber();
                code.append("temp_").append(tempNumber).append(s).append(" :=").append(s).append(" ").append(retString);
                code.append(";\n");
                optimization.appendToOllir(code.toString());
                ret.append("temp_").append(tempNumber).append(s).append(" ").append(jmmNode.get("op")).append(s).append(" ");
            }
            else{
                ret.append(visit(node, s)).append(" ").append(jmmNode.get("op")).append(s).append(" ");

            }
        }
        ret.delete(ret.length() - jmmNode.get("op").length() - s.length() - 2, ret.length());
        if(Objects.equals(jmmNode.getJmmParent().getKind(), "DotOp")){
            int tempNumber = optimization.getTempNumber();
            StringBuilder temp = new StringBuilder();
            temp.append("temp_").append(tempNumber).append(s).append(" :=").append(s).append(" ").append(ret);
            temp.append(";\n");
            optimization.appendToOllir(temp.toString());
            StringBuilder newRet = new StringBuilder();
            newRet.append("temp_").append(tempNumber).append(s);
            return newRet.toString();
        }
        else
            return ret.toString();

    }

    private String dealWithExprStmt(JmmNode jmmNode, String s) {
        StringBuilder ret = new StringBuilder();
        for (JmmNode node : jmmNode.getChildren()){
            ret.append(visit(node, ".V"));
        }
        this.optimization.appendToOllir(ret + ";\n");
        return "";
    }

    private String dealWithMethodDeclaration(JmmNode jmmNode, String s) {
        optimization.addMethod(jmmNode);
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        optimization.checkVoidMethod(jmmNode.getJmmChild(0));
        optimization.appendToOllir("}\n\n");
        return "";

    }

    private String dealWithType(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithParameterType(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithVarDeclarationStmt(JmmNode jmmNode, String s) {
        return "";

    }

    private String dealWithInstanceDeclaration(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithConditionalStatement(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithInteger(JmmNode jmmNode, String s){
        if(Objects.equals(jmmNode.getJmmParent().getKind(), "ArrayDeclaration") || Objects.equals(jmmNode.getJmmParent().getKind(), "SubscriptOp")){
            StringBuilder code = new StringBuilder();
            System.out.println("yes");
            int tempNumber = optimization.getTempNumber();
            code.append("temp_").append(tempNumber).append(".i32").append(" :=").append(".i32").append(" ").append(jmmNode.get("value")).append(".i32");
            code.append(";\n");
            optimization.appendToOllir(code.toString());
            return "temp_" + tempNumber + ".i32";
        }
        else if(Objects.equals(jmmNode.getJmmParent().getKind(), "ArrayAssignment") && jmmNode.getIndexOfSelf() == 0){
            StringBuilder code = new StringBuilder();
            System.out.println("no");
            int tempNumber = optimization.getTempNumber();
            code.append("temp_").append(tempNumber).append(".i32").append(" :=").append(".i32").append(" ").append(jmmNode.get("value")).append(".i32");
            code.append(";\n");
            optimization.appendToOllir(code.toString());
            return "temp_" + tempNumber + ".i32";
        }
        else{
            JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            return this.optimization.getVarOrType(jmmNode, instance, "var");
        }

    }

    private String dealWithLoopStatement(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReservedExpr(JmmNode jmmNode, String s){
        String value = jmmNode.get("value");
        if(Objects.equals(value, "true") || Objects.equals(value, "false")){
            return value + ".bool";
        }
        return jmmNode.get("value");
    }

    private String dealWithStmt(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReturnStmt(JmmNode jmmNode, String s){
        StringBuilder ret = new StringBuilder();
        StringBuilder add = new StringBuilder();
        JmmNode parent = jmmNode.getJmmParent();
        while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
            parent = parent.getJmmParent();
        }
        JmmNode instance = parent.getJmmChild(0);

        String retType = optimization.getMethodRetType(instance);
        ret.append("ret").append(retType).append(" ");
        for (JmmNode node : jmmNode.getChildren()){
            if(Objects.equals(node.getKind(), "DotOp") || Objects.equals(node.getKind(), "BinaryOp")){
                String visit = visit(node, retType);
                int number = optimization.getTempNumber();
                add.append("temp_").append(number).append(retType).append(" :=").append(retType).append(" ").append(visit).append(";\n");
                optimization.appendToOllir(add.toString());
                ret.append("temp_").append(number).append(retType);
            }
            else{
                ret.append(visit(node, retType));
            }
        }
        this.optimization.appendToOllir(ret + ";\n");
        return "";
    }

    private String dealWithReturnType(JmmNode jmmNode, String s){
        return "";
    }


    private String dealWithAssignment(JmmNode jmmNode, String s){
        StringBuilder ret = new StringBuilder();
        JmmNode parent = jmmNode.getJmmParent();
        while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
            parent = parent.getJmmParent();
        }
        JmmNode instance = parent.getJmmChild(0);
        String var = optimization.getVarOrType(jmmNode, instance, "var");
        String type = optimization.getVarOrType(jmmNode, instance, "type");
        for (JmmNode node : jmmNode.getChildren()){
            if(optimization.isField(jmmNode, instance)){
                ret.append("putfield(this, ").append(var);
                ret.append(", ");
                ret.append(visit(node, type));
                ret.append(").V");
            }
            else{

                ret.append(var).append(" :=").append(type).append(" ");
                ret.append(visit(node, type));
            }
        }
        this.optimization.appendToOllir(ret + ";\n");
        return "";
    }

    private String dealWithDotOp(JmmNode jmmNode, String s){
        StringBuilder code = new StringBuilder();
        JmmNode parent = jmmNode.getJmmParent();
        StringBuilder ret = new StringBuilder();
        String oldS = s;
        while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
            parent = parent.getJmmParent();
        }
        JmmNode instance = parent.getJmmChild(0);

        StringBuilder invoke = new StringBuilder();
        String invokeType = optimization.getInvoke(jmmNode, instance);
        invoke.append(invokeType).append("(");
        List<JmmNode> children = jmmNode.getChildren();

        if(Objects.equals(invokeType, "invokevirtual") && Objects.equals(s, ".V")){
            s = optimization.getDotOpType(jmmNode, instance);
        }
        invoke.append(visit(children.get(0), s));

        children.remove(0);
        invoke.append(", \"").append(jmmNode.get("method")).append("\"");
        for(JmmNode node : children){
            invoke.append(", ").append(visit(node, s));
        }
        int tempNumber = optimization.getTempNumber();

        if(!Objects.equals(s, ".V")) {
            code.append("temp_").append(tempNumber).append(s).append(" :=").append(s).append(" ");
        }
        code.append(invoke);
        code.append(")").append(s);

        if(!Objects.equals(oldS, ".V")){
            code.append(";\n");
            ret.append("temp_").append(tempNumber).append(s);
        }
        else{
            optimization.decreaseTempNumber();
        }
        optimization.appendToOllir(code.toString());
        return ret.toString();
    }

    private String dealWithAccessModifier(JmmNode jmmNode, String s){
        return "";
    }

    private String dealWithObjectDeclaration(JmmNode jmmNode, String s){
        JmmNode assignment = jmmNode.getJmmParent();
        while (!Objects.equals(assignment.getKind(), "Assignment")){
            assignment = assignment.getJmmParent();
        }
        JmmNode instance = jmmNode.getJmmParent();
        while (!Objects.equals(instance.getKind(), "MethodDeclaration")){
            instance = instance.getJmmParent();
        }
        return this.optimization.initObjectDeclaration(jmmNode, assignment, instance);

    }

    private String dealWithArrayDeclaration(JmmNode jmmNode, String s){
        StringBuilder ret = new StringBuilder();
        ret.append("new(array, ").append(visit(jmmNode.getJmmChild(0), s)).append(")").append(s);
        return ret.toString();

    }

    private String dealWithArrayAssignment(JmmNode jmmNode, String s){
        String index, value;
        JmmNode instance = jmmNode.getJmmParent();
        while (!Objects.equals(instance.getKind(), "MethodDeclaration")){
            instance = instance.getJmmParent();
        }
        s = optimization.getVarOrType(jmmNode, instance, "type");
        s = optimization.getSubstringAfterSecondDot(s);
        index = visit(jmmNode.getJmmChild(0), s);
        value = visit(jmmNode.getJmmChild(1), s);
        StringBuilder ret = new StringBuilder();
        ret.append(jmmNode.get("var")).append("[").append(index).append("]").append(s).append(" :=").append(s).append(" ").append(value);
        this.optimization.appendToOllir(ret + ";\n");
        return "";
    }

    private String dealWithSubscriptOp(JmmNode jmmNode, String s){
        StringBuilder ret = new StringBuilder();
        StringBuilder code = new StringBuilder();
        String index, value, temp;
        index = visit(jmmNode.getJmmChild(1), s);
        int tempNumber = optimization.getTempNumber();
        JmmNode instance = jmmNode.getJmmParent();
        while (!Objects.equals(instance.getKind(), "MethodDeclaration")){
            instance = instance.getJmmParent();
        }
        s = optimization.getVarOrType(jmmNode.getJmmChild(0), instance, "type");
        s = optimization.getSubstringAfterSecondDot(s);
        code.append("temp_").append(tempNumber).append(s).append(" :=").append(s).append(" ").append(jmmNode.getJmmChild(0).get("value")).append("[").append(index).append("]").append(s);
        optimization.appendToOllir(code + ";\n");
        ret.append("temp_").append(tempNumber).append(s);
        return ret.toString();
    }

    private String dealWithLengthOp(JmmNode jmmNode, String s){
        StringBuilder ret = new StringBuilder();
        StringBuilder code = new StringBuilder();
        String index, value, temp;
        int tempNumber = optimization.getTempNumber();
        JmmNode instance = jmmNode.getJmmParent();
        while (!Objects.equals(instance.getKind(), "MethodDeclaration")){
            instance = instance.getJmmParent();
        }
        s = optimization.getVarOrType(jmmNode.getJmmChild(0), instance, "var");
        code.append("temp_").append(tempNumber).append(".i32 :=.i32 arraylength(").append(s).append(").i32");
        optimization.appendToOllir(code + ";\n");
        ret.append("temp_").append(tempNumber).append(".i32");
        return ret.toString();
    }


}