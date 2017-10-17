import myenum.TokenType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static myenum.StringEnum.*;
import static utils.StringUtils.*;

public class CompilationEngine {
    private BufferedWriter bw;
    private JackTokenizer jackTokenizer;
    //tag
    private final String TAG_CLASSVAR_DEC = "classVarDec";
    private final String TAG_VAR_DEC = "varDec";
    private final String TAG_TOKENS = "tokens";
    private final String TAG_CLASS = "class";
    private final String TAG_SUBROUTINE_DEC = "subroutineDec";
    private final String TAG_PARAMETERLIST = "parameterList";
    private final String TAG_SUBROUTINEBODY = "subroutineBody";
    private final String TAG_STATEMENTS = "statements";
    private final String TAG_LET = "letStatement";
    private final String TAG_IF = "ifStatement";
    private final String TAG_WHILE = "whileStatement";
    private final String TAG_DO = "doStatement";
    private final String TAG_RETURN = "returnStatement";
    private final String TAG_EXPRESSION = "expression";
    private final String TAG_TERM = "term";
    private final String TAG_EXPRESSIONLIST = "expressionList";

    public CompilationEngine(JackTokenizer jackTokenizer, String test) {
        String outputPath = jackTokenizer.getFilePath();
        try {
            outputPath = outputPath.replace(".jack", "T.xml");
            bw = new BufferedWriter(new FileWriter(outputPath));
            write(wrapBySAB(TAG_TOKENS));
            this.jackTokenizer = jackTokenizer;
            outPutTXML();
            write(wrapByEAB(TAG_TOKENS));
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompilationEngine(JackTokenizer jackTokenizer) {
        String outputPath = jackTokenizer.getFilePath();
        try {
            outputPath = outputPath.replace(".jack", ".xml");
            bw = new BufferedWriter(new FileWriter(outputPath));
            this.jackTokenizer = jackTokenizer;
            while (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                compileClass();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void outPutTXML() {
        while (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
            switch (jackTokenizer.tokenType()) {
                case KEYWORD:
                    write(wrapByKeywordTag(jackTokenizer.keyword()));
                    break;
                case SYMBOL:
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    break;
                case IDENTIFIER:
                    write(wrapByIdentifierTag(jackTokenizer.identifier()));
                    break;
                case INT_CONSTANT:
                    write(wrapByIntegerConstantTag(String.valueOf(jackTokenizer.intVal())));
                    break;
                case STRING_CONSTANT:
                    write(wrapByStringConstantTag(jackTokenizer.stringVal()));
                    break;
            }
        }
    }

    //调用完后父程序需要调用advance
    private void compileClass() {
        //'class'
        write(wrapBySAB(TAG_CLASS));
        if (!jackTokenizer.keyword().equals("class")) {
            throw new RuntimeException("lack 'class' token");
        }
        write(wrapByKeywordTag(jackTokenizer.keyword()));

        //className
        advance();
        if (jackTokenizer.tokenType() != TokenType.IDENTIFIER) {
            throw new RuntimeException("expect an identifier after 'class'");
        }
        write(wrapByIdentifierTag(jackTokenizer.identifier()));

        //'{'
        advance();
        if (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("{"))) {
            throw new RuntimeException("expect '{' after className");
        }
        write(wrapBySymbolTag(jackTokenizer.symbol()));

        advance();
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals('}')) {
            //'}'
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            write(wrapByEAB(TAG_CLASS));
            return;
        } else if (jackTokenizer.tokenType() == TokenType.SYMBOL && !jackTokenizer.symbol().equals('}')) {
            throw new RuntimeException("expect '}' to end class");
        } else if (jackTokenizer.tokenType() != TokenType.KEYWORD) {
            throw new RuntimeException("expect varDec or subroutineDec or '}' follow '{' in class");
        }

        if (jackTokenizer.isClassVarType()) {
            //varClassDec*
            while (jackTokenizer.isClassVarType()) {
                compileClassVarDec();
            }
        }

        if (jackTokenizer.isFunKeyword()) {
            //subroutineDec*
            compileSubroutine();
        }

        //'}'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        write(wrapByEAB(TAG_CLASS));
    }

    private void compileClassVarDec() {
        write(wrapBySAB(TAG_CLASSVAR_DEC));
        varDec();
        write(wrapByEAB(TAG_CLASSVAR_DEC));
    }

    private void compileVarDec() {
        write(wrapBySAB(TAG_VAR_DEC));
        varDec();
        write(wrapByEAB(TAG_VAR_DEC));
    }

    private void varDec() {
        //'static' | 'field' | 'var'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        advance();
        //primitiveType

        if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
        } else if (jackTokenizer.isPrimitiveType()) {
            write(wrapByKeywordTag(jackTokenizer.keyword()));
        } else {
            throw new RuntimeException("expect 'int' or 'char' or 'boolean' or identifier to declare variable");
        }

        //varName
        advance();
        write(wrapByIdentifierTag(jackTokenizer.identifier()));

        //(',',varName)*
        advance();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(";"))) {
            //','
            if (!jackTokenizer.symbol().equals(",")) {
                throw new RuntimeException("expect ',' to declare more variable");
            }
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //varName
            advance();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
            advance();
        }
        //';'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        advance();
    }

