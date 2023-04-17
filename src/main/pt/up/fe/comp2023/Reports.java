package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.report.ReportType;

public class Reports {
    public static Report reportCheckType(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There has been an error concerning checkType");
    }

    public static Report reportCheckBinaryOp(JmmNode jmmNode, String specific){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There has been an error concerning checkBinaryOp in " + specific);
    }

    public static Report reportcheckConditionalStatement(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There has been an error concerning checkConditionalStatement, the opartion is not boolean");
    }

    public static Report reportcheckAssignment(JmmNode jmmNode){
        return new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "There has been an error concerning checkAssignement");
    }
}

//    public Report(pt.up.fe.comp.jmm.report.ReportType type, pt.up.fe.comp.jmm.report.Stage stage, int line, java.lang.String message) { /* compiled code */ }