package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoldingAnalyser extends PostorderJmmVisitor<MySymbolTable, String> {

    public List<Report> globalReports = new ArrayList<>();

    public Boolean foldHasChanged = false;

    public HashMap<String, String> variableHashmap = new HashMap<>();

    final List<String> _PERMITTED_TYPES = List.of(new String[]{"int", "boolean", "void", "String[]", "int[]", "String"});
    final List<String> _BOOLEAN_OPERATORS = List.of(new String[]{"&&", "||", ">", "<", ">=", "<=", "==", "!="});

    //receber a symbolTable
    //reports (depois têm de ser concatenados)
    @Override
    protected void buildVisitor() {

        System.out.println("foldHasChanged inside buildVisitor: " + foldHasChanged);
        addVisit("BinaryOp", this::checkBinaryOpOptimization);
        addVisit("Integer", this::checkInteger);


        this.setDefaultVisit(this::defaultVisitor);
    }


    private String defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return "";
    }


    private String checkBinaryOpOptimization(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        System.out.println("checkBinaryOp no fold");

        System.out.println("node: " + jmmNode);
        System.out.println("children: " + jmmNode.getChildren());


        //se um dos filhos não for um integer, então não vou poder ter um value no binaryOp node
        for (int i = 0; i < jmmNode.getChildren().size(); i++) {
            if (!jmmNode.getChildren().get(i).getKind().equals("Integer")) {
                return "";
            }
        }

        System.out.println("depois do if");

        String op = jmmNode.get("op");
        Integer result;
        //Boolean temp = false;
        if (jmmNode.getChildren().size() == 2) {
            Integer firstChild = Integer.valueOf(jmmNode.getChildren().get(0).get("value"));
            Integer secondChild = Integer.valueOf(jmmNode.getChildren().get(1).get("value"));

            System.out.println("firstChild: " + firstChild);
            System.out.println("secondChild: " + secondChild);

            if (op.equals("+")) {
                result = firstChild + secondChild;
            } else if (op.equals("-")) {
                result = firstChild - secondChild;
            } else if (op.equals("*")) {
                result = firstChild * secondChild;
            } else if (op.equals("/")) {
                result = firstChild / secondChild;
            } else {
                result = null;
            }
            System.out.println("result depois do cálculo: " + result);

            if (result != null) {
                JmmNode substituteNode = new JmmNodeImpl("Integer");
                System.out.println("value no fold: " + String.valueOf(result));
                substituteNode.put("value", String.valueOf(result));
                System.out.println("substituteNode: " + substituteNode);
                jmmNode.replace(substituteNode);
                foldHasChanged = true;
                System.out.println("houve um replace no fold:" + foldHasChanged);
                //temp = true;
            }
        }


        System.out.println("aaaaa: " + foldHasChanged);

        return "";
    }


    private String checkInteger(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkInteger no fold");

        System.out.println("foldHAsChanged aqui no fold: " + foldHasChanged);

        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());


        return "";

    }


}
