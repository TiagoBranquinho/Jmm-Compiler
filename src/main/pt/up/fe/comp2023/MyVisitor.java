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
    public MyVisitor(Optimization optimization) {
        this.optimization = optimization;
        a = 1;
    }
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
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
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
        return "";
    }

    private String dealWithDotOpSymbolTable(JmmNode jmmNode, String s){
        return "";
    }

    private String dealWithAccessModifier(JmmNode jmmNode, String s){
        return "";
    }

    //---------------------------------------


    private String dealWithProgramOllir(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithClassDeclarationOllir(JmmNode jmmNode, String s) {
        optimization.addClass();
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithImportDeclarationOllir(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithFieldDeclarationOllir(JmmNode jmmNode, String s) {
        //symbolTable.addField(jmmNode);
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithLiteralOllir(JmmNode jmmNode, String s) {
        return jmmNode.get("value");
    }

    private String dealWithBinaryOpOllir(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithExprStmtOllir(JmmNode jmmNode, String s) {
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithMethodDeclarationOllir(JmmNode jmmNode, String s) {
        optimization.addMethod(jmmNode);
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithTypeOllir(JmmNode jmmNode, String s) {
        return "";
    }

    private String dealWithParameterTypeOllir(JmmNode jmmNode, String s) {
        JmmNode parent = jmmNode.getJmmParent();
        if(Objects.equals(parent.getKind(), "InstanceDeclaration")){
            symbolTable.addLocalArg(parent.get("instance"), jmmNode);
        }
        else if(Objects.equals(parent.getKind(), "MainDeclaratiom")){
            symbolTable.addLocalArg("main", jmmNode);
        }
        return "";
    }

    private String dealWithVarDeclarationStmtOllir(JmmNode jmmNode, String s) {
        /*JmmNode parent = jmmNode.getJmmParent();
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
        */return "";
    }

    private String dealWithInstanceDeclarationOllir(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithConditionalStatementOllir(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithIntegerOllir(JmmNode jmmNode, String s){
        return "";
    }

    private String dealWithLoopStatementOllir(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReservedExprOllir(JmmNode jmmNode, String s){return "";}

    private String dealWithStmtOllir(JmmNode jmmNode, String s){
        for (JmmNode node : jmmNode.getChildren()){
            visit(node);
        }
        return "";
    }

    private String dealWithReturnStmtOllir(JmmNode jmmNode, String s){
        return "";
    }

    private String dealWithReturnTypeOllir(JmmNode jmmNode, String s){
        /*JmmNode parent = jmmNode.getJmmParent();
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

        */return "";
    }

    private String dealWithAssignmentOllir(JmmNode jmmNode, String s){
        return "";
    }

    private String dealWithDotOpOllir(JmmNode jmmNode, String s){
        return "";
    }

}