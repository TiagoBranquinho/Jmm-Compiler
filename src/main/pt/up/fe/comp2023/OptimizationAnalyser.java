package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OptimizationAnalyser extends PostorderJmmVisitor<MySymbolTable, String> {


    public Boolean propHasChanged = false;

    public HashMap<String, String> variableHashmap = new HashMap<>();


    @Override
    protected void buildVisitor() {

        addVisit("Identifier", this::checkDeclaration);
        addVisit("BinaryOp", this::checkBinaryOpOptimization);
        addVisit("Integer", this::checkInteger);
        addVisit("Assignment", this::checkAssignment);
        addVisit("ArrayAssignment", this::checkAssignment);
        addVisit("MethodDeclaration", this::checkMethodDeclaration);


        this.setDefaultVisit(this::defaultVisitor);
    }


    private String defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return "";
    }


    private String checkMethodDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        variableHashmap.clear();

        return "";
    }

    private String checkDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        List<JmmNode> children = jmmNode.getChildren();


        boolean isParameter = false;
        boolean isField = false;

        Optional<JmmNode> loopAncestor = jmmNode.getAncestor("LoopStmt");
        Optional<JmmNode> condicionalAncestor = jmmNode.getAncestor("CondicionalStmt");
        Optional<JmmNode> subscriptAncestor = jmmNode.getAncestor("SubscriptOp");


        //Se o pai for um condicional statement, significa que este node é o node da condição, no qual deve ser feito constant propagation
        JmmNode parent = jmmNode.getJmmParent().getJmmParent();

        if (jmmNode.getJmmParent().getKind().equals("CondicionalStmt")) {
            //se o node for logo a condição
            parent = jmmNode.getJmmParent();
        }

        if (subscriptAncestor.isPresent() && jmmNode.getJmmParent().getChildren().get(0) == jmmNode) {
            //se este ifentifier for o "a" na expressão a[b], ou seja, o primeiro child, não pode ser substituído porque é um array
            return "";
        }


        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");


        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        //Se fôr parâmetro

        List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

        //Se for um type que não é pârametro
        for (int i = 0; i < parameters.size(); i++) {
            if (Objects.equals(parameters.get(i).getName(), jmmNode.get("value"))) {
                isParameter = true;
            }
        }

        List<Symbol> fields = mySymbolTable.getFields();

        //Se for uma variável da classe
        for (int i = 0; i < fields.size(); i++) {
            System.out.println("getFields");
            System.out.println(fields.get(i));
            if (Objects.equals(fields.get(i).getName(), jmmNode.get("value"))) {
                isField = true;

            }
        }

        if (variableHashmap.containsKey(jmmNode.get("value")) && !isField && !isParameter && (loopAncestor.isEmpty() /*|| parent.getKind().equals("LoopStmt")*/) && (condicionalAncestor.isEmpty() || parent.getKind().equals("CondicionalStmt")) && variableHashmap.get(jmmNode.get("value")) != null) {

            JmmNode substituteNode = new JmmNodeImpl("Integer");
            substituteNode.put("value", variableHashmap.get(jmmNode.get("value")));
            jmmNode.replace(substituteNode);
            propHasChanged = true;
        }

        return "";

    }


    private String checkBinaryOpOptimization(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        //se um dos filhos não for um integer, então não vou poder ter um value no binaryOp node e vai ser posto a none,
        // para depois ser verificado
        for (int i = 0; i < jmmNode.getChildren().size(); i++) {
            if (jmmNode.getChildren().get(i).getKind().equals("Integer")) {
                jmmNode.put("value", "none");
                break;
            }
        }

        return "";
    }


    private String checkInteger(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");

        return "";

    }


    private String checkAssignment(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        Optional<JmmNode> loopAncestor = jmmNode.getAncestor("LoopStmt");
        Optional<JmmNode> condicionalAncestor = jmmNode.getAncestor("CondicionalStmt");
        String valor;
        if (jmmNode.getChildren().get(0).hasAttribute("value")) {
            valor = jmmNode.getChildren().get(0).get("value");
        } else {
            valor = "";
        }
        if (loopAncestor.isPresent() || condicionalAncestor.isPresent() || valor.equals("none")) {


            if (variableHashmap.containsKey(jmmNode.get("var"))) {
                variableHashmap.replace(jmmNode.get("var"), null);
            }
            return "";
        }

        if (variableHashmap.containsKey(jmmNode.get("var")) && variableHashmap.get(jmmNode.get("var")) != null) {
            variableHashmap.replace(jmmNode.get("var"), jmmNode.getChildren().get(0).get("value"));
            return "";
        } else if (jmmNode.getChildren().get(0).hasAttribute("value") && jmmNode.getChildren().get(0).getKind().equals("Integer")) {
            variableHashmap.put(jmmNode.get("var"), jmmNode.getChildren().get(0).get("value"));
            return "";
        }

        return "";
    }

    
}
