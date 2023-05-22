package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;

public class OptimizationAnalyser extends PostorderJmmVisitor<MySymbolTable, String> {

    public List<Report> globalReports = new ArrayList<>();

    public HashMap<String, String> variableHashmap = new HashMap<>();

    final List<String> _PERMITTED_TYPES = List.of(new String[]{"int", "boolean", "void", "String[]", "int[]", "String"});
    final List<String> _BOOLEAN_OPERATORS = List.of(new String[]{"&&", "||", ">", "<", ">=", "<=", "==", "!="});

    //receber a symbolTable
    //reports (depois têm de ser concatenados)
    @Override
    protected void buildVisitor() {
        addVisit("Identifier", this::checkDeclaration);
        addVisit("BinaryOp", this::checkBinaryOpOptimization);
        addVisit("CondicionalStmt", this::checkConditionalStatement);
        addVisit("LoopStmt", this::checkLoopStatement);
        addVisit("Integer", this::checkInteger);
        addVisit("Assignment", this::checkAssignment);
        addVisit("ArrayAssignment", this::checkAssignment);

        this.setDefaultVisit(this::defaultVisitor);
    }


    private String defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return "";
    }

    private boolean checkIfIsConstant(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkIfIsConstant");

        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        System.out.println("instanceDeclaration: " + instanceDeclaration);

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }


        List<Symbol> tipo = mySymbolTable.getLocalVariables(methodNode);


        String var = jmmNode.get("var");

        //Se for um type que não é pârametro
        /*for (int i = 0; i < tipo.size(); i++) {
            System.out.println("getLocalVariables");
            System.out.println(tipo.get(i));
            //significa que a variável é
            if(Objects.equals(tipo.get(i).getName(), var)){
                jmmNode.put("type", tipo.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(tipo.get(i).getType().isArray()));

                return false;
            }
        }*/

        //Se fôr parâmetro

        List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

        //Se for um type que não é pârametro
        for (int i = 0; i < parameters.size(); i++) {
            System.out.println("getParameters");
            System.out.println(parameters.get(i));
            //significa que o variável é um parâmetro
            if (Objects.equals(parameters.get(i).getName(), var)) {

                return false;
            }
        }

        List<Symbol> fields = mySymbolTable.getFields();


        //Se for uma variável da classe/ ou seja, um field
        for (int i = 0; i < fields.size(); i++) {
            System.out.println("getFields");
            System.out.println(fields.get(i));
            if (Objects.equals(fields.get(i).getName(), var)) {
                System.out.println("Objects.equals(fields.get(i).getName(), var)");
                jmmNode.put("type", fields.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(fields.get(i).getType().isArray()));

                if (methodNode.equals("main")) {
                    //trata do caso mesmo que seja static
                    //globalReports.add(Reports.reportCheckDeclaration(jmmNode));
                }
                return false;
            }
        }

        return true;
    }

    private String checkDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkDeclaration ");
        System.out.println(jmmNode.getAttributes());
        System.out.println("node: " + jmmNode);
        List<JmmNode> children = jmmNode.getChildren();

        System.out.println("children: " + children);

        Optional<JmmNode> loopAncestor = jmmNode.getAncestor("LoopStmt");
        Optional<JmmNode> condicionalAncestor = jmmNode.getAncestor("CondicionalStmt");
        if (loopAncestor.isPresent()) {
            System.out.println("ancestors na checkDeclaration: " + loopAncestor);
        }
        if (condicionalAncestor.isPresent()) {
            System.out.println("condicionalAncestors na checkDeclaration: " + condicionalAncestor);
        }


        if (variableHashmap.containsKey(jmmNode.get("value")) && loopAncestor.isEmpty() && condicionalAncestor.isEmpty() && variableHashmap.get(jmmNode.get("value")) != null) {
            System.out.println("vai ser criado um novo node");
            System.out.println("variableHasmap: " + variableHashmap);
            JmmNode substituteNode = new JmmNodeImpl("Integer");
            substituteNode.put("value", variableHashmap.get(jmmNode.get("value")));
            System.out.println("substituteNode: " + substituteNode);
            jmmNode.replace(substituteNode);
        }

        return "";

    }


    private String checkBinaryOpOptimization(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        System.out.println("checkBinaryOp");

        System.out.println("node: " + jmmNode);

        Optional<JmmNode> loopAncestor = jmmNode.getAncestor("LoopStmt");
        Optional<JmmNode> condicionalAncestor = jmmNode.getAncestor("CondicionalStmt");

        if (loopAncestor.isPresent()) {
            System.out.println("ancestors na BinaryOp: " + loopAncestor);
        }

        if (condicionalAncestor.isPresent()) {
            System.out.println("condicionalAncestor na BinaryOp: " + condicionalAncestor);
        }

        return "";
    }

    private String checkConditionalStatement(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        System.out.println("checkConditionalStatement");
        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());


        return "";

    }

    private String checkLoopStatement(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkLoopStatement");

        System.out.println("hashmap antes da substituição: " + variableHashmap);

        /*if (variableHashmap.containsKey(jmmNode.get("var"))) {
            variableHashmap.replace(jmmNode.get("var"), null);
            System.out.println("hashmap depois da substituição: " + variableHashmap);
            return "";
        } else {
            return "";
        }*/

        return "";

    }

    private String checkWithStmt(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkWithStmt");


        return "";
    }


    private String checkInteger(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkInteger");

        System.out.println("node: " + jmmNode);
        System.out.println("attributes: " + jmmNode.getAttributes());
        System.out.println("children: " + jmmNode.getChildren());

        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");


        System.out.println("attributes after put: " + jmmNode.getAttributes());

        return "";

    }


    private String checkAssignment(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkAssignment");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());
        System.out.println("var: " + jmmNode.get("var"));
        System.out.println("children: " + jmmNode.getChildren());
        Optional<JmmNode> loopAncestor = jmmNode.getAncestor("LoopStmt");
        Optional<JmmNode> condicionalAncestor = jmmNode.getAncestor("CondicionalStmt");
        if (loopAncestor.isPresent() || condicionalAncestor.isPresent()) {
            System.out.println("loopAncestors no checkAssignment: " + jmmNode.getAncestor("LoopStmt"));
            System.out.println("condicionalAncestor no checkAssignment: + " + jmmNode);
            System.out.println("Significa que tem um Loop node como pai");
            if (variableHashmap.containsKey(jmmNode.get("var"))) {
                variableHashmap.replace(jmmNode.get("var"), null);
                System.out.println("hashmap depois da substituição: " + variableHashmap);
                System.out.println("variável foi posta com o valor null");
            } else {
                System.out.println("variável não foi acrescentada");
            }
            System.out.println("hashmap depois das transformações por ter estado num loop: " + variableHashmap);
            return "";
        }

        if (variableHashmap.containsKey(jmmNode.get("var")) && variableHashmap.get(jmmNode.get("var")) != null) {
            System.out.println("Substituição do valor da variável no hashmap");
            variableHashmap.replace(jmmNode.get("var"), jmmNode.getChildren().get(0).get("value"));
            return "";
        } else if (jmmNode.getChildren().get(0).hasAttribute("value")) {
            System.out.println("pôr um novo valor no hashmap");
            variableHashmap.put(jmmNode.get("var"), jmmNode.getChildren().get(0).get("value"));
            return "";
        }

        return "";
    }

    private String checkArrayDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkArrayDeclaration");

        System.out.println("node: " + jmmNode);
        System.out.println("node attributes: " + jmmNode.getAttributes());

        System.out.println("children: " + jmmNode.getChildren());
        System.out.println("child 0 attributes: " + jmmNode.getChildren().get(0).getAttributes());

        jmmNode.put("isArray", "true");
        jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));

        System.out.println("isArray: " + jmmNode.get("isArray"));
        System.out.println("after, node attributes: " + jmmNode.getAttributes());

        return "";
    }

    private String checkReservedExpr(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        System.out.println("checkReservedExpr");


        return "";
    }


    public static boolean isStringOfIntegers(String str) {
        return str.matches("\\d+"); //matches one or more digits
    }
}
