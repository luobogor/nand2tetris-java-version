import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Parser {
    enum CommandType {
        A_COMMAND,
        C_COMMAND,
        L_COMMAND
    }

    private List<String> commands;
    private int pointer;
    private int LCommandCounter;
    private String thisCommand;

    public Parser() {
    }

    public Parser(String filePath) {
        LCommandCounter = 0;
        initPointer();
        commands = new ArrayList();
        String line;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            line = in.readLine();
            while (line != null) {
                line = line.replaceAll(" ", "");
                System.out.println(line);
                if (line.equals("") || line.charAt(0) == '/' && line.charAt(1) == '/') {
                    line = in.readLine();
                    continue;
                }
                String[] splitRes = line.split("//");
                commands.add(splitRes[0]);
                line = in.readLine();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(commands);
    }

    public Boolean hasMoreCommands() {
        return pointer < commands.size() - 1;
    }

    public void addLCommand() {
        LCommandCounter++;
    }

    public int getLCommandCounter() {
        return LCommandCounter;
    }

    public void advance() {
        pointer++;
        this.thisCommand = commands.get(pointer);
    }

    public CommandType commandType() {
        if (thisCommand.startsWith("@")) {
            return CommandType.A_COMMAND;
        } else if (thisCommand.startsWith("(")) {
            return CommandType.L_COMMAND;
        } else {
            return CommandType.C_COMMAND;
        }
    }

    public String symbol() {
        try {
            if (commandType() != CommandType.A_COMMAND && commandType() != CommandType.L_COMMAND) {
                throw new RuntimeException("当前指令不是A指令且不是L指令，调用symbol方法失败");
            }
            if (commandType() == CommandType.A_COMMAND) {
                return thisCommand.substring(1);
            } else {
                return thisCommand.replace("(", "").replace(")","");
            }
        } catch (RuntimeException r) {
            System.err.println(r.getMessage());
            return null;
        }
    }

    public static boolean isVar(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return !pattern.matcher(str).matches();
    }

    public String dest() {
        try {
            if (commandType() != CommandType.C_COMMAND) {
                throw new RuntimeException("当前指令不是C指令，调用dest方法失败");
            }
            if (thisCommand.contains("=")) {
                return thisCommand.split("=")[0];
            }
            return "null";
        } catch (RuntimeException r) {
            System.err.println(r.getMessage());
            return null;
        }
    }

    public String comp() {
        try {
            if (commandType() != CommandType.C_COMMAND) {
                throw new RuntimeException("当前指令不是C指令，调用jump方法失败");
            }

            if (thisCommand.contains("=")) {
                return thisCommand.split("=")[1];
            } else if (thisCommand.contains(";")) {
                return thisCommand.split(";")[0];
            } else if (thisCommand.contains("=") && thisCommand.contains(";")) {
                int semicolonIdx = thisCommand.indexOf(";");
                int equalsignIdx = thisCommand.indexOf("=");
                return thisCommand.substring(equalsignIdx + 1, semicolonIdx);
            } else {
                return thisCommand;
            }
        } catch (RuntimeException r) {
            System.err.println(r.getMessage());
            return null;
        }
    }

    public String jump() {
        try {
            if (commandType() != CommandType.C_COMMAND) {
                throw new RuntimeException("当前指令不是C指令，调用jump方法失败");
            }
            if (thisCommand.contains(";")) {
                return thisCommand.split(";")[1];
            }
            return "null";
        } catch (RuntimeException r) {
            System.err.println(r.getMessage());
            return null;
        }
    }


    public void initPointer() {
        pointer = -1;
    }

    public int getPointer() {
        return pointer;
    }
}
