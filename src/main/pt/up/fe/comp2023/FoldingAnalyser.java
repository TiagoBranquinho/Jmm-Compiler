package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;

public class FoldingAnalyser extends PostorderJmmVisitor<MySymbolTable, String> {

    public Boolean foldHasChanged = false;


    @Override
    protected void buildVisitor() {

        addVisit("BinaryOp", this::checkBinaryOpOptimization);

        this.setDefaultVisit(this::defaultVisitor);
    }


    private String defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return "";
    }


    private String checkBinaryOpOptimization(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        //se um dos filhos não for um integer, então não vou poder ter um value no binaryOp node
        for (int i = 0; i < jmmNode.getChildren().size(); i++) {
            if (!jmmNode.getChildren().get(i).getKind().equals("Integer")) {
                return "";
            }
        }


        String op = jmmNode.get("op");
        Integer result;
        //Boolean temp = false;
        if (jmmNode.getChildren().size() == 2) {
            Integer firstChild = Integer.valueOf(jmmNode.getChildren().get(0).get("value"));
            Integer secondChild = Integer.valueOf(jmmNode.getChildren().get(1).get("value"));


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

            if (result != null) {
                JmmNode substituteNode = new JmmNodeImpl("Integer");
                substituteNode.put("value", String.valueOf(result));
                jmmNode.replace(substituteNode);
                foldHasChanged = true;
            }
        }

        return "";
    }


}
