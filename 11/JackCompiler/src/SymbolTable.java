import domain.Kind;
import domain.Symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private Map<String, Symbol> classSymbols = new HashMap<>();
    private Map<String, Symbol> subroutineSymbols = new HashMap<>();
    private Map<String, Symbol> scope;
    private int staticNum = 0;
    private int fieldNum = 0;
    private int argNum = 0;
    private int varNum = 0;

    public SymbolTable() {
    }

    public void localAtClassScope() {
        scope = classSymbols;
    }

    public void localAtSubroutineScope() {
        scope = subroutineSymbols;
    }

    public void startSubroutine() {
        subroutineSymbols.clear();
        argNum = 0;
        varNum = 0;
    }

    public void define(String name, String type, String kind) {
        switch (kind) {
            case "static":
                classSymbols.put(name, new Symbol(name, type, Kind.STATIC, staticNum++));
                break;
            case "field":
                classSymbols.put(name, new Symbol(name, type, Kind.FIELD, fieldNum++));
                break;
            case "arg":
                subroutineSymbols.put(name, new Symbol(name, type, Kind.ARG, argNum++));
                break;
            case "var":
                subroutineSymbols.put(name, new Symbol(name, type, Kind.VAR, varNum++));
                break;
        }
    }


    public int varCount(Kind kind) {
        switch (kind) {
            case STATIC:
                return staticNum;
            case FIELD:
                return fieldNum;
            case ARG:
                return argNum;
            case VAR:
                return varNum;
            default:
                return 0;
        }
    }

    public Kind kindOf(String name) {
        return scope.get(name) == null ? Kind.NONE : scope.get(name).getKind();
    }

    public String typeOf(String name) {
        return scope.get(name).getType();
    }

    public int indexOf(String name) {
        return scope.get(name).getIdx();
    }

}
