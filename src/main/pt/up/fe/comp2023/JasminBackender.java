package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

import static org.specs.comp.ollir.InstructionType.BINARYOPER;
import static org.specs.comp.ollir.InstructionType.RETURN;

public class JasminBackender implements JasminBackend {
    ClassUnit classUnit = null;
    String superClass;
    int limit_stack = 99;
    int limit_locals = 99;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        try {

            this.classUnit = ollirResult.getOllirClass();

            this.classUnit.checkMethodLabels();
            this.classUnit.buildCFGs();
            this.classUnit.buildVarTables();

            System.out.println("Generating Jasmin code...");

            String jasminCode = getJasminCode();
            List<Report> reports = new ArrayList<>();

            /*
            if (ollirResult.getConfig().get("debug") != null && ollirResult.getConfig().get("debug").equals("true")) {
                System.out.println("JASMIN CODE : \n" + jasminCode);
            }
             */

            System.out.println("JASMIN CODE : \n" + jasminCode);

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(classUnit.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1,
                            "Jasmin generation exception.", e)));
        }
    }

    private String getJasminCode() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(".class ").append(this.classUnit.getClassName()).append("\n");


        this.superClass = this.classUnit.getSuperClass();
        if (this.superClass == null) {
            this.superClass = "java/lang/Object";
        }

        boolean name_is_full = true;

        if(this.superClass.equals("this")){
            stringBuilder.append(".super ").append(this.classUnit.getClassName()).append("\n");
            name_is_full = false;
        }
        else{
            for (String importName : this.classUnit.getImports()) {
                if (importName.endsWith(this.superClass)) {
                    stringBuilder.append(".super ").append(importName.replaceAll("\\.", "/")).append("\n");
                    name_is_full = false;
                    break;
                }
            }
        }

        if (name_is_full) {
            stringBuilder.append(".super ").append(this.superClass).append("\n");
        }

        // Fields
        for (Field field : this.classUnit.getFields()) {
            StringBuilder accessSpec = new StringBuilder();
            if (field.getFieldAccessModifier() != AccessModifiers.DEFAULT) {
                accessSpec.append(field.getFieldAccessModifier().name().toLowerCase()).append(" ");
            }

            if (field.isStaticField()) {
                accessSpec.append("static ");
            }
            if (field.isInitialized()) {
                accessSpec.append("final ");
            }

            stringBuilder.append(".field ").append(accessSpec).append(field.getFieldName()).append(" ").append(this.getFieldDescriptor(field.getFieldType())).append("\n");
        }

        // Methods
        for (Method method : this.classUnit.getMethods()) {
            stringBuilder.append(this.getMethodHeader(method));
            stringBuilder.append(this.getMethodStatements(method));
            stringBuilder.append(".end method\n");
        }

        return stringBuilder.toString();
    }

    private String getFieldDescriptor(Type type) {
        StringBuilder stringBuilder = new StringBuilder();
        ElementType elementType = type.getTypeOfElement();

        if (elementType == ElementType.ARRAYREF) {
            stringBuilder.append("[");
            elementType = ((ArrayType) type).getArrayType();
        }

        switch (elementType) {
            case INT32 -> stringBuilder.append("I");
            case BOOLEAN -> stringBuilder.append("Z");
            case OBJECTREF -> {
                String name = ((ClassType) type).getName();
                boolean name_is_full = true;

                if(name.equals("this")){
                    stringBuilder.append("L").append(this.classUnit.getClassName()).append(";");
                    name_is_full = false;
                }
                else{
                    for (String importName : this.classUnit.getImports()) {
                        if (importName.endsWith(name)) {
                            stringBuilder.append("L").append(importName.replaceAll("\\.", "/")).append(";");
                            name_is_full = false;
                            break;
                        }
                    }
                }

                if (name_is_full) {
                    stringBuilder.append("L").append(name).append(";");
                }
            }
            case STRING -> stringBuilder.append("Ljava/lang/String;");
            case VOID -> stringBuilder.append("V");
            default -> stringBuilder.append("; ERROR: descriptor type is not supported\n");
        }

        return stringBuilder.toString();
    }

    private String getMethodHeader(Method method) {
        StringBuilder stringBuilder = new StringBuilder("\n.method ");

        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT) {
            stringBuilder.append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        }

        if (method.isStaticMethod()) stringBuilder.append("static ");
        if (method.isFinalMethod()) stringBuilder.append("final ");

        if (method.isConstructMethod()) {
            stringBuilder.append("<init>");
        } else {
            stringBuilder.append(method.getMethodName());
        }
        stringBuilder.append("(");

        for (Element param : method.getParams()) {
            stringBuilder.append(this.getFieldDescriptor(param.getType()));
        }
        stringBuilder.append(")");
        stringBuilder.append(this.getFieldDescriptor(method.getReturnType())).append("\n");

        return stringBuilder.toString();
    }

    private String getMethodStatements(Method method) {

        String methodInstructions = this.getMethodInstructions(method);

        return "\t.limit stack " + this.limit_stack + "\n" + "\t.limit locals " + this.limit_locals + "\n" + methodInstructions;
    }

    private String getMethodInstructions(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        List<Instruction> methodInstructions = method.getInstructions();
        for (Instruction instruction : methodInstructions) {

            for (Map.Entry<String, Instruction> label : method.getLabels().entrySet()) {
                if (label.getValue().equals(instruction)) {
                    stringBuilder.append(label.getKey()).append(":\n");
                }
            }

            stringBuilder.append(this.getInstruction(instruction, method.getVarTable()));
            if (instruction.getInstType() == InstructionType.CALL && ((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {
                stringBuilder.append("\tpop\n");
            }

        }

        boolean returnInstruction = (methodInstructions.size() > 0) && (methodInstructions.get(methodInstructions.size() - 1).getInstType() == RETURN);

        if (!returnInstruction && method.getReturnType().getTypeOfElement() == ElementType.VOID) {
            stringBuilder.append("\treturn\n");
        }

        return stringBuilder.toString();
    }

    private String getInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();
        return switch (instruction.getInstType()) {
            case ASSIGN -> this.getAssignInstruction((AssignInstruction) instruction, varTable);
            case CALL -> this.getCallInstruction((CallInstruction) instruction, varTable);
            //case GOTO -> this.getGotoInstruction((GotoInstruction) instruction);
            case RETURN -> this.getReturnInstruction((ReturnInstruction) instruction, varTable);
            case PUTFIELD -> this.getPutFieldInstruction((PutFieldInstruction) instruction, varTable);
            case GETFIELD -> this.getGetFieldInstruction((GetFieldInstruction) instruction, varTable);
            //case UNARYOPER -> this.getUnaryOperationInstruction((UnaryOpInstruction) instruction, varTable);
            case BINARYOPER -> this.getBinaryOperationInstruction((BinaryOpInstruction) instruction, varTable);
            case NOPER -> this.getLoadToStackInstruction(((SingleOpInstruction) instruction).getSingleOperand(), varTable);
            default -> stringBuilder.append("; ERROR: instruction type is not supported\n").toString();
        };
    }

    private String getBinaryOperationInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(this.getLoadToStackInstruction(instruction.getLeftOperand(), varTable)).append(this.getLoadToStackInstruction(instruction.getRightOperand(), varTable)).append("\t").append(this.getOperation(instruction.getOperation())).append("\n");

        return stringBuilder.toString();
    }

    private String getOperation(Operation operation) {
        return switch (operation.getOpType()) {
            case ADD -> "iadd";
            case SUB -> "isub";
            case MUL -> "imul";
            case DIV -> "idiv";

            default -> "; ERROR: operation not supported: " + operation.getOpType() + "\n";
        };
    }

    private String getLoadToStackInstruction(Element element, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        if (element instanceof LiteralElement) {
            String literal = ((LiteralElement) element).getLiteral();
            if (element.getType().getTypeOfElement() == ElementType.INT32 || element.getType().getTypeOfElement() == ElementType.BOOLEAN) {
                int parsedInt = Integer.parseInt(literal);

                if (parsedInt >= -1 && parsedInt <= 5) { // [-1,5]
                    stringBuilder.append("\ticonst_");
                } else if (parsedInt >= -128 && parsedInt <= 127) { // byte
                    stringBuilder.append("\tbipush ");
                } else if (parsedInt >= -32768 && parsedInt <= 32767) { // short
                    stringBuilder.append("\tsipush ");
                } else {
                    stringBuilder.append("\tldc "); // int
                }

                if (parsedInt == -1) {
                    stringBuilder.append("m1");
                } else {
                    stringBuilder.append(parsedInt);
                }

            } else {
                stringBuilder.append("\tldc ").append(literal);
            }

        } else if (element instanceof Operand) {
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case INT32, BOOLEAN -> stringBuilder.append("\tiload").append(this.getVariableNumber(operand.getName(), varTable));
                case OBJECTREF, STRING, ARRAYREF -> stringBuilder.append("\taload").append(this.getVariableNumber(operand.getName(), varTable));
                case THIS -> stringBuilder.append("\taload_0");
                default -> stringBuilder.append("; ERROR: getLoadToStack() operand ").append(operand.getType().getTypeOfElement()).append("\n");
            }
        } else {
            stringBuilder.append("; ERROR: getLoadToStack() unsupported element instance\n");
        }

        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String getVariableNumber(String name, HashMap<String, Descriptor> varTable) {
        if (name.equals("this")) {
            return "_0";
        }
        else {
            int virtualReg = varTable.get(name).getVirtualReg();

            StringBuilder stringBuilder = new StringBuilder();

            // virtual reg 0, 1, 2, 3 have specific operation
            if (virtualReg < 4) {
                stringBuilder.append("_");
            }else {
                stringBuilder.append(" ");
            }

            stringBuilder.append(virtualReg);

            return stringBuilder.toString();
        }
    }

    private String getAssignInstruction(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        Operand dest = (Operand) instruction.getDest();

        if (instruction.getRhs().getInstType() == BINARYOPER) {
            BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) instruction.getRhs();

            if (binaryOpInstruction.getOperation().getOpType() == OperationType.ADD) {
                boolean left_literal = binaryOpInstruction.getLeftOperand().isLiteral();
                boolean right_literal = binaryOpInstruction.getRightOperand().isLiteral();

                Operand operand = null;
                LiteralElement literal = null;

                if (left_literal && !right_literal) {
                    literal = (LiteralElement) binaryOpInstruction.getLeftOperand();
                    operand = (Operand) binaryOpInstruction.getRightOperand();
                } else if (!left_literal && right_literal) {
                    literal = (LiteralElement) binaryOpInstruction.getRightOperand();
                    operand = (Operand) binaryOpInstruction.getLeftOperand();
                }

                if (literal != null && operand != null) {
                    if (operand.getName().equals(dest.getName())) {
                        int literalValue = Integer.parseInt((literal).getLiteral());

                        if (literalValue >= -128 && literalValue <= 127) {
                            return "\tiinc " + varTable.get(operand.getName()).getVirtualReg() + " " + literalValue + "\n";
                        }
                    }
                }

            }
        }

        stringBuilder.append(this.getInstruction(instruction.getRhs(), varTable)).append(this.getStore(dest, varTable));

        return stringBuilder.toString();
    }

    private String getStore(Operand dest, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        switch (dest.getType().getTypeOfElement()) {
            case INT32, BOOLEAN -> {
                stringBuilder.append("\tistore").append(this.getVariableNumber(dest.getName(), varTable)).append("\n");
            }
            case OBJECTREF, THIS, STRING, ARRAYREF -> {
                stringBuilder.append("\tastore").append(this.getVariableNumber(dest.getName(), varTable)).append("\n");
            }
            default -> stringBuilder.append("; ERROR: getStore() type of element not supported\n");
        }

        return stringBuilder.toString();
    }

    private String getCallInstruction(CallInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();


        switch (instruction.getInvocationType()) {
            case invokevirtual -> {
                stringBuilder.append(this.getLoadToStackInstruction(instruction.getFirstArg(), varTable));

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStackInstruction(element, varTable));
                }

                boolean name_is_full = true;

                if(((ClassType) instruction.getFirstArg().getType()).getName().equals("this")){
                    stringBuilder.append("\tinvokevirtual ").append(this.classUnit.getClassName()).append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")).append("(");;
                    name_is_full = false;
                }
                else{
                    for (String importName : this.classUnit.getImports()) {
                        if (importName.endsWith(((ClassType) instruction.getFirstArg().getType()).getName())) {
                            stringBuilder.append("\tinvokevirtual ").append(importName.replaceAll("\\.", "/")).append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")).append("(");;
                            name_is_full = false;
                            break;
                        }
                    }
                }

                if (name_is_full) {
                    stringBuilder.append("\tinvokevirtual ").append(((ClassType) instruction.getFirstArg().getType()).getName()).append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")).append("(");
                }

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                }

            }
            case invokespecial -> {
                stringBuilder.append(this.getLoadToStackInstruction(instruction.getFirstArg(), varTable));

                stringBuilder.append("\tinvokespecial ");

                if (instruction.getFirstArg().getType().getTypeOfElement() == ElementType.THIS) {
                    stringBuilder.append(this.superClass);
                } else {
                    String className = null;
                    boolean name_is_full = true;

                    if(((ClassType) instruction.getFirstArg().getType()).getName().equals("this")){
                        className = this.classUnit.getClassName();
                        name_is_full = false;
                    }
                    else{
                        for (String importName : this.classUnit.getImports()) {
                            if (importName.endsWith(((ClassType) instruction.getFirstArg().getType()).getName())) {
                                className = importName.replaceAll("\\.", "/");
                                name_is_full = false;
                                break;
                            }
                        }
                    }

                    if (name_is_full) {
                        className = ((ClassType) instruction.getFirstArg().getType()).getName();
                    }
                    stringBuilder.append(className);
                }

                stringBuilder.append("/").append("<init>(");

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

                if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                }

            }
            case invokestatic -> {

                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getLoadToStackInstruction(element, varTable));
                }

                boolean name_is_full = true;

                if(((Operand) instruction.getFirstArg()).getName().equals("this")){
                    stringBuilder.append("\tinvokestatic ").append(this.classUnit.getClassName()).append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")).append("(");
                    name_is_full = false;
                }
                else{
                    for (String importName : this.classUnit.getImports()) {
                        if (importName.endsWith(((Operand) instruction.getFirstArg()).getName())) {
                            stringBuilder.append("\tinvokestatic ").append(importName.replaceAll("\\.", "/")).append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")).append("(");
                            name_is_full = false;
                            break;
                        }
                    }
                }

                if (name_is_full) {
                    stringBuilder.append("\tinvokestatic ").append(((Operand) instruction.getFirstArg()).getName()).append("/").append(((LiteralElement) instruction.getSecondArg()).getLiteral().replace("\"", "")).append("(");
                }


                for (Element element : instruction.getListOfOperands()) {
                    stringBuilder.append(this.getFieldDescriptor(element.getType()));
                }

                stringBuilder.append(")").append(this.getFieldDescriptor(instruction.getReturnType())).append("\n");

            }
            case NEW -> {

                ElementType elementType = instruction.getReturnType().getTypeOfElement();

                if (elementType == ElementType.OBJECTREF) {
                    for (Element element : instruction.getListOfOperands()) {
                        stringBuilder.append(this.getLoadToStackInstruction(element, varTable));
                    }

                    boolean name_is_full = true;

                    if(((Operand) instruction.getFirstArg()).getName().equals("this")){
                        stringBuilder.append("\tnew ").append(this.classUnit.getClassName()).append("\n");
                        name_is_full = false;
                    }
                    else{
                        for (String importName : this.classUnit.getImports()) {
                            if (importName.endsWith(((Operand) instruction.getFirstArg()).getName())) {
                                stringBuilder.append("\tnew ").append(importName.replaceAll("\\.", "/")).append("\n");
                                name_is_full = false;
                                break;
                            }
                        }
                    }

                    if (name_is_full) {
                        stringBuilder.append("\tnew ").append(((Operand) instruction.getFirstArg()).getName()).append("\n");
                    }

                } else {
                    stringBuilder.append("; ERROR: new invocation type not supported\n");
                }
            }
            case ldc -> stringBuilder.append(this.getLoadToStackInstruction(instruction.getFirstArg(), varTable));
            default -> stringBuilder.append("; ERROR: call instruction not supported\n");
        }

        return stringBuilder.toString();
    }

    private String getReturnInstruction(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();
        if(instruction.hasReturnValue()) {
            stringBuilder.append(this.getLoadToStackInstruction(instruction.getOperand(), varTable));
        }
        stringBuilder.append("\t");
        if(instruction.getOperand() != null){
            if(instruction.getOperand().getType().getTypeOfElement() == ElementType.INT32 || instruction.getOperand().getType().getTypeOfElement() == ElementType.BOOLEAN) {
                stringBuilder.append("i");
            } else {
                stringBuilder.append("a");
            }
        }

        stringBuilder.append("return\n");

        return stringBuilder.toString();
    }

    private String getPutFieldInstruction(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        String className = null;
        boolean name_is_full = true;

        if(((Operand) instruction.getFirstOperand()).getName().equals("this")){
            className = this.classUnit.getClassName();
            name_is_full = false;
        }
        else{
            for (String importName : this.classUnit.getImports()) {
                if (importName.endsWith(((Operand) instruction.getFirstOperand()).getName())) {
                    className = importName.replaceAll("\\.", "/");
                    name_is_full = false;
                    break;
                }
            }
        }

        if (name_is_full) {
            className = ((Operand) instruction.getFirstOperand()).getName();
        }

        stringBuilder.append(this.getLoadToStackInstruction(instruction.getFirstOperand(), varTable) + this.getLoadToStackInstruction(instruction.getThirdOperand(), varTable) + "\tputfield " + className + "/" + ((Operand) instruction.getSecondOperand()).getName() + " " + this.getFieldDescriptor(instruction.getSecondOperand().getType()) + "\n");

        return stringBuilder.toString();
    }

    private String getGetFieldInstruction(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder stringBuilder = new StringBuilder();

        String className = null;
        boolean name_is_full = true;

        if(((Operand) instruction.getFirstOperand()).getName().equals("this")){
            className = this.classUnit.getClassName();
            name_is_full = false;
        }
        else{
            for (String importName : this.classUnit.getImports()) {
                if (importName.endsWith(((Operand) instruction.getFirstOperand()).getName())) {
                    className = importName.replaceAll("\\.", "/");
                    name_is_full = false;
                    break;
                }
            }
        }

        if (name_is_full) {
            className = ((Operand) instruction.getFirstOperand()).getName();
        }

        stringBuilder.append(this.getLoadToStackInstruction(instruction.getFirstOperand(), varTable) + "\tgetfield " + className + "/" + ((Operand) instruction.getSecondOperand()).getName() + " " + this.getFieldDescriptor(instruction.getSecondOperand().getType()) + "\n");

        return stringBuilder.toString();
    }

}
