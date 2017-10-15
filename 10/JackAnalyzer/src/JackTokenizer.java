import myenum.TokenType;
import utils.NumberUtils;
import utils.StringUtils;

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
    private String fileName;
    private String filePath;
    private final String BLANK = " ";

    public JackTokenizer() {
    }

    public JackTokenizer(String filePath) {
        initPointer();
        tokens = new ArrayList();
        String line;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            this.filePath = filePath;
            File file = new File(filePath);
            String tempFileName = file.getName();
            this.fileName = tempFileName.substring(0, tempFileName.lastIndexOf('.'));
            line = in.readLine();
            boolean isMuilLineNeglect = false;
            while (line != null) {
                line = line.trim();
                System.out.println(line);

                //删除注释
                if (line.startsWith("/*") && !line.endsWith("*/")) {
                    isMuilLineNeglect = true;
                    line = in.readLine();
                    continue;
                } else if (line.endsWith("*/") || line.startsWith("*/")) {
                    isMuilLineNeglect = false;
                    line = in.readLine();
                    continue;
                } else if (line.startsWith("/*") && line.endsWith("*/")) {
                    line = in.readLine();
                    continue;
                }

                if (line.equals("") || isMuilLineNeglect || line.startsWith("//")) {
                    line = in.readLine();
                    continue;
                }
                //

                String[] segment = line.split("//")[0].trim().split("\"");
                boolean even = true;
                for (int i = 0; i < segment.length; i++) {
                    String statement = segment[i];
                    if (even) {
                        String[] words = statement.split(BLANK);
                        for (int j = 0; j <words.length; j++) {
                            List<String> thisLineTokes = new ArrayList<>();
                            splitToToken(words[j], thisLineTokes);
                            tokens.addAll(thisLineTokes);
                        }
                        even = false;
                    } else {
                        tokens.add(StringUtils.wrapByDoubleQuotation(statement));
                        even = true;
                    }
                }
                line = in.readLine();
            }
            System.out.println();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void splitToToken(String word, List<String> tokens) {
        if (word == null || word.isEmpty()) {
            return;
        }
        if (word.length() == 1) {
            tokens.add(word);
            return;
        }
        boolean isContainSymbol = false;

        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            if (word.contains(symbol)) {
                isContainSymbol = true;
                int symbolIdx = word.indexOf(symbol);
                splitToToken(word.substring(0, symbolIdx), tokens);
                tokens.add(symbol);
                if (symbolIdx + 1 < word.length()) {
                    splitToToken(word.substring(symbolIdx + 1, word.length()), tokens);
                }
                break;
            }
        }
        if (!isContainSymbol) {
            tokens.add(word);
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
            return SYMBOL;
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
        if (tokenType()!=KEYWORD) {
            throw new RuntimeException("only when type of token is 'KEYWORD' can keyword()");
        }
        return getThisToken();
    }

    public String symbol() {
        if (tokenType()!=SYMBOL) {
            throw new RuntimeException("only when type of token is 'SYMBOL' can symbol()");
        }
        String token = thisToken;
        switch (thisToken) {
            case ">":
                token = "&gt;";
                break;
            case "<":
                token = "&lt;";
                break;
            case "&":
                token = "&amp;";
                break;
        }
        return token;
    }

    public String identifier() {
        if (tokenType()!=IDENTIFIER) {
            throw new RuntimeException("only when type of token is 'IDENTIFIER' can identifier()");
        }
        return getThisToken();
    }

    public int intVal() {
        if (tokenType()!=INT_CONSTANT) {
            throw new RuntimeException("only when type of token is 'INT_CONSTANT' can intVal()");
        }
        return Integer.parseInt(getThisToken());
    }

    public String stringVal() {
        if (tokenType()!=STRING_CONSTANT) {
            throw new RuntimeException("only when type of token is 'STRING_CONSTANT' can stringVal()");
        }
        return getThisToken().replace("\"", "");
    }

    public void initPointer() {
        pointer = -1;
    }

    public String getThisToken() {
        return thisToken;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }
}
