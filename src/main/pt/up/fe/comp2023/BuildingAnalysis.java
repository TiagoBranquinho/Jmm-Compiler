/*package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

// extends PostorderJmmVisitor<MySymbolTable, List<Report>> {
public class BuildingAnalysis implements JmmAnalysis{
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult){

        MySymbolTable mySymbolTable = new MySymbolTable(parserResult.getRootNode());

        List<Report> reports = parserResult.getReports();

        List<Report> semanticAnalysis = new Analyser().visit(parserResult.getRootNode(), mySymbolTable);
        System.out.println("============");
        System.out.println(mySymbolTable);
        // juntar os symbolTableReports com os da semanticAnalysis e meter aqui
        return new JmmSemanticsResult(parserResult, mySymbolTable, new ArrayList<>());
    }


}*/
