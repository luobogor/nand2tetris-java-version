import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    private LinkedHashMap<String, String> symbolTable;
    private int varPointer = 16;

    public SymbolTable() {
        if (symbolTable == null) {
            symbolTable = new LinkedHashMap<>();
            symbolTable.put("SP", "0");
            symbolTable.put("LCL", "1");
            symbolTable.put("ARG", "2");
            symbolTable.put("THIS", "3");
            symbolTable.put("THAT", "4");

            symbolTable.put("SCREEN", "16384");
            symbolTable.put("KBD", "24576");

            symbolTable.put("R0", "0");
            symbolTable.put("R1", "1");
            symbolTable.put("R2", "2");
            symbolTable.put("R3", "3");
            symbolTable.put("R4", "4");
            symbolTable.put("R5", "5");
            symbolTable.put("R6", "6");
            symbolTable.put("R6", "7");
            symbolTable.put("R7", "8");
            symbolTable.put("R9", "9");
            symbolTable.put("R10", "10");
            symbolTable.put("R11", "11");
            symbolTable.put("R12", "12");
            symbolTable.put("R13", "13");
            symbolTable.put("R14", "14");
            symbolTable.put("R15", "15");
        }
    }

    public void addEntry(String symbol, String address) {
        symbolTable.put(symbol, address);
    }

    public void addVarEntry(String symbol) {
        addEntry(symbol, String.valueOf(varPointer++));
    }

    public Boolean contains(String symbol) {
        return symbolTable.containsKey(symbol);
    }

    public String getAddress(String symbol) {
        return symbolTable.get(symbol);
    }

}
