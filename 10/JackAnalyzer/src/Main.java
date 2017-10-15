import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static utils.StringUtils.*;

public class Main {
    static List<JackTokenizer> jackTokenizerList = new ArrayList<>();

    public static void main(String[] args) {
        String filePath = args[0];
        traverseFolder(filePath);
        for (JackTokenizer jackTokenizer : jackTokenizerList) {
            CompilationEngine compilationEngine = new CompilationEngine(jackTokenizer.getFilePath());
            while (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                //option + enter 生成条件
                switch (jackTokenizer.tokenType()) {
                    case KEYWORD:
                        compilationEngine.outPut(wrapByKeyword(jackTokenizer.keyword()));
                        break;
                    case SYBOL:
                        compilationEngine.outPut(wrapBySymbol(jackTokenizer.symbol()));
                        break;
                    case IDENTIFIER:
                        compilationEngine.outPut(wrapByIdentifier(jackTokenizer.identifier()));
                        break;
                    case INT_CONSTANT:
                        compilationEngine.outPut(wrapByIntegerConstant(String.valueOf(jackTokenizer.intVal())));
                        break;
                    case STRING_CONSTANT:
                        compilationEngine.outPut(wrapByStringConstant(jackTokenizer.stringVal()));
                        break;
                    default:
                        String tips = jackTokenizer.getFileName() + "  " + jackTokenizer.getThisToken() + " unknown token";
                        throw new RuntimeException(tips);
                }
            }
            compilationEngine.close();
        }
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
                    if (filePath.endsWith(".jack")) {
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

