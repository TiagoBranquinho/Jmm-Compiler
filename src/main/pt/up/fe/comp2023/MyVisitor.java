package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;

public class MyVisitor extends AJmmVisitor <String , String > {
    private String className;

    private MySymbolTable symbolTable;

    private Optimization optimization;

    private int a = 0;
    public MyVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }




    protected void buildVisitor() {
        addVisit("Program", this::dealWithProgramSymbolTable);
        addVisit("ClassDeclaration", this::dealWithClassDeclarationSymbolTable);
        addVisit("ImportDeclaration", this::dealWithImportDeclarationSymbolTable);
        addVisit("FieldDeclaration", this::dealWithFieldDeclarationSymbolTable);
        addVisit("MethodDeclaration", this::dealWithMethodDeclarationSymbolTable);
        addVisit("InstanceDeclaration", this::dealWithInstanceDeclarationSymbolTable);
        addVisit("MainDeclaration", this::dealWithInstanceDeclarationSymbolTable);
        addVisit("VarDeclarationStmt", this::dealWithVarDeclarationStmtSymbolTable);
        addVisit("Identifier", this::dealWithLiteralSymbolTable);
        addVisit("BinaryOp", this::dealWithBinaryOpSymbolTable);
        addVisit("ExprStmt", this::dealWithExprStmtSymbolTable);
        addVisit("Type", this::dealWithTypeSymbolTable);
        addVisit("CondicionalStmt", this::dealWithConditionalStatementSymbolTable);
        addVisit("LoopStmt", this::dealWithLoopStatementSymbolTable);
        addVisit("Integer", this::dealWithIntegerSymbolTable);
        addVisit("ReservedExpr", this::dealWithReservedExprSymbolTable);
        addVisit("Stmt", this::dealWithStmtSymbolTable);
        addVisit("ReturnStmt", this::dealWithReturnStmtSymbolTable);
        addVisit("ReturnType", this::dealWithReturnTypeSymbolTable);
        addVisit("ParameterType", this::dealWithParameterTypeSymbolTable);
        addVisit("Assignment", this::dealWithAssignmentSymbolTable);
        addVisit("DotOp", this::dealWithDotOpSymbolTable);
        addVisit("AccessModifier", this::dealWithAccessModifier);
        addVisit("ObjectDeclaration", this::dealWithObjectDeclaration);
        this.setDefaultVisit(this::defaultVisitor);







    }


    private String defaultVisitor(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithProgramSymbolTable(JmmNode jmmNode, String s) {
        if(this.symbolTable != null){
            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
            return "";
        }
        else{
            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
            optimization.appendToOllir("\n\n}");
        return "";
        }
    }

    private String dealWithClassDeclarationSymbolTable(JmmNode jmmNode, String s) {
        if(this.symbolTable != null){
            symbolTable.addClass(jmmNode);
            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
            return "";
        }
        else{
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

    }

    private String dealWithImportDeclarationSymbolTable(JmmNode jmmNode, String s) {
        if(this.symbolTable != null) symbolTable.addImport(jmmNode);
        else this.optimization.addImport(jmmNode);
        return "";
    }

    private String dealWithFieldDeclarationSymbolTable(JmmNode jmmNode, String s) {
        if(this.symbolTable != null) symbolTable.addField(jmmNode);
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithLiteralSymbolTable(JmmNode jmmNode, String s) {
        return jmmNode.get("value");
    }

    private String dealWithBinaryOpSymbolTable(JmmNode jmmNode, String s) {
        if(this.optimization != null){
            System.out.println("in binary op");
            System.out.println(s);
            StringBuilder ret = new StringBuilder();

            for (JmmNode node : jmmNode.getChildren()){
                ret.append(visit(node, s)).append(" ").append(jmmNode.get("op")).append(s);
            }
            return "";
        }
        else{
            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
            return "";
        }

    }

    private String dealWithExprStmtSymbolTable(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithMethodDeclarationSymbolTable(JmmNode jmmNode, String s) {
        if(this.symbolTable != null){
            symbolTable.addMethod(jmmNode);
            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
            return "";
        }
        else{
            optimization.addMethod(jmmNode);
            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
            optimization.appendToOllir("}\n\n");
            return "";
        }

    }

    private String dealWithTypeSymbolTable(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithParameterTypeSymbolTable(JmmNode jmmNode, String s) {
        if(this.symbolTable != null){
            JmmNode parent = jmmNode.getJmmParent();
            if(Objects.equals(parent.getKind(), "InstanceDeclaration")){
                symbolTable.addLocalArg(parent.get("instance"), jmmNode);
            }
            else if(Objects.equals(parent.getKind(), "MainDeclaration")){
                symbolTable.addLocalArg("main", jmmNode);
            }
            return "";
        }
        else{
            return "";
        }
    }

    private String dealWithVarDeclarationStmtSymbolTable(JmmNode jmmNode, String s) {
        if(this.symbolTable != null){
            JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            if(Objects.equals(instance.getKind(), "InstanceDeclaration")){
                symbolTable.addLocalVar(instance.get("instance"), jmmNode);
            }
            else
                symbolTable.addLocalVar("main", jmmNode);
            return "";
        }
        else{
            /*JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            optimization.addVar(instance, jmmNode);*/
        }
        return "";

    }

    private String dealWithInstanceDeclarationSymbolTable(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithConditionalStatementSymbolTable(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithIntegerSymbolTable(JmmNode jmmNode, String s){
        if(this.optimization != null){
            return this.optimization.intToOllir(jmmNode);
        }
        return "";
    }

    private String dealWithLoopStatementSymbolTable(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReservedExprSymbolTable(JmmNode jmmNode, String s){return "";}

    private String dealWithStmtSymbolTable(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReturnStmtSymbolTable(JmmNode jmmNode, String s){
        if(this.optimization != null){
            JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            optimization.addMethodRetType(instance, jmmNode);
        }
        return "";
    }

    private String dealWithReturnTypeSymbolTable(JmmNode jmmNode, String s){
        if(this.symbolTable != null){
            JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            if(Objects.equals(instance.getKind(), "InstanceDeclaration")){
                symbolTable.getMainClass().getMethodDescriptor().get(instance.get("instance")).setReturnType(jmmNode);
            }
            else{
                symbolTable.getMainClass().getMethodDescriptor().get("main").setReturnType(jmmNode);
            }

            return "";
        }
        else{

        }

        return "";
    }


    private String dealWithAssignmentSymbolTable(JmmNode jmmNode, String s){
        if(this.optimization != null){
            StringBuilder ret = new StringBuilder();
            JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            String var = optimization.getVarOrType(jmmNode, instance, "var");
            String type = optimization.getVarOrType(jmmNode, instance, "type");
            ret.append(var).append(" :=").append(type).append(" ");
            for (JmmNode node : jmmNode.getChildren()){
                ret.append(visit(node, type));
            }
            this.optimization.appendToOllir(ret.toString() + ";\n");
        }
        return "";
    }

    private String dealWithDotOpSymbolTable(JmmNode jmmNode, String type, String s){
        if(this.optimization != null){
            JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            StringBuilder code = new StringBuilder();
            int tempNumber = optimization.getTempNumber();
            code.append("temp_").append(tempNumber).append(type).append(" :=").append(type).append(" ");
            code.append(optimization.getInvoke(jmmNode, instance)).append(type);
            code.append(";\n");
            optimization.appendToOllir(code.toString());

            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
        }
        return "";
    }

    private String dealWithDotOpSymbolTable(JmmNode jmmNode, String s){
        if(this.optimization != null){
            JmmNode parent = jmmNode.getJmmParent();
            while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
                parent = parent.getJmmParent();
            }
            JmmNode instance = parent.getJmmChild(0);
            StringBuilder code = new StringBuilder();
            int tempNumber = optimization.getTempNumber();
            code.append("temp_").append(tempNumber).append(s).append(" :=").append(s).append(" ");
            code.append(optimization.getInvoke(jmmNode, instance)).append(s);
            code.append(";\n");
            optimization.appendToOllir(code.toString());

            for (JmmNode node : jmmNode.getChildren()){
                visit(node);
            }
        }
        return "";
    }

    private String dealWithAccessModifier(JmmNode jmmNode, String s){
        return "";
    }

    private String dealWithObjectDeclaration(JmmNode jmmNode, String s){
        if(this.optimization != null){
            JmmNode assignment = jmmNode.getJmmParent();
            while (!Objects.equals(assignment.getKind(), "Assignment")){
                assignment = assignment.getJmmParent();
            }
            JmmNode instance = jmmNode.getJmmParent();
            while (!Objects.equals(instance.getKind(), "Assignment")){
                instance = instance.getJmmParent();
            }
            this.optimization.initObjectDeclaration(jmmNode, assignment, instance);
        }
        return "";
    }


}