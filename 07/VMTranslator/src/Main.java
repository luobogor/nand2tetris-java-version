
public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser(args[0]);
        CodeWriter codeWriter = new CodeWriter(args[0]);
        while (parser.hasMoreCommands()) {
            parser.advance();
            switch (parser.commandType()) {
                case C_ARITHMETIC:
                    codeWriter.writeArithmetic(parser.arg1());
                    break;
                case C_PUSH:
                    codeWriter.writePushPop(Parser.CommandType.C_PUSH,parser.arg1(),parser.arg2());
                    break;
                case C_POP:
                    codeWriter.writePushPop(Parser.CommandType.C_POP,parser.arg1(),parser.arg2());
                    break;
                default:
                    return;
            }
        }
        codeWriter.close();
    }
}

