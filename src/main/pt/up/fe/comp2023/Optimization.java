package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class Optimization implements JmmOptimization {
    private String ollirCode = "";

    private JmmSemanticsResult jmmSemanticsResult;
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        this.jmmSemanticsResult = jmmSemanticsResult;
        this.addImports();
        this.addClass();
        this.addMethod();
        return new OllirResult(ollirCode, jmmSemanticsResult.getConfig());
    }

    private void addImports(){
        for(String importString : jmmSemanticsResult.getSymbolTable().getImports()){
            ollirCode += "import " + importString + ";\n";
        }
    }

    private void addClass(){
        String className = jmmSemanticsResult.getSymbolTable().getClassName();
        ollirCode += className + " {\n" +
        ".construct " + className + "().V {\n" +
        "invokespecial(this, \"<init>\").V;";
    }

    private void addMethod(){
        String className = jmmSemanticsResult.getSymbolTable().getClassName();
        ollirCode += className + " {\n" +
                ".construct " + className + "().V {\n" +
                "invokespecial(this, \"<init>\").V;";
    }
}
