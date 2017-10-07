import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Parser parser = new Parser(args[0]);
            Code code = new Code();
            SymbolTable symbolTable = new SymbolTable();
            BufferedWriter out = new BufferedWriter(new FileWriter(args[0].replace(".asm", ".hack")));

            while (parser.hasMoreCommands()) {
                parser.advance();
                if (parser.commandType() == Parser.CommandType.L_COMMAND) {
                    symbolTable.addEntry(parser.symbol(), String.valueOf(parser.getPointer()-parser.getLCommandCounter()));
                    parser.addLCommand();
                }
            }

            parser.initPointer();

            while (parser.hasMoreCommands()) {
                parser.advance();
                switch (parser.commandType()) {
                    case A_COMMAND:
                        String AInstruction = parser.symbol();
                        if (parser.isVar(AInstruction)) {
                            if (!symbolTable.contains(AInstruction)) {
                                symbolTable.addVarEntry(AInstruction);
                            }
                            AInstruction = Code.getAInstructionBinaryString(symbolTable.getAddress(AInstruction));
                        } else {
                            AInstruction = Code.getAInstructionBinaryString(AInstruction);
                        }
                        System.out.println(AInstruction);
                        out.write(AInstruction);
                        break;
                    case C_COMMAND:
                        String dest = code.dest(parser.dest());
                        String comp = code.comp(parser.comp());
                        String jump = code.jump(parser.jump());
                        System.out.println("111" + comp + dest + jump);
                        out.write("111" + comp + dest + jump);
                        break;
                    case L_COMMAND:
                        continue;
                    default:
                        throw new RuntimeException("line" + parser.getPointer() + " illegal instruction");
                }
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

