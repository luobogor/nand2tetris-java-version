import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JackAnalyzer {
    static List<JackTokenizer> jackTokenizerList = new ArrayList<>();

    public static void main(String[] args) {
        String filePath = args[0];
        traverseFolder(filePath);
        for (JackTokenizer jackTokenizer : jackTokenizerList) {
            //generate T.xml
            //new CompilationEngine(jackTokenizer,"");
            jackTokenizer.initPointer();
            new CompilationEngine(jackTokenizer);
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

