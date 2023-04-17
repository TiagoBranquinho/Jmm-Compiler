package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pt.up.fe.comp2023.Reports;

public class Analyser extends PostorderJmmVisitor<MySymbolTable, List<Report>> {


    final List<String> _PERMITTED_TYPES = List.of(new String[]{"int", "boolean", "void"});
    //receber a symbolTable
    //reports (depois têm de ser concatenados)
    @Override
    protected void buildVisitor() {
        /*addVisit("Program", this::);
        addVisit("ClassDeclaration", this::);
        addVisit("ImportDeclaration", this::);
        addVisit("FieldDeclaration", this::);
        addVisit("MethodDeclaration", this::);
        addVisit("InstanceDeclaration", this::);*/
        /*addVisit("MainDeclaration", this::dealWithInstanceDeclaration);*/
        //addVisit("VarDeclarationStmt", this::checkVarDeclarationStmt);
        //addVisit("Identifier", this::checkDeclaration);
        addVisit("BinaryOp", this::checkBinaryOp);
        /*addVisit("ExprStmt", this::dealWithExprStmt);*/
        addVisit("Type", this::checkType);
        addVisit("CondicionalStmt", this::checkConditionalStatement);
        //addVisit("LoopStmt", this::dealWithLoopStatement);
        //addVisit("Integer", this::dealWithInteger); não é preciso ir verificar
        addVisit("ReservedExpr", this::checkReservedExpr);
        /*addVisit("Stmt", this::dealWithStmt);
        addVisit("ReturnStmt", this::dealWithReturnStmt);
        addVisit("ReturnType", this::dealWithReturnType);
        addVisit("ParameterType", this::dealWithParameterType);*/
        addVisit("Assignment", this::checkAssignment);
        //addVisit("DotOp", this::dealWithDotOp);
        this.setDefaultVisit(this::defaultVisitor);
    }

    private List<Report> defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return new ArrayList<>();
    }


    private List<Report> visitDefault(JmmNode jmmNode, SymbolTable symbolTable){
        System.out.println(jmmNode.getKind());
        return new ArrayList<>();
    }

    private List<Report> checkType(JmmNode jmmNode, MySymbolTable mySymbolTable){

        List<Report> errorReports = new ArrayList<>();
        String value = jmmNode.get("value");

        if (_PERMITTED_TYPES.contains(value)){
            jmmNode.put("type", value);
            return new ArrayList<>();
        }
        // TO DO criar reports em caso de erro

        errorReports.add(Reports.reportCheckType(jmmNode));

        return errorReports;

    }

    private List<Report> checkBinaryOp(JmmNode jmmNode, MySymbolTable mySymbolTable){

        System.out.println("akakak1111");
        List<Report> errorReports = new ArrayList<>();

        System.out.println("akakak2222");
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("akakak3333");
        String op = jmmNode.get("op");

        String childValue = children.get(0).get("value");
        System.out.println("akakak" + children.get(0).get("value"));
        if (children.get(0).get("value") != children.get(1).get("value")){
            //significa que os dois nodes em que se está a fazer a operação são de tipos
            // diferentes, logo não dá
            //fazer um report
            errorReports.add(Reports.reportCheckBinaryOp(jmmNode, "children nodes have diferent types"));
        } else {

            //Se não for um operador que permita a utilização de booleanos
            //e a situação de aceder a elementos de arrays?
            if(op != "&&" || op != "||"){
                if(childValue == "true"
                || childValue == "false"){
                    //dar return a report
                    errorReports.add(Reports.reportCheckBinaryOp(jmmNode, "not boolean operation with boolean type children"));
                }
                else{
                    return errorReports;
                }
                return new ArrayList<>();
            }else if(op == "&&" || op == "||"){
                if(childValue != "true"
                        || childValue != "false"){
                    errorReports.add(Reports.reportCheckBinaryOp(jmmNode, "boolean operation with not boolean type children"));

                }
                else{
                    return errorReports;
                }
            }


            //é necessário isto? As bynaryOps não têm sempre tipo?
            String value = children.get(0).get("value");
            jmmNode.put("type", value);
        }

        return errorReports;
    }

    private List<Report> checkConditionalStatement(JmmNode jmmNode, MySymbolTable mySymbolTable){

        List<Report> errorReports = new ArrayList<>();
        String value = jmmNode.get("value");

        if(value != "boolean"){
            errorReports.add(Reports.reportcheckConditionalStatement(jmmNode));
        }

        return errorReports;
    }

    private List<Report> checkVarDeclarationStatement(JmmNode jmmNode, MySymbolTable mySymbolTable){



        return new ArrayList<>();
    }

    private List<Report> checkAssignment(JmmNode jmmNode, MySymbolTable mySymbolTable){

        List<Report> errorReports = new ArrayList<>();
        List<JmmNode> children = jmmNode.getChildren();


        if (children.get(0).get("value") != "true"
        || children.get(0).get("value") != "false"
        || children.get(0).get("isArray") == "true"){
            //tem de se dar um report porque o if está a ser feito mal
            errorReports.add(Reports.reportcheckAssignment(jmmNode));
        }

        return new ArrayList<>();
    }

    private List<Report> checkReservedExpr(JmmNode jmmNode, MySymbolTable mySymbolTable){

        Optional<JmmNode> parent = jmmNode.getAncestor("mainDeclaration");

        if(parent.isPresent()){
            //dar return do report de erro porque significa que o static tem como pai o mainMethod, o que
            // não pode ser
        }

        return new ArrayList<>();
    }

}
