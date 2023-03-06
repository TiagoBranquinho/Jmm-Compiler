package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.Map;

public class VariableDescriptor {

    private Symbol symbol;

    public VariableDescriptor(JmmNode node){
            symbol = processNode(node);
    }

    private Symbol processNode(JmmNode node){
        return new Symbol(new Type("int", false), "gato");
    }

    public Symbol getSymbol() {
        return symbol;
    }


}
