import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static List<Parser> parserList = new ArrayList<>();

    public static void main(String[] args) {
        String filePath = args[0];
        traverseFolder(args[0]);
        CodeWriter codeWriter = new CodeWriter(args[0]);
        //BasicLoop,Fibonacci,SimpleFunction不需要用到writeInit可以注释掉
        if (!filePath.endsWith(".vm")) {
            codeWriter.writeInit();
        }
        for (Parser parser : parserList) {
            codeWriter.setFileName(parser.getFileName());
            while (parser.hasMoreCommands()) {
                parser.advance();
//                codeWriter.writeBreakPoint();
                switch (parser.commandType()) {
                    case C_ARITHMETIC:
                        codeWriter.writeArithmetic(parser.arg1());
                        break;
                    case C_PUSH:
                        codeWriter.writePushPop(Parser.CommandType.C_PUSH, parser.arg1(), parser.arg2());
                        break;
                    case C_POP:
                        codeWriter.writePushPop(Parser.CommandType.C_POP, parser.arg1(), parser.arg2());
                        break;
                    case C_LABEL:
                        codeWriter.writeLabel(parser.arg1());
                        break;
                    case C_GOTO:
                        codeWriter.writeGoto(parser.arg1());
                        break;
                    case C_IF:
                        codeWriter.writeIf(parser.arg1());
                        break;
                    case C_FUNCTION:
                        codeWriter.writeFunction(parser.arg1(), parser.arg2());
                        break;
                    case C_RETURN:
//                        codeWriter.writeBreakPoint();
                        codeWriter.writeReturn();
                        break;
                    case C_CALL:
//                        codeWriter.writeBreakPoint();
                        codeWriter.writeCall(parser.arg1(), parser.arg2());
                        break;
                    default:
                        String tips = parser.getFileName() + "  " + parser.getThisCommand() + "命令异常";
                        throw new RuntimeException(tips);
                }
            }
        }
        codeWriter.close();
    }

    public static void traverseFolder(String path) {
        File file = new File(path);
        if (file.exists()) {
            //单个文件
            if (file.getName().endsWith(".vm")) {
                Parser parser = new Parser(path);
                parserList.add(parser);
                return;
            }
            //文件夹
            File[] files = file.listFiles();
            if (files.length == 0) {
                System.err.println("文件夹是空的!");
                return;
            } else {
                for (File item : files) {
                    String filePath = item.getAbsolutePath();
                    if (filePath.endsWith(".vm")) {
                        Parser parser = new Parser(filePath);
                        parserList.add(parser);
                    }
                }
            }
        } else {
            System.err.println("vm文件不存在!");
        }
    }
}

