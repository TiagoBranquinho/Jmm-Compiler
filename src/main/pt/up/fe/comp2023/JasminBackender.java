package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.*;

public class JasminBackender implements JasminBackend {
    ClassUnit classUnit = null;
    String superClass;
    int limit_stack = 99;
    int limit_locals= 99;
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        try {

            this.classUnit = ollirResult.getOllirClass();

            // SETUP classUnit
            this.classUnit.checkMethodLabels();
            this.classUnit.buildCFGs();
            this.classUnit.buildVarTables();

            System.out.println("Generating Jasmin code ...");

            String jasminCode = getJasminCode(); //implement Jasmin code generator
            List<Report> reports = new ArrayList<>();

            if (ollirResult.getConfig().get("debug") != null && ollirResult.getConfig().get("debug").equals("true")) {
                System.out.println("JASMIN CODE : \n" + jasminCode);
            }

            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(classUnit.getClassName(), null,
                    Collections.singletonList(Report.newError(Stage.GENERATION, -1, -1,
                            "Jasmin generation exception.", e)));
        }

    }

    private String getJasminCode() {
        StringBuilder stringBuilder = new StringBuilder();

        // .class  <access-spec> <class-name>
        stringBuilder.append(".class ").append(this.classUnit.getClassName()).append("\n");


        this.superClass = this.classUnit.getSuperClass();
        if (this.superClass == null) {
            this.superClass = "java/lang/Object";
        }

        // .super  <class-name>
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
            // .field <access-spec> <field-name> <descriptor>
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
            // .method <access-spec> <method-spec>
            //     <statements>
            // .end method
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
                    stringBuilder.append(".super ").append(name).append("\n");
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

        // <access-spec>
        if (method.getMethodAccessModifier() != AccessModifiers.DEFAULT) {
            stringBuilder.append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        }

        if (method.isStaticMethod()) stringBuilder.append("static ");
        if (method.isFinalMethod()) stringBuilder.append("final ");

        // <method-spec>
        if (method.isConstructMethod())
            stringBuilder.append("<init>");
        else
            stringBuilder.append(method.getMethodName());
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

        return "\t.limit stack " + this.limit_stack + "\n" +
                "\t.limit locals " + this.limit_locals + "\n" +
                methodInstructions;
    }

    private String getMethodInstructions(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        return stringBuilder.toString();
    }
}
