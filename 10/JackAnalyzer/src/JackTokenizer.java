import myenum.TokenType;
import utils.NumberUtils;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static myenum.StringEnum.keywords;
import static myenum.StringEnum.symbols;
import static myenum.TokenType.*;

public class JackTokenizer {

    private List<String> tokens;
    private int pointer;
    private String thisToken;
//    private String fileName;

    public JackTokenizer() {
    }

    public JackTokenizer(String filePath) {
        initPointer();
        tokens = new ArrayList();
        String line;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
//            File file = new File(filePath);
//            String tempFileName = file.getName();
//            this.fileName = tempFileName.substring(0, tempFileName.lastIndexOf('.'));
            line = in.readLine();
            boolean isMuilLineNeglect = false;
            while (line != null) {
                line = line.trim();
                System.out.println(line);
                if (line.equals("") || isMuilLineNeglect || line.startsWith("//")) {
                    line = in.readLine();
                    continue;
                }

                if (line.startsWith("/*") && !line.endsWith("*/")) {
                    isMuilLineNeglect = true;
                    line = in.readLine();
                    continue;
                } else if (!line.startsWith("/*") && line.endsWith("*/")) {
                    isMuilLineNeglect = false;
                    line = in.readLine();
                    continue;
                } else if (line.startsWith("/*") && line.endsWith("*/")) {
                    line = in.readLine();
                    continue;
                }
                String[] statement = line.split("//")[0].trim().split(" ");
                for (int i = 0; i < statement.length; i++) {
                    List<String> thisLineTokes = new ArrayList<>();
                    splitToToken(statement[i],thisLineTokes);
                    tokens.addAll(thisLineTokes);
                }
                line = in.readLine();
            }
            System.out.println();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void splitToToken(String word,List<String> tokens) {
        if (!symbols.contains(word)) {
            tokens.add(word);
            return;
        }
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            if (word.contains(symbol)) {
                int symbolIdx = word.indexOf(symbol);
                splitToToken(word.substring(symbolIdx),tokens);
                tokens.add(symbol);
                splitToToken(word.substring(symbolIdx + 1, word.length()),tokens);
                return;
            }
        }
    }

    public void advance() {
        pointer++;
        this.thisToken = tokens.get(pointer);
    }



    public Boolean hasMoreTokens() {
        return pointer < tokens.size() - 1;
    }


    public TokenType tokenType() {
        if (keywords.contains(thisToken)) {
            return KEYWORD;
        } else if (symbols.contains(thisToken)) {
            return SYBOL;
        } else if (NumberUtils.isNumeric(thisToken)) {
            return INT_CONSTANT;
        } else if (thisToken.startsWith("\"") && thisToken.endsWith("\"")) {
            return STRING_CONSTANT;
        } else {
            if (Character.isDigit(thisToken.charAt(0))) {
                throw new RuntimeException(thisToken + " syntax error");
            }
            return IDENTIFIER;
        }
    }

    public String keyword() {
        return getThisToken();
    }

    public String symbol() {
        return getThisToken();
    }

    public String identifier() {
        return getThisToken();
    }

    public int intVal() {
        return Integer.parseInt(getThisToken());
    }

    public String stringVal() {
        return getThisToken().replace("\"", "");
    }

    public void initPointer() {
        pointer = -1;
    }

    public String getThisToken() {
        return thisToken;
    }
//
//    public String getFileName() {
//        return fileName;
//    }

}
