package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;
public class Analyser extends PostorderJmmVisitor<MySymbolTable, List<Report>> {

    private List<Report> visitDefault(JmmNode jmmNode, SymbolTable symbolTable){
        System.out.println(jmmNode.getKind());
    }

    @Override
    protected void buildVisitor() {

    }
}
