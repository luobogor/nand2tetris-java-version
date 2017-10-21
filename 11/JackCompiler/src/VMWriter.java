import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static domain.VMEnum.*;
import static utils.StringUtils.wrapByParenthesis;

public class VMWriter {
    private BufferedWriter bw;
    private String className;

    public VMWriter() {

    }

    public VMWriter(String outputPath) {
        try {
            File file = new File(outputPath);
            className = file.getName().substring(0, file.getName().indexOf("."));
            outputPath = outputPath.replace(".jack", ".vm");
            bw = new BufferedWriter(new FileWriter(outputPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void writePush(String segment, int index) {
        write("push " + segment + " " + String.valueOf(index));
    }

    public void writePop(String segment, int index) {
        write("pop " + segment + " " + String.valueOf(index));
    }

    public void writeArithmetic(String command) {
        switch (command) {
            case "+":
                write(ADD);
                break;
            case "-":
                write(SUB);
                break;
            case "_":
                write(NEG);
                break;
            case ">":
                write(GT);
                break;
            case "<":
                write(LT);
                break;
            case "=":
                write(EQ);
                break;
            case "|":
                write(OR);
                break;
            case "&":
                write(AND);
                break;
            case "~":
                write(NOT);
                break;
            default:
                throw new RuntimeException("not exist arithmetic symbol " + command);
        }
    }

    public void writeLabel(String label) {
        write("label " + label);
    }


    public void writeCall(String name, int nArgs) {
        //call f n
        //在n个参数被压入栈之后调用f函数
        write("call " + name + " " + String.valueOf(nArgs));
    }

    public void writeFunction(String name, int nArgs) {
        //function className.funName nArgs
        write("function " + className + "." + name + " " + String.valueOf(nArgs));
    }

    public void writeIf(String label) {
        write("if-goto " + label);
    }

    public void writeGoto(String label) {
        write("goto " + label);
    }

    public void writeReturn() {
        write("return");
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
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
