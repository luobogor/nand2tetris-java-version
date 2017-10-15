import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private BufferedWriter bw;

    public CompilationEngine() {
    }

    public CompilationEngine(String outputPath) {
        try {
            if (outputPath.endsWith(".jack")) {
                outputPath = outputPath.replace(".jack", ".xml");
            } else {
                File file = new File(outputPath);
                outputPath = outputPath + "/" + file.getName() + ".xml";
            }
            bw = new BufferedWriter(new FileWriter(outputPath));
            write("<tokens>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outPut(String str) {
        write(str);
    }

    private void write(String str) {
        try {
            bw.write(str);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void close() {
        try {
            write("</tokens>");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
