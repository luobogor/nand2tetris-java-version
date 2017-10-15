import myenum.TokenType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static utils.StringUtils.*;

public class CompilationEngine {
    private BufferedWriter bw;
    private JackTokenizer jackTokenizer;
    //tag
    private final String VAR_DEC = "varDec";
    private final String TOKENS = "tokens";
    private final String CLASS = "class";
    private final String SUBROUTINE_DEC = "subroutineDec";


    public CompilationEngine(){
    }

    public CompilationEngine(JackTokenizer jackTokenizer) {
        String outputPath = jackTokenizer.getFilePath();
        try {
            outputPath = outputPath.replace(".jack", ".xml");
            bw = new BufferedWriter(new FileWriter(outputPath));
            write(wrapBySAB(TOKENS));
            this.jackTokenizer = jackTokenizer;

            advance();
            while (jackTokenizer.hasMoreTokens()) {
                compileClass();
            }
            write(wrapByEAB(TOKENS));
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compileClass() {
        advance();
        write(wrapBySAB(CLASS));
        if (!jackTokenizer.keyword().equals("class")) {
            throw new RuntimeException("lack 'class' token");
        }
        write(wrapByKeyword(jackTokenizer.keyword()));

        advance();
        if (jackTokenizer.tokenType() != TokenType.IDENTIFIER) {
            throw new RuntimeException("expect an identifier after 'class'");
        }
        write(wrapByIdentifierTag(jackTokenizer.identifier()));

        advance();
        if (jackTokenizer.tokenType() != TokenType.SYMBOL) {
            throw new RuntimeException("expect a symbol after className");
        } else if ( !jackTokenizer.symbol().equals("{")) {
            throw new RuntimeException("expect '{' after className");
        }
        write(wrapByIdentifierTag(jackTokenizer.identifier()));

        compileVarDec();
        compileSubroutineDec();
        wrapByEAB(CLASS);
    }

    private void compileVarDec() {
        write(wrapBySAB(VAR_DEC));
        advance();
        if (!jackTokenizer.keyword().equals("var")) {
            throw new RuntimeException("expect 'var' to declare variable");
        }
        write(wrapByKeyword(jackTokenizer.keyword()));

        advance();
        if (!jackTokenizer.keyword().equals("int") &&
                !jackTokenizer.keyword().equals("char") &&
                !jackTokenizer.keyword().equals("boolean")) {
            throw new RuntimeException("expect 'int' or 'char' or 'boolean' to declare variable");
        }
        write(wrapByKeyword(jackTokenizer.keyword()));

        advance();
        //防错机制有待补充
        while (!jackTokenizer.symbol().equals(";")) {
            if (!jackTokenizer.symbol().equals(",")) {
                throw new RuntimeException("expect ',' to declare more variable");
            }
            write(wrapBySymbolTag(jackTokenizer.symbol()));

            advance();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
            advance();
        }
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        write(wrapByEAB(VAR_DEC));
    }

    private void compileSubroutineDec() {

    }
    private void advance() {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
        } else {
            throw new RuntimeException("no more token");
        }
    }

    private void write(String str) {
        try {
            bw.write(str);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