    private void compileSubroutine() {
        while (jackTokenizer.isFunKeyword()) {
            write(wrapBySAB(TAG_SUBROUTINE_DEC));

            //函数类型
            write(wrapByKeywordTag(jackTokenizer.keyword()));

            //函数类型
            advance();
            if (!(jackTokenizer.isPrimitiveType() || jackTokenizer.getThisToken().equals(KEYWORD_VOID) || jackTokenizer.tokenType() == TokenType.IDENTIFIER)) {
                throw new RuntimeException("expect 'void' or 'int' or 'char' or 'boolean' or className to declare subroutine return type");
            }

            if (jackTokenizer.isPrimitiveType() || jackTokenizer.getThisToken().equals(KEYWORD_VOID)) {
                write(wrapByKeywordTag(jackTokenizer.keyword()));
            } else {
                write(wrapByIdentifierTag(jackTokenizer.identifier()));
            }


            //函数名
            advance();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));

            // '('
            advance();
            if (!jackTokenizer.symbol().equals("(")) {
                throw new RuntimeException("expect '(' after subroutineName");
            }
            write(wrapBySymbolTag(jackTokenizer.symbol()));

            advance();
            //parameterlist
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                write(wrapBySAB(TAG_PARAMETERLIST));
                write(wrapByEAB(TAG_PARAMETERLIST));
            } else if (jackTokenizer.isPrimitiveType()) {
                compileParameterList();
                //返回后当前token必定是')'
            } else {
                throw new RuntimeException("expect primitive type or empty string to declare paramList");
            }
            // ')'
            write(wrapBySymbolTag(jackTokenizer.symbol()));

            //函数体(包括"{}")
            advance();
            compileSubroutineBody();
            write(wrapByEAB(TAG_SUBROUTINE_DEC));
        }

    }

    private void compileParameterList() {
        write(wrapBySAB(TAG_PARAMETERLIST));
        //primitive type
        write(wrapByKeywordTag(jackTokenizer.keyword()));

        //identifier
        advance();
        write(wrapByIdentifierTag(jackTokenizer.identifier()));
        advance();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol().equals(")"))) {
            //','
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //primitive type
            advance();
            write(wrapByKeywordTag(jackTokenizer.keyword()));

            //identifier
            advance();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
            advance();
        }
        write(wrapByEAB(TAG_PARAMETERLIST));
    }

    private void compileSubroutineBody() {
        write(wrapBySAB(TAG_SUBROUTINEBODY));

        //'{'
        if (!jackTokenizer.symbol().equals("{")) {
            throw new RuntimeException("subroutineBody should start with '{' ");
        }
        write(wrapBySymbolTag(jackTokenizer.symbol()));

        advance();
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals('}')) {
            //'}'
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            write(wrapByEAB(TAG_SUBROUTINEBODY));
            advance();
            return;
        } else if (jackTokenizer.tokenType() == TokenType.SYMBOL && !jackTokenizer.symbol().equals('}')) {
            throw new RuntimeException("expect '}' to end subroutineBody");
        }

        if (jackTokenizer.tokenType() != TokenType.KEYWORD) {
            throw new RuntimeException("expect varDec or statements or '}' follow '{' in subroutineBody");
        }

        if (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.keyword().equals(KEYWORD_VAR)) {
            //varDec*
            while ((jackTokenizer.tokenType() == TokenType.KEYWORD &&
                    jackTokenizer.keyword().equals(KEYWORD_VAR))) {
                compileVarDec();
            }
        }

        if (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.isStatement()) {
            //statements
            compileStatements();
        }
        //'}'
        write(wrapBySymbolTag(jackTokenizer.symbol()));

        write(wrapByEAB(TAG_SUBROUTINEBODY));
        advance();
    }


    //调用完父程序无需advance
    private void compileStatements() {
        write(wrapBySAB(TAG_STATEMENTS));
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("}"))) {
            switch (jackTokenizer.keyword()) {
                case KEYWORD_LET:
                    compileLet();
                    break;
                case KEYWORD_IF:
                    compileIf();
                    break;
                case KEYWORD_WHILE:
                    compileWhile();
                    break;
                case KEYWORD_DO:
                    compileDo();
                    break;
                case KEYWORD_RETURN:
                    compileReturn();
                    break;
            }
        }
        write(wrapByEAB(TAG_STATEMENTS));
    }

    //调用完父程序无需advance
    private void compileLet() {
        write(wrapBySAB(TAG_LET));
        //'let'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //varName
        advance();
        write(wrapByIdentifierTag(jackTokenizer.identifier()));
        //('['expression']')?
        advance();
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol() == "[") {
            //'['
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            advance();
            compileExpression();
            //']'
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            advance();
        } else if (jackTokenizer.tokenType() != TokenType.SYMBOL || (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                !jackTokenizer.symbol().equals("="))) {
            throw new RuntimeException("expect '=' to declare let statement");
        }

        //'='
        write(wrapBySymbolTag(jackTokenizer.symbol()));

        //expression
        advance();
        compileExpression();

        //';'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        write(wrapByEAB(TAG_LET));
        advance();
    }

    //调用完此方法后父程序无需advance
    private void compileDo() {
        write(wrapBySAB(TAG_DO));
        //'do'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //subroutineCall
        advance();
        //subroutineName | className |varName
        write(wrapByIdentifierTag(jackTokenizer.identifier()));

        //'(' | '.'
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));

        if (jackTokenizer.symbol().equals("(")) {
            //expressionList
            advance();
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                write(wrapBySAB(TAG_EXPRESSIONLIST));
                write(wrapByEAB(TAG_EXPRESSIONLIST));
            } else {
                compileExpressionList();
            }
        } else if (jackTokenizer.symbol().equals(".")) {
            //subroutineName
            advance();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
            //'('
            advance();
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //expressionList
            advance();
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                write(wrapBySAB(TAG_EXPRESSIONLIST));
                write(wrapByEAB(TAG_EXPRESSIONLIST));
            } else {
                compileExpressionList();
            }
        } else {
            throw new RuntimeException("'doStatement' compile error");
        }

        //')'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //';'
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        write(wrapByEAB(TAG_DO));
        advance();
    }

    private void compileExpressionList() {
        write(wrapBySAB(TAG_EXPRESSIONLIST));
        //expression
        compileExpression();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")"))) {
            //','
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //expression
            advance();
            compileExpression();
        }
        write(wrapByEAB(TAG_EXPRESSIONLIST));
    }

    //调用完此方法后父程序无需advance
    private void compileWhile() {
        write(wrapBySAB(TAG_WHILE));
        //'while'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //'('
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //expression
        advance();
        compileExpression();
        //')'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //'{'
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //statements
        advance();
        if (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.isStatement()) {
            compileStatements();
        }
        //'}'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        write(wrapByEAB(TAG_WHILE));
        advance();
    }

    private void compileReturn() {
        write(wrapBySAB(TAG_RETURN));
        //'return'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //expression?
        advance();
        if (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(";"))) {
            compileExpression();
        }
        //';'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        write(wrapByEAB(TAG_RETURN));
        advance();
    }

    //调用完此方法后父程序无需advance
    private void compileIf() {
        write(wrapBySAB(TAG_IF));
        //'if'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //'('
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //expression
        advance();
        compileExpression();
        //')'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //'{'
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //statements
        advance();
        if (jackTokenizer.isStatement()) {
            compileStatements();
        }
        //'}'
        write(wrapBySymbolTag(jackTokenizer.symbol()));

        advance();
        if (jackTokenizer.keyword().equals("else")) {
            //'else'
            write(wrapByKeywordTag(jackTokenizer.keyword()));
            //'{'
            advance();
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //statements
            advance();
            compileStatements();
            //'}'
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            advance();
        }
        write(wrapByEAB(TAG_IF));
    }

    //调用完此方法后父程序无需advance
    private void compileExpression() {
        write(wrapBySAB(TAG_EXPRESSION));
        compileTerm();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL &&
                (jackTokenizer.symbol().equals(";") ||
                        jackTokenizer.symbol().equals(")") ||
                        jackTokenizer.symbol().equals("]") ||
                        jackTokenizer.symbol().equals(",")))) {
            //op
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //term
            advance();
            compileTerm();
        }
        write(wrapByEAB(TAG_EXPRESSION));
    }

    private void compileTerm() {
        write(wrapBySAB(TAG_TERM));

        switch (jackTokenizer.tokenType()) {
            case KEYWORD:
                write(wrapByKeywordTag(jackTokenizer.keyword()));
                advance();
                break;
            case INT_CONSTANT:
                write(wrapByIntegerConstantTag(String.valueOf(jackTokenizer.intVal())));
                advance();
                break;
            case STRING_CONSTANT:
                write(wrapByStringConstantTag(jackTokenizer.stringVal()));
                advance();
                break;
            case SYMBOL:
                if (jackTokenizer.isunaryOp()) {
                    //'unaryOp'
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                    compileTerm();
                } else {
                    //"("
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                    compileExpression();
                    //")"
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                }
                break;
            case IDENTIFIER:
                write(wrapByIdentifierTag(jackTokenizer.identifier()));
                advance();
                if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                        jackTokenizer.symbol().equals(".") || jackTokenizer.symbol().equals("(")) {

                    write(wrapBySymbolTag(jackTokenizer.symbol()));

                    if (jackTokenizer.symbol().equals("(")) {
                        //expressionList
                        advance();
                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                            write(wrapBySAB(TAG_EXPRESSIONLIST));
                            write(wrapByEAB(TAG_EXPRESSIONLIST));
                            advance();
                        } else {
                            compileExpressionList();
                        }
                    } else if (jackTokenizer.symbol().equals(".")) {
                        //subroutineName
                        advance();
                        write(wrapByIdentifierTag(jackTokenizer.identifier()));
                        //'('
                        advance();
                        write(wrapBySymbolTag(jackTokenizer.symbol()));
                        //expressionList
                        advance();
                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                            write(wrapBySAB(TAG_EXPRESSIONLIST));
                            write(wrapByEAB(TAG_EXPRESSIONLIST));
                        } else {
                            compileExpressionList();
                        }
                        //')'
                        write(wrapBySymbolTag(jackTokenizer.symbol()));
                        advance();
                    }
                } else if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("[")) {
                    //'['expression']'
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                    compileExpression();
                    //']'
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                }
                break;
        }

        write(wrapByEAB(TAG_TERM));
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
