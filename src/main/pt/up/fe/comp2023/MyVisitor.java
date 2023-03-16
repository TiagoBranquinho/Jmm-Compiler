package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;

public class MyVisitor extends AJmmVisitor <String , String > {
    private String className;

    private MySymbolTable symbolTable;
    public MyVisitor(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;
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






    }


    private String dealWithProgram(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithClassDeclaration(JmmNode jmmNode, String s) {
        symbolTable.addClass(jmmNode);
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithImportDeclaration(JmmNode jmmNode, String s) {
        symbolTable.addImport(jmmNode);
        return "";
    }

    private String dealWithFieldDeclaration(JmmNode jmmNode, String s) {
        symbolTable.addField(jmmNode);
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithLiteral(JmmNode jmmNode, String s) {
        return jmmNode.get("value");
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithExprStmt(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithMethodDeclaration(JmmNode jmmNode, String s) {
        symbolTable.addMethod(jmmNode);
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithType(JmmNode jmmNode, String s) {
        JmmNode parent = jmmNode.getJmmParent();
        if(Objects.equals(parent.getKind(), "InstanceDeclaration")){
            symbolTable.addLocalArg(parent.get("instance"), jmmNode);
        }
        else if(Objects.equals(parent.getKind(), "MainDeclaratiom")){
            symbolTable.addLocalArg("main", jmmNode);
        }
        return "";
    }

    private String dealWithVarDeclarationStmt(JmmNode jmmNode, String s) {
        JmmNode parent = jmmNode.getJmmParent();
        while (!Objects.equals(parent.getKind(), "MethodDeclaration")){
            parent = parent.getJmmParent();
        }
        JmmNode instance = parent.getJmmChild(0);
        System.out.println(instance);
        if(Objects.equals(instance.getKind(), "InstanceDeclaration")){
            symbolTable.addLocalVar(instance.get("instance"), jmmNode);
        }
        else
            symbolTable.addLocalVar("main", jmmNode);
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
        return "";
    }

    private String dealWithLoopStatement(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReservedExpr(JmmNode jmmNode, String s){return "";}

    private String dealWithStmt(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReturnStmt(JmmNode jmmNode, String s){
        return "";
    }

    private String dealWithReturnType(JmmNode jmmNode, String s){
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
}