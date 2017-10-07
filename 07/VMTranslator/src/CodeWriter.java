import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    private BufferedWriter bw;
    private String fileName;
    private int labelFlag;

    public CodeWriter(String outputPath) {
        try {
            labelFlag = 0;
            File file = new File(outputPath.replace(".vm", ".asm"));
            String tempFileName = file.getName();
            fileName = tempFileName.substring(0, tempFileName.lastIndexOf('.'));
            bw = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeArithmetic(String command) {
        try {
            bw.newLine();
            switch (command) {
                case "add":
                    bw.write("//add");
                    bw.newLine();
                    binaryOperation1("+");
                    break;
                case "sub":
                    bw.write("//sub");
                    bw.newLine();
                    binaryOperation1("-");
                    break;
                case "neg":
                    bw.write("//neg");
                    bw.newLine();
                    negOperation();
                    break;
                case "eq":
                    bw.write("//eq");
                    bw.newLine();
                    binaryOperation1("-");
                    binaryOperation2("=");
                    break;
                case "gt":
                    bw.write("//gt");
                    bw.newLine();
                    binaryOperation1("-");
                    binaryOperation2(">");
                    break;
                case "lt":
                    bw.write("//lt");
                    bw.newLine();
                    binaryOperation1("-");
                    binaryOperation2("<");
                    break;
                case "and":
                    bw.write("//and");
                    bw.newLine();
                    binaryOperation1("&");
                    break;
                case "or":
                    bw.write("//or");
                    bw.newLine();
                    binaryOperation1("|");
                    break;
                case "not":
                    bw.write("//not");
                    bw.newLine();
                    notOperation();
                    break;
                default:
                    throw new RuntimeException("UnExcepted arithmetic");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void writePushPop(Parser.CommandType commandType, String segment, int index) {
        try {
            bw.newLine();
            if (commandType == Parser.CommandType.C_PUSH) {
                bw.write("//push " + segment + " " + index);
                bw.newLine();
                switch (segment) {
                    case "constant":
                        pushConstant("SP", index);
                        break;
                    case "local":
                        pushLATT("LCL", index);
                        break;
                    case "argument":
                        pushLATT("ARG", index);
                        break;
                    case "this":
                        pushLATT("THIS", index);
                        break;
                    case "that":
                        pushLATT("THAT", index);
                        break;
                    case "temp":
                        pushTemp(index);
                        break;
                    case "pointer":
                        pushPointer(index);
                        break;
                    case "static":
                        pushStatic(index);
                        break;
                }
            } else if (commandType == Parser.CommandType.C_POP) {
                bw.write("//pop " + segment + " " + index);
                bw.newLine();
                switch (segment) {
                    case "local":
                        popLATT("LCL", index);
                        break;
                    case "argument":
                        popLATT("ARG", index);
                        break;
                    case "this":
                        popLATT("THIS", index);
                        break;
                    case "that":
                        popLATT("THAT", index);
                        break;
                    case "temp":
                        popTemp(index);
                        break;
                    case "pointer":
                        popPointer(index);
                        break;
                    case "static":
                        popStatic(index);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void notOperation() {
        try {
            //SP--
            SPMinusMinus();
            //*SP
            AP("SP");
            //*SP = ! *SP
            bw.write("M=!M");
            bw.newLine();
            //SP++
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void negOperation() {
        //取反后+1得到补码
        try {
            notOperation();
            //SP--
            SPMinusMinus();
            //*SP
            AP("SP");
            //*SP = *SP + 1
            bw.write("M=M+1");
            bw.newLine();
            //SP++
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binaryOperation1(String arithmetic) {
        try {
            //SP--
            SPMinusMinus();
            //D = *SP
            DEAP("SP");
            //SP--
            SPMinusMinus();
            //*SP = *SP + D
            bw.write("A=M");
            bw.newLine();
            bw.write("M=M" + arithmetic + "D");
            bw.newLine();
            //SP++
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void binaryOperation2(String arithmetic) {
        try {
            //true:0, false:-1
            int a = 0, b = 0, c = 0;
            switch (arithmetic) {
                case "=":
                    a = -1;
                    break;
                case ">":
                    b = -1;
                    break;
                case "<":
                    c = -1;
                    break;
            }
            String flag = "LABELFLAG_";
            String label1 = flag + labelFlag++;
            String label2 = flag + labelFlag++;
            String label3 = flag + labelFlag++;
            String labelEnd = flag + labelFlag++;

            //SP--
            SPMinusMinus();
            //D = *SP
            DEAP("SP");
            bw.write("@" + label1);
            bw.newLine();
            bw.write("D;JEQ");
            bw.newLine();
            bw.write("@" + label2);
            bw.newLine();
            bw.write("D;JGT");
            bw.newLine();
            bw.write("@" + label3);
            bw.newLine();
            bw.write("D;JLT");
            bw.newLine();

            bw.write("(" + label1 + ")");
            bw.newLine();
            //if(D == 0) 即 x==y 则 *SP = 1 否则 *SP = 0
            AP("SP");
            bw.write("M=" + a);
            bw.newLine();
            JMP(labelEnd);

            bw.write("(" + label2 + ")");
            bw.newLine();
            //if(D > 0) 即 x>y 则 *SP = 1 否则 *SP = 0
            AP("SP");
            bw.write("M=" + b);
            bw.newLine();
            JMP(labelEnd);

            bw.write("(" + label3 + ")");
            bw.newLine();
            //if(D < 0) 即 x<y 则 *SP = 1 否则 *SP = 0
            AP("SP");
            bw.write("M=" + c);
            bw.newLine();
            JMP(labelEnd);

            bw.write("(" + labelEnd + ")");
            bw.newLine();
            //SP++
            SPPlusPlus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //*pointer
    private void AP(String pointer) {
        try {
            bw.write("@" + pointer);
            bw.newLine();
            bw.write("A=M");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void JMP(String label) {
        try {
            bw.write("@" + label);
            bw.newLine();
            bw.write("0;JMP");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //D = *p
    private void DEAP(String pointer) {
        try {
            bw.write("@" + pointer);
            bw.newLine();
            bw.write("A=M");
            bw.newLine();
            bw.write("D=M");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //D = *(pointer+i)
    private void DEAPPlusi(String pointer, int index) {
        try {
            bw.write("@" + pointer);
            bw.newLine();
            bw.write("D=M");
            bw.newLine();
            bw.write("@" + index);
            bw.newLine();
            bw.write("A=D+A");
            bw.newLine();
            bw.write("D=M");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*p = D
    private void APED(String pointer) {
        try {
            bw.write("@" + pointer);
            bw.newLine();
            bw.write("A=M");
            bw.newLine();
            bw.write("M=D");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //D = x
    //x为预定义变量或常量
    private void DEX(int x) {
        DEX(String.valueOf(x));
    }

    private void DEX(String x) {
        try {
            bw.write("@" + x);
            bw.newLine();
            bw.write("D=A");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void MED(String x) {
        try {
            bw.write("@" + x);
            bw.newLine();
            bw.write("M=D");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void DEM(String addr) {
        try {
            bw.write("@" + addr);
            bw.newLine();
            bw.write("D=M");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //SP++
    private void SPPlusPlus() {
        try {
            bw.write("@SP");
            bw.newLine();
            bw.write("M=M+1");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //SP--
    private void SPMinusMinus() {
        try {
            bw.write("@SP");
            bw.newLine();
            bw.write("M=M-1");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pushConstant(String pointer, int index) {
        //*pointer = index

        //D = index
        DEX(index);
        //*pointer = D
        APED(pointer);
        //SP++
        SPPlusPlus();
    }

    private void pushLATT(String pointer, int index) {
        //push local 8 => *SP = *(LCL + 8),SP++

        //D = *(LCL + 8)
        DEAPPlusi(pointer, index);
        //*SP = D
        APED("SP");
        //SP++
        SPPlusPlus();
    }

    private void pushTemp(int index) {
        //push temp i => *SP = M[R(5+i)];,SP++

        //D = M[R(5+i)]
        DEM("R" + (index + 5));
        //*SP = D
        APED("SP");
        //SP++
        SPPlusPlus();
    }

    private void pushPointer(int index) {
        //push pointer 0/1 => *SP = THIS/THAT,SP++

        //D = THIS/THAT
        if (index == 0) {
            DEM("THIS");
        } else {
            DEM("THAT");
        }
        //*SP = D
        APED("SP");
        //SP++
        SPPlusPlus();
    }

    private void pushStatic(int index) {
        //push static i => D = M[fileName.i], *SP = D, SP++

        //D = M[fileName.i]
        DEM(fileName + "." + index);
        //*SP = D
        APED("SP");
        //SP++
        SPPlusPlus();
    }

    private void popTemp(int index) {
        //pop temp i => SP--, M[R(5+i)] = *SP

        //SP--
        SPMinusMinus();
        //D = *SP
        DEAP("SP");
        MED("R" + (5 + index));
    }

    private void popPointer(int index) {
        //pop pointer 0/1 => SP--, THIS/THAT = *SP

        //SP--
        SPMinusMinus();
        //D = *SP
        DEAP("SP");
        //THIS/THAT = D
        if (index == 0) {
            MED("THIS");
        } else {
            MED("THAT");
        }
    }

    private void popLATT(String pointer, int index) {
        try {
            //address = pointer + i
            bw.write("@" + pointer);
            bw.newLine();
            bw.write("D=M");
            bw.newLine();
            bw.write("@" + index);
            bw.newLine();
            bw.write("D=D+A");
            bw.newLine();
            bw.write("@addr");
            bw.newLine();
            bw.write("M=D");
            bw.newLine();
            //SP--
            SPMinusMinus();
            //*addr = *SP

            //D = *SP
            DEAP("SP");
            //*addr = D
            APED("addr");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void popStatic(int index) {
        //pop static i => D = stack.pop, @fileName.i, M = D

        //SP--
        SPMinusMinus();
        //D = *SP
        DEAP("SP");
        //@fileName.i, M = D
        MED(fileName + "." + index);
    }


    public void close() {
        try {
            bw.newLine();
            bw.write("//end");
            bw.newLine();
            bw.write("(END)");
            bw.newLine();
            bw.write("@END");
            bw.newLine();
            bw.write("0;JMP");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
