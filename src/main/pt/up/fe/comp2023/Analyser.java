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

    public static List<Report> globalReports = new ArrayList<>();


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
        addVisit("VarDeclarationStmt", this::checkVarDeclarationStmt);
        addVisit("Identifier", this::checkDeclaration);
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
        return globalReports;
    }


    private List<Report> visitDefault(JmmNode jmmNode, SymbolTable symbolTable){
        System.out.println(jmmNode.getKind());
        return new ArrayList<>();
    }

    private List<Report> checkVarDeclarationStmt(JmmNode jmmNode, SymbolTable symbolTable){

        System.out.println("checkVarDeclarationStatement");
        System.out.println(jmmNode.getAttributes());
        System.out.println("node: " + jmmNode);
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("children: " + children);

        //System.out.println("child1: " + children.get(0).getAttributes());
        //System.out.println("child1 type: " + children.get(0).get("type"));
        //System.out.println("child1 value: " + children.get(0).get("value"));
        System.out.println("-.-.-.-.-.-.-.-.-");

        //System.out.println("Value: ");
        //System.out.println(jmmNode.get("value"));

        return globalReports;
    }

    private List<Report> checkDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable){

        System.out.println("checkVarDeclaration ");
        System.out.println(jmmNode.getAttributes());
        System.out.println("node: " + jmmNode);
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("children: " + children);

        System.out.println("child1: " + children.get(0).getAttributes());
        System.out.println("child1 type: " + children.get(0).get("type"));
        System.out.println("child1 value: " + children.get(0).get("value"));
        System.out.println("x.x.x.x.x.x.x.x");

        //eu verifico depois se está na SymbolTable
        //primeiro nas localVariables, depois nos parametros e depois nos fields



        //List<Report> errorReports = new ArrayList<>();

        return globalReports;
    }

    private List<Report> checkType(JmmNode jmmNode, MySymbolTable mySymbolTable){

        //List<Report> errorReports = new ArrayList<>();

        System.out.println("checkType");

        String value = jmmNode.get("value");

        if (_PERMITTED_TYPES.contains(value)){
            jmmNode.put("type", value);

            return globalReports;
        }
        // TO DO criar reports em caso de erro

        globalReports.add(Reports.reportCheckType(jmmNode));

        return globalReports;

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

        System.out.println(children.get(0).get("value"));
        System.out.println(children.get(1).get("value"));
        System.out.println(children.get(0).getAttributes());
        if (children.get(0).get("value") != children.get(1).get("value")){
            //significa que os dois nodes em que se está a fazer a operação são de tipos
            // diferentes, logo não dá
            //fazer um report
            globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "children nodes have diferent types"));

            System.out.println("globalReports: " + globalReports);
            return globalReports;
        } else {

            //Se não for um operador que permita a utilização de booleanos
            //e a situação de aceder a elementos de arrays?
            if(op != "&&" || op != "||"){
                if(childValue == "true"
                || childValue == "false"){
                    //dar return a report
                    globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "not boolean operation with boolean type children"));
                    System.out.println("globalReports 2: " + globalReports);
                    return globalReports;
                }
                else{
                    System.out.println("globalReports 3: " + globalReports);
                    return globalReports;
                }

            }else if(op == "&&" || op == "||"){
                if(childValue != "true"
                        || childValue != "false"){
                    globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "boolean operation with not boolean type children"));
                    System.out.println("globalReports 4: " + globalReports);
                    return globalReports;
                }
                else{

                    System.out.println("globalReports : " + globalReports);
                    return globalReports;
                }
            }


            //é necessário isto? As bynaryOps não têm sempre tipo?
            String value = children.get(0).get("value");
            jmmNode.put("type", value);
        }

        return globalReports;
    }

    private List<Report> checkConditionalStatement(JmmNode jmmNode, MySymbolTable mySymbolTable){

        List<Report> errorReports = new ArrayList<>();
        String value = jmmNode.get("value");

        if(value != "boolean"){
            globalReports.add(Reports.reportcheckConditionalStatement(jmmNode));

            return globalReports;
        }


        return globalReports;
    }

    private List<Report> checkVarDeclarationStatement(JmmNode jmmNode, MySymbolTable mySymbolTable){



        return globalReports;
    }

    private List<Report> checkAssignment(JmmNode jmmNode, MySymbolTable mySymbolTable){

        List<Report> errorReports = new ArrayList<>();
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("checkAssignment");

        System.out.println("children: " + children);

        System.out.println("assignement child 0: " + children.get(0).getAttributes() + "value" +children.get(0).get("value"));
        //System.out.println("assignement child 1: " + children.get(1).getAttributes() + children.get(1).get("value"));

/*
        if (children.get(0).get("value") != "true"
        || children.get(0).get("value") != "false"
        || children.get(0).get("isArray") == "true"){
            //tem de se dar um report porque o if está a ser feito mal
            globalReports.add(Reports.reportcheckAssignment(jmmNode));

            return globalReports;
        }*/


        return globalReports;
    }

    private List<Report> checkReservedExpr(JmmNode jmmNode, MySymbolTable mySymbolTable){

        Optional<JmmNode> parent = jmmNode.getAncestor("mainDeclaration");

        if(parent.isPresent()){
            //dar return do report de erro porque significa que o static tem como pai o mainMethod, o que
            // não pode ser
        }

        return globalReports;
    }

}
