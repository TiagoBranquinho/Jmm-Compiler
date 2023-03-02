package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class JavaCalcGenerator extends AJmmVisitor <String,String>{
    private String className;
    public JavaCalcGenerator(String className) {
        this.className = className;
    }
    protected void buildVisitor() {
        addVisit ("Program", this :: dealWithProgram);
        addVisit ("Assignment", this :: dealWithAssignment);
        addVisit ("Integer", this :: dealWithLiteral);
        addVisit ("Identifier", this :: dealWithLiteral);
        addVisit ("BinaryOp", this :: dealWithBinaryOp);
        addVisit ("ExprStmt", this :: dealWithExprStmt);
    }

    private String dealWithProgram(JmmNode jmmNode, String s) {
        s = (s!= null ?s:"");
        String ret = s + "public class " + this.className + " {\n";
        String s2 = s + "\t";
        ret += s2 + " public static void main ( String [] args ) {\n";

        for ( JmmNode child : jmmNode.getChildren()){
            ret += visit(child ,s2 + "\t");
            ret += "\n";
        }
        ret += s2 + "}\n";
        ret += s + "}\n";
        return ret;
    }

    private String dealWithAssignment(JmmNode jmmNode , String s) {
        return s + "int " + jmmNode.get("var") + " = " + jmmNode.get("value") + ";";
    }

    private String dealWithLiteral( JmmNode jmmNode , String s) {
        return s + jmmNode.get("value");
    }

    private String dealWithBinaryOp(JmmNode jmmNode, String s) {
        return s + jmmNode.get("op");
    }

    private String dealWithExprStmt(JmmNode jmmNode, String s) {
        s = (s!= null ?s:"");
        String ret = "\t\tSystem.out.println(";
        for(JmmNode child : jmmNode.getChildren()){
            if(child.getChildren().size() == 0){
                ret += visit(child, "");
            }
            else {
                ret += visit(child.getJmmChild(0), "");
                ret += child.get("op").replace("'", "");
                ret += visit(child.getJmmChild(1), "");
            }
        }
        ret += ");";

        return ret;
    }

}
