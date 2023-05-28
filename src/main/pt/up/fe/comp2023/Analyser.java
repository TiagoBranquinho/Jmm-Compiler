package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Analyser extends PostorderJmmVisitor<MySymbolTable, List<Report>> {

    public List<Report> globalReports = new ArrayList<>();

    final List<String> _PERMITTED_TYPES = List.of(new String[]{"int", "boolean", "void", "String[]", "int[]", "String"});
    final List<String> _BOOLEAN_OPERATORS = List.of(new String[]{"&&", "||", ">", "<", ">=", "<=", "==", "!="});

    @Override
    protected void buildVisitor() {
        addVisit("FieldDeclaration", this::checkFieldDeclaration);
        addVisit("InstanceDeclaration", this::checkInstanceDeclaration);
        addVisit("VarDeclarationStmt", this::checkVarDeclarationStmt);
        addVisit("Identifier", this::checkDeclaration);
        addVisit("BinaryOp", this::checkBinaryOp);
        addVisit("LengthOp", this::checkLengthOp);
        addVisit("Type", this::checkType);
        addVisit("CondicionalStmt", this::checkConditionalStatement);
        addVisit("LoopStmt", this::checkLoopStatement);
        addVisit("Integer", this::checkInteger);
        addVisit("ReservedExpr", this::checkReservedExpr);
        addVisit("Stmt", this::checkWithStmt);
        addVisit("ReturnStmt", this::checkReturnStmt);
        addVisit("ParameterType", this::checkParameterType);
        addVisit("Assignment", this::checkAssignment);
        addVisit("ArrayAssignment", this::checkAssignment);
        addVisit("ArrayDeclaration", this::checkArrayDeclaration);
        addVisit("SubscriptOp", this::checkSubscriptOp);
        addVisit("ObjectDeclaration", this::checkObjectDeclaration);
        addVisit("DotOp", this::checkDotOp);
        addVisit("PrecedenceOp", this::checkPrecedenceOp);
        this.setDefaultVisit(this::defaultVisitor);
    }

    private List<Report> defaultVisitor(JmmNode jmmNode, MySymbolTable mySymbolTable) {
        return globalReports;
    }


    private List<Report> checkVarDeclarationStmt(JmmNode jmmNode, SymbolTable symbolTable) {


        List<JmmNode> children = jmmNode.getChildren();


        if (!children.isEmpty()) {
            jmmNode.put("type", children.get(0).get("type"));
            jmmNode.put("isArray", children.get(0).get("isArray"));
        }


        return globalReports;
    }

    private List<Report> checkDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        //eu verifico depois se está na SymbolTable
        //primeiro nas localVariables, depois nos parametros e depois nos fields

        //tem de ser ir ver à symbol table qual o valor efetivo da variável

        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }


        if (jmmNode.get("value").equals("this")) {
            jmmNode.put("type", mySymbolTable.getClassName());
            jmmNode.put("isArray", "false");

            if (methodNode.equals("main")) {
                globalReports.add(Reports.reportCheckDeclaration(jmmNode));
            }
            return globalReports;
        }


        List<Symbol> tipo = mySymbolTable.getLocalVariables(methodNode);


        String var = jmmNode.get("value");

        //Se for um type que não é pârametro
        for (int i = 0; i < tipo.size(); i++) {

            if (Objects.equals(tipo.get(i).getName(), var)) {
                jmmNode.put("type", tipo.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(tipo.get(i).getType().isArray()));

                return globalReports;
            }
        }

        //Se fôr parâmetro

        List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

        //Se for um type que não é pârametro
        for (int i = 0; i < parameters.size(); i++) {

            if (Objects.equals(parameters.get(i).getName(), var)) {
                jmmNode.put("type", parameters.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(parameters.get(i).getType().isArray()));

                return globalReports;
            }
        }

        List<Symbol> fields = mySymbolTable.getFields();

        //Se for uma variável da classe
        for (int i = 0; i < fields.size(); i++) {

            if (Objects.equals(fields.get(i).getName(), var)) {
                jmmNode.put("type", fields.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(fields.get(i).getType().isArray()));

                if (methodNode.equals("main")) {
                    //trata do caso mesmo que seja static
                    globalReports.add(Reports.reportCheckDeclaration(jmmNode));
                }
                return globalReports;
            }
        }

        List<String> imports = mySymbolTable.getImports();

        for (int i = 0; i < imports.size(); i++) {
            if (Objects.equals(imports.get(i), var)) {
                jmmNode.put("type", imports.get(i));
                jmmNode.put("isArray", "false");

                return globalReports;
            }
        }


        //Se não for nenhum dos casos
        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");

        globalReports.add(Reports.reportCheckDeclaration(jmmNode));
        return globalReports;
    }

    private List<Report> checkFieldDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        return globalReports;
    }


    private List<Report> checkType(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        String value = jmmNode.get("value");

        if (_PERMITTED_TYPES.contains(value)) {
            jmmNode.put("type", value);
            if (value.equals("String[]") || value.equals("int[]")) {
                jmmNode.put("isArray", "true");
            } else {
                jmmNode.put("isArray", "false");
            }


            return globalReports;
        }

        //Verificar se o tipo não foi importado

        List<String> imports = mySymbolTable.getImports();

        for (int i = 0; i < imports.size(); i++) {
            if (imports.get(i).equals(jmmNode.get("value"))) {
                jmmNode.put("type", imports.get(i));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String superClass = mySymbolTable.getSuper();

        if (superClass != null) {
            if (superClass.equals(jmmNode.get("value"))) {
                jmmNode.put("type", jmmNode.get("value"));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String className = mySymbolTable.getClassName();

        if (className.equals(jmmNode.get("value"))) {
            jmmNode.put("type", jmmNode.get("value"));
            jmmNode.put("isArray", "false");
            return globalReports;
        }


        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");
        globalReports.add(Reports.reportCheckType(jmmNode));
        return globalReports;

    }

    private List<Report> checkBinaryOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        List<JmmNode> children = jmmNode.getChildren();
        String op = jmmNode.get("op");

        if (jmmNode.get("op").equals("!") && children.get(0).get("type").equals("boolean")) {
            return globalReports;
        } else if (jmmNode.get("op").equals("!") && !children.get(0).get("type").equals("boolean")) {

            globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "children nodes have different types"));
            return globalReports;
        }


        if (!Objects.equals(children.get(0).get("type"), children.get(1).get("type"))
                || children.get(0).get("isArray").equals("true")
                || children.get(1).get("isArray").equals("true")) {
            //significa que os dois nodes em que se está a fazer a operação são de tipos
            // diferentes, logo não dá
            //fazer um report


            jmmNode.put("type", "none");
            jmmNode.put("isArray", "false");

            globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "children nodes have different types"));

            return globalReports;
        } else {

            //Se não for um operador que permita a utilização de booleanos
            //e a situação de aceder a elementos de arrays?
            if (_BOOLEAN_OPERATORS.contains(op)) {
                jmmNode.put("type", "boolean");
                jmmNode.put("isArray", "false");
                return globalReports;
            } else {
                if (jmmNode.getChildren().get(0).get("type").equals("boolean")) {
                    //dar return a report
                    jmmNode.put("type", "int");
                    jmmNode.put("isArray", "false");

                    globalReports.add(Reports.reportCheckBinaryOp(jmmNode, "not boolean operation with boolean type children"));
                    return globalReports;
                } else {
                    jmmNode.put("type", "int");
                    jmmNode.put("isArray", "false");

                    return globalReports;
                }

            }


        }

        //return globalReports;
    }

    private List<Report> checkConditionalStatement(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        List<JmmNode> children = jmmNode.getChildren();


        if (jmmNode.getChildren().get(0).get("type").equals("boolean")) {
            return globalReports;
        }

        String op = children.get(0).get("op");

        if (!_BOOLEAN_OPERATORS.contains(op)) {
            globalReports.add(Reports.reportcheckConditionalStatement(jmmNode));
            return globalReports;
        }


        return globalReports;

    }

    private List<Report> checkLoopStatement(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        if (jmmNode.getChildren().get(0).get("type").equals("none")) {
            jmmNode.put("type", "none");
            jmmNode.put("type", "false");
            return globalReports;

        } else if (!jmmNode.getChildren().get(0).get("type").equals("boolean") || jmmNode.getChildren().get(0).get("isArray").equals("true")) {

            jmmNode.put("type", "none");
            jmmNode.put("type", "false");
            globalReports.add(Reports.reportCheckLoopStatement(jmmNode));
            return globalReports;
        }

        jmmNode.put("type", "none");
        jmmNode.put("type", "false");

        return globalReports;
    }

    private List<Report> checkWithStmt(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        jmmNode.put("type", "none");
        jmmNode.put("type", "false");

        return globalReports;
    }


    private List<Report> checkInteger(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");

        return globalReports;

    }


    private List<Report> checkAssignment(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        List<JmmNode> children = jmmNode.getChildren();

        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        List<Symbol> tipo = mySymbolTable.getLocalVariables(methodNode);


        String var = jmmNode.get("var");

        Boolean checkFields = true;

        //Se for um type que não é pârametro
        for (int i = 0; i < tipo.size(); i++) {
            if (Objects.equals(tipo.get(i).getName(), var)) {
                jmmNode.put("type", tipo.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(tipo.get(i).getType().isArray()));
                checkFields = false;
                break;
            }
        }

        List<Symbol> parameters = mySymbolTable.getParameters(methodNode);


        //Se for um type que não é pârametro
        for (int i = 0; i < parameters.size(); i++) {
            if (Objects.equals(parameters.get(i).getName(), var)) {
                jmmNode.put("type", parameters.get(i).getType().getName());
                jmmNode.put("isArray", String.valueOf(parameters.get(i).getType().isArray()));
                break;

            }
        }

        if (checkFields) {
            List<Symbol> fields = mySymbolTable.getFields();
            //Se for um type que não é pârametro
            for (int i = 0; i < fields.size(); i++) {
                if (Objects.equals(fields.get(i).getName(), var)) {
                    jmmNode.put("type", fields.get(i).getType().getName());
                    jmmNode.put("isArray", String.valueOf(fields.get(i).getType().isArray()));

                    if (methodNode.equals("main")) {
                        //trata do caso mesmo que seja static
                        globalReports.add(Reports.reportcheckAssignment(jmmNode));
                        return globalReports;
                    }
                    break;
                }
            }
        }


        List<String> imports = mySymbolTable.getImports();
        String extendedClassName = mySymbolTable.getSuper();
        String className = mySymbolTable.getClassName();


        //Primeiro é um int resultante de um array
        if (jmmNode.getChildren().size() > 1) {
            if (!jmmNode.getChildren().get(0).get("type").equals(jmmNode.getChildren().get(1).get("type"))) {

                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");
                globalReports.add(Reports.reportcheckAssignment(jmmNode));
                return globalReports;
            } else {
                return globalReports;

            }

        }

        if (jmmNode.hasAttribute("type")) {

            if (jmmNode.getChildren().get(0).hasAttribute("op")) {
                if (jmmNode.getChildren().get(0).get("op").equals("!")) {
                    if (jmmNode.get("type").equals(jmmNode.getChildren().get(0).getChildren().get(0).get("type"))
                            && jmmNode.get("type").equals("booelan")) {
                        return globalReports;
                    } else {
                        globalReports.add(Reports.reportcheckAssignment(jmmNode));
                        return globalReports;
                    }
                }
            }
            if (jmmNode.get("type").equals("none") && jmmNode.getChildren().get(0).get("type").equals("none")) {
                return globalReports;

            } else if (jmmNode.get("type").equals("none")) { //se o node tiver tipo none (não confundir com não estar declarado), passa a ter o type do child

                jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));
                jmmNode.put("isArray", jmmNode.getChildren().get(0).get("isArray"));
                return globalReports;

            } else if (jmmNode.getChildren().get(0).get("type").equals("none")) { //o type do filho é none, não acontece nada

                return globalReports;

            } else if (jmmNode.get("type").equals(extendedClassName) && jmmNode.getChildren().get(0).get("type").equals(className)) {

                return globalReports;
            } else if (imports.contains(jmmNode.get("type")) && imports.contains(jmmNode.getChildren().get(0).get("type"))) { //se ambos forem um import

                return globalReports;

            } else if (!jmmNode.get("type").equals(jmmNode.getChildren().get(0).get("type"))) {

                globalReports.add(Reports.reportcheckAssignment(jmmNode));
                return globalReports;

            } else if (jmmNode.get("type").equals(jmmNode.getChildren().get(0).get("type"))) {

                return globalReports;
            }
        } else { //ou um ou outro ou ambos são unknown, nunca nenhum foi declarado

            jmmNode.put("type", "none");
            jmmNode.put("isArray", "false");
            globalReports.add(Reports.reportcheckAssignment(jmmNode));
            return globalReports;
        }


        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");

        globalReports.add(Reports.reportcheckAssignment(jmmNode));
        return globalReports;
    }

    private List<Report> checkArrayDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        jmmNode.put("isArray", "true");
        jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));

        return globalReports;
    }

    private List<Report> checkReservedExpr(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        Optional<JmmNode> parent = jmmNode.getAncestor("mainDeclaration");

        String value = jmmNode.get("value");

        if (value.equals("true") || value.equals("false")) {
            jmmNode.put("type", "boolean");
            jmmNode.put("isArray", "false");
            return globalReports;
        }

        String className = mySymbolTable.getClassName();
        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        if (jmmNode.get("value").equals("this")) {

            if (methodNode.equals("main")) {
                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");
                globalReports.add(Reports.reportCheckReservedExpr(jmmNode));
                return globalReports;
            }

            jmmNode.put("type", className);
            jmmNode.put("isArray", "false");
            return globalReports;
        }


        if (parent.isPresent()) {
            //dar return do report de erro porque significa que o static tem como pai o mainMethod, o que
            // não pode ser
            globalReports.add(Reports.reportCheckReservedExpr(jmmNode));
            return globalReports;
        }

        return globalReports;
    }


    private List<Report> checkSubscriptOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        if (jmmNode.getChildren().get(0).get("isArray").equals("false")) {
            jmmNode.put("type", "int");
            jmmNode.put("isArray", "false");
            globalReports.add(Reports.reportCheckSubscriptOp(jmmNode));
            return globalReports;
        }

        if (!jmmNode.getChildren().get(1).get("type").equals("int")) {
            jmmNode.put("type", "int");
            jmmNode.put("isArray", "false");
            globalReports.add(Reports.reportCheckSubscriptOp(jmmNode));
            return globalReports;
        }

        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");

        return globalReports;
    }

    private List<Report> checkObjectDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        List<String> imports = mySymbolTable.getImports();

        for (int i = 0; i < imports.size(); i++) {
            if (imports.get(i).equals(jmmNode.get("objClass"))) {
                jmmNode.put("type", jmmNode.get("objClass"));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String superClass = mySymbolTable.getSuper();

        if (superClass != null) {
            if (superClass.equals(jmmNode.get("objClass"))) {
                jmmNode.put("type", jmmNode.get("objClass"));
                jmmNode.put("isArray", "false");
                return globalReports;
            }
        }

        String className = mySymbolTable.getClassName();

        if (className.equals(jmmNode.get("objClass"))) {
            jmmNode.put("type", jmmNode.get("objClass"));
            jmmNode.put("isArray", "false");
            return globalReports;
        }

        jmmNode.put("type", "none");
        jmmNode.put("isArray", "false");
        globalReports.add(Reports.reportCheckObjectDeclaration(jmmNode));
        return globalReports;
    }

    private List<Report> checkReturnStmt(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        String methodNode = null;
        Optional<JmmNode> instanceDeclaration = jmmNode.getAncestor("InstanceDeclaration");

        if (instanceDeclaration.isPresent()) {
            methodNode = instanceDeclaration.get().get("instance");
        } else {
            methodNode = "main";
        }

        Type returnType = mySymbolTable.getReturnType(methodNode);

        if (!returnType.getName().equals(jmmNode.getChildren().get(0).get("type"))
                && !returnType.getName().equals("none")
                && !jmmNode.getChildren().get(0).get("type").equals("none")) {
            //Return type e o child 0 não têm o mesmo type
            globalReports.add(Reports.checkReturnStmt(jmmNode));
            return globalReports;
        }

        return globalReports;
    }

    private List<Report> checkDotOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        String methodString = jmmNode.get("method");

        String methodNode = jmmNode.get("method");

        Type returnType = mySymbolTable.getReturnType(methodNode);


        List<String> imports = mySymbolTable.getImports();
        String extendedClassName = mySymbolTable.getSuper();
        String className = mySymbolTable.getClassName();


        //Se for de um import
        if (imports.contains(jmmNode.getChildren().get(0).get("type")) ||
                (className.equals(jmmNode.getChildren().get(0).get("type")) && extendedClassName != null)) {
            if (returnType != null) {


                List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

                List<String> parameterTypes = new ArrayList<>();

                List<String> parameterArrays = new ArrayList<>();

                for (int i = 0; i < parameters.size(); i++) {

                    parameterTypes.add(parameters.get(i).getType().getName());
                    parameterArrays.add(String.valueOf(parameters.get(i).getType().isArray()));


                }

                List<String> parameterTypesCalled = new ArrayList<>();
                List<String> parameterIsArrayCalled = new ArrayList<>();

                for (int i = 0; i < jmmNode.getChildren().size(); i++) {
                    if (i > 0) {

                        parameterTypesCalled.add(jmmNode.getChildren().get(i).get("type"));
                        parameterIsArrayCalled.add(jmmNode.getChildren().get(i).get("isArray"));

                    }

                }

                for (int i = 0; i < parameterTypesCalled.size(); i++) {

                    if (!parameterTypes.get(i).equals(parameterTypesCalled.get(i))
                            || !parameterArrays.get(i).equals(parameterIsArrayCalled.get(i))) {

                        jmmNode.put("type", returnType.getName());
                        jmmNode.put("isArray", String.valueOf(returnType.isArray()));
                        globalReports.add(Reports.reportCheckDotOp(jmmNode));
                        return globalReports;
                    }
                }

                if (parameterTypes.size() != parameterTypesCalled.size()) {
                    jmmNode.put("type", "none");
                    jmmNode.put("isArray", "false");
                    globalReports.add(Reports.reportCheckDotOp(jmmNode));
                    return globalReports;
                }


                jmmNode.put("type", returnType.getName());
                jmmNode.put("isArray", String.valueOf(returnType.isArray()));

                return globalReports;
            } else { //significa que é de um import, não se sabem os argumentos da função

                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");

                return globalReports;
            }
        } else { //se não tiver imports
            if (returnType != null) {


                List<Symbol> parameters = mySymbolTable.getParameters(methodNode);

                List<String> parameterTypes = new ArrayList<>();
                List<String> parameterArrays = new ArrayList<>();

                for (int i = 0; i < parameters.size(); i++) {


                    parameterTypes.add(parameters.get(i).getType().getName());
                    parameterArrays.add(String.valueOf(parameters.get(i).getType().isArray()));
                }

                List<String> parameterTypesCalled = new ArrayList<>();
                List<String> parameterIsArrayCalled = new ArrayList<>();

                for (int i = 0; i < jmmNode.getChildren().size(); i++) {
                    if (i > 0) {
                        if (jmmNode.getChildren().get(i).hasAttribute("type") && jmmNode.getChildren().get(i).hasAttribute("isArray")) {
                            parameterTypesCalled.add(jmmNode.getChildren().get(i).get("type"));
                            parameterIsArrayCalled.add(jmmNode.getChildren().get(i).get("isArray"));
                        }

                    }

                }


                if (parameterTypes.size() != parameterTypesCalled.size()) {
                    jmmNode.put("type", "none");
                    jmmNode.put("isArray", "false");
                    globalReports.add(Reports.reportCheckDotOp(jmmNode));
                    return globalReports;
                }

                for (int i = 0; i < parameterTypesCalled.size(); i++) {

                    if ((!parameterTypes.get(i).equals(parameterTypesCalled.get(i))
                            && !parameterTypes.get(i).equals("none")
                            && !parameterTypesCalled.get(i).equals("none"))
                            || !parameterArrays.get(i).equals(parameterIsArrayCalled.get(i))) {

                        jmmNode.put("type", returnType.getName());
                        jmmNode.put("isArray", String.valueOf(returnType.isArray()));
                        globalReports.add(Reports.reportCheckDotOp(jmmNode));
                        return globalReports;
                    }
                }


                jmmNode.put("type", returnType.getName());
                jmmNode.put("isArray", String.valueOf(returnType.isArray()));

                return globalReports;
            } else { //significa que é de um import, não se sabem os argumentos da função


                jmmNode.put("type", "none");
                jmmNode.put("isArray", "false");
                globalReports.add(Reports.reportCheckDotOp(jmmNode));
                return globalReports;
            }
        }

        //return globalReports;
    }

    private List<Report> checkParameterType(JmmNode jmmNode, MySymbolTable mySymbolTable) {

        jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));
        jmmNode.put("isArray", jmmNode.getChildren().get(0).get("isArray"));

        return globalReports;
    }

    private List<Report> checkInstanceDeclaration(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        if (jmmNode.getChildren().size() > 2) {
            if (jmmNode.getChildren().get(2).hasAttribute("type") && jmmNode.getChildren().get(2).hasAttribute("isArray")) {
                jmmNode.put("type", jmmNode.getChildren().get(2).get("type"));
                jmmNode.put("isArray", jmmNode.getChildren().get(2).get("isArray"));

            }
        } else {
            jmmNode.put("type", "none");
            jmmNode.put("isArray", "false");
        }


        return globalReports;
    }

    private List<Report> checkLengthOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        String methodString = jmmNode.get("method");

        jmmNode.put("type", "int");
        jmmNode.put("isArray", "false");

        return globalReports;

    }

    private List<Report> checkPrecedenceOp(JmmNode jmmNode, MySymbolTable mySymbolTable) {


        jmmNode.put("type", jmmNode.getChildren().get(0).get("type"));
        jmmNode.put("isArray", jmmNode.getChildren().get(0).get("isArray"));

        return globalReports;
    }

}
