import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    enum CommandType {
        C_ARITHMETIC,
        C_PUSH,
        C_POP,
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL
    }

    private List<String> arithmetics;
    private List<String> commands;
    private int pointer;
    private String thisCommand;

    public Parser() {
    }

    public Parser(String filePath) {
        initArithmetics();
        initPointer();
        commands = new ArrayList();
        String line;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            line = in.readLine();
            while (line != null) {
                System.out.println(line);
                if (line.equals("") || line.charAt(0) == '/' && line.charAt(1) == '/') {
                    line = in.readLine();
                    continue;
                }
                String[] splitRes = line.split("//");
                commands.add(splitRes[0]);
                line = in.readLine();
            }
            System.out.println();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void advance() {
        pointer++;
        this.thisCommand = commands.get(pointer);
    }

    public String arg1() {
        if (commandType() == CommandType.C_ARITHMETIC) {
            return thisCommand;
        }
        return thisCommand.split(" ")[1];
    }

    public int arg2() {
        return Integer.parseInt(thisCommand.split(" ")[2]);
    }

    private void initArithmetics() {
        arithmetics = Arrays.asList("add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not");
    }


    public Boolean hasMoreCommands() {
        return pointer < commands.size() - 1;
    }


    public CommandType commandType() {
        if (arithmetics.contains(thisCommand)) {
            return CommandType.C_ARITHMETIC;
        } else if (thisCommand.startsWith("push")) {
            return CommandType.C_PUSH;
        } else if (thisCommand.startsWith("pop")) {
            return CommandType.C_POP;
        }
        return null;
    }

    public void initPointer() {
        pointer = -1;
    }
}
