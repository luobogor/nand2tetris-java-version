import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static List<JackTokenizer> jackTokenizerList = new ArrayList<>();

    public static void main(String[] args) {
        String filePath = args[0];
        traverseFolder(args[0]);
//        CodeWriter codeWriter = new CodeWriter(args[0]);
        for (JackTokenizer jackTokenizer : jackTokenizerList) {
//            codeWriter.setFileName(jackTokenizer.getFileName());
            while (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
//                switch (jackTokenizer.commandType()) {
//                    case C_ARITHMETIC:
//                        codeWriter.writeArithmetic(jackTokenizer.arg1());
//                        break;
//                    case C_PUSH:
//                        codeWriter.writePushPop(JackTokenizer.CommandType.C_PUSH, jackTokenizer.arg1(), jackTokenizer.arg2());
//                        break;
//                    case C_POP:
//                        codeWriter.writePushPop(JackTokenizer.CommandType.C_POP, jackTokenizer.arg1(), jackTokenizer.arg2());
//                        break;
//                    case C_LABEL:
//                        codeWriter.writeLabel(jackTokenizer.arg1());
//                        break;
//                    case C_GOTO:
//                        codeWriter.writeGoto(jackTokenizer.arg1());
//                        break;
//                    case C_IF:
//                        codeWriter.writeIf(jackTokenizer.arg1());
//                        break;
//                    case C_FUNCTION:
//                        codeWriter.writeFunction(jackTokenizer.arg1(), jackTokenizer.arg2());
//                        break;
//                    case C_RETURN:
//                        codeWriter.writeReturn();
//                        break;
//                    case C_CALL:
//                        codeWriter.writeCall(jackTokenizer.arg1(), jackTokenizer.arg2());
//                        break;
//                    default:
//                        String tips = jackTokenizer.getFileName() + "  " + jackTokenizer.getThisToken() + "命令异常";
//                        throw new RuntimeException(tips);
//                }
            }
        }
//        codeWriter.close();
    }

    public static void traverseFolder(String path) {
        File file = new File(path);
        if (file.exists()) {
            //单个文件
            if (file.getName().endsWith(".jack")) {
                JackTokenizer jackTokenizer = new JackTokenizer(path);
                jackTokenizerList.add(jackTokenizer);
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
                        JackTokenizer jackTokenizer = new JackTokenizer(filePath);
                        jackTokenizerList.add(jackTokenizer);
                    }
                }
            }
        } else {
            System.err.println("jack文件不存在!");
        }
    }
}

