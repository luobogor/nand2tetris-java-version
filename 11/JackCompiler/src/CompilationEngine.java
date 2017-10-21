import domain.Kind;
import domain.TokenType;
import domain.VMEnum;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static domain.StringEnum.*;
import static utils.StringUtils.*;

public class CompilationEngine {
    private BufferedWriter bw;
    private JackTokenizer jackTokenizer;
    private SymbolTable symbolTable;
    private VMWriter vmWriter;
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
    //flag
    private final String LABEL_FLAG = "LABEL_FLAG_";
    private int labelCounter;


    public CompilationEngine(JackTokenizer jackTokenizer) {
        String outputPath = jackTokenizer.getFilePath();
        try {

            outputPath = outputPath.replace(".jack", ".xml");
            bw = new BufferedWriter(new FileWriter(outputPath));

            labelCounter = 0;
            vmWriter = new VMWriter(jackTokenizer.getFilePath());
            this.jackTokenizer = jackTokenizer;

            while (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                compileClass();
            }
            bw.close();

            vmWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //调用完后父程序需要调用advance
    private void compileClass() {
        //创建符号表
        symbolTable = new SymbolTable();
        symbolTable.localAtClassScope();
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
            symbolTable.localAtSubroutineScope();
            compileSubroutine();
            symbolTable.localAtClassScope();
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
        String name;
        String type;
        String kind;
        //'static' | 'field' | 'var'
        kind = jackTokenizer.keyword();
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        advance();
        //primitiveType
        if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            type = jackTokenizer.identifier();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
        } else if (jackTokenizer.isPrimitiveType()) {
            type = jackTokenizer.keyword();
            write(wrapByKeywordTag(jackTokenizer.keyword()));
        } else {
            throw new RuntimeException("expect 'int' or 'char' or 'boolean' or identifier to declare variable");
        }

        //varName
        advance();
        write(wrapByIdentifierTag(jackTokenizer.identifier()));
        name = jackTokenizer.identifier();

        //add to symbolTable
        symbolTable.define(name, type, kind);

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

            //add to symbolTable
            name = jackTokenizer.identifier();
            symbolTable.define(name, type, kind);

            advance();
        }
        //';'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        advance();
    }

    private void compileSubroutine() {
        while (jackTokenizer.isFunKeyword()) {
            symbolTable.startSubroutine();
            write(wrapBySAB(TAG_SUBROUTINE_DEC));

            //'constructor' | 'method' | 'function'
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
            String funName = jackTokenizer.identifier();

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
            compileSubroutineBody(funName);
            write(wrapByEAB(TAG_SUBROUTINE_DEC));
        }

    }

    private void compileParameterList() {
        write(wrapBySAB(TAG_PARAMETERLIST));
        //primitive type
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        String type = jackTokenizer.keyword();

        //identifier
        advance();
        write(wrapByIdentifierTag(jackTokenizer.identifier()));
        /**
         * add args to symbolTable
         */
        symbolTable.define(jackTokenizer.identifier(), type, "arg");

        advance();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol().equals(")"))) {
            //','
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //primitive type
            advance();
            write(wrapByKeywordTag(jackTokenizer.keyword()));
            String type2 = jackTokenizer.keyword();

            //identifier
            advance();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
            /**
             * add args to symbolTable
             */
            symbolTable.define(jackTokenizer.identifier(), type2, "arg");

            advance();
        }
        write(wrapByEAB(TAG_PARAMETERLIST));
    }

    private void compileSubroutineBody(String funName) {
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
        /**
         * function xx.xx nArgs
         * */
        vmWriter.writeFunction(funName, symbolTable.varCount(Kind.VAR));

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
        String name = jackTokenizer.identifier();
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

        /**
         * accept the value after computed
         * */
        switch (symbolTable.kindOf(name)) {
            case STATIC:
                break;
            case FIELD:
                break;
            case ARG:
                vmWriter.writePop(VMEnum.ARG, symbolTable.indexOf(name));
                break;
            case VAR:
                vmWriter.writePop(VMEnum.LOCAL, symbolTable.indexOf(name));
                break;
            case NONE:
                break;
        }
    }

    //调用完此方法后父程序无需advance
    private void compileDo() {
        write(wrapBySAB(TAG_DO));
        //'do'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //subroutineCall
        advance();
        //subroutineName | className | varName
        write(wrapByIdentifierTag(jackTokenizer.identifier()));
        String nameLv1 = jackTokenizer.identifier();

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

            String nameLv2 = jackTokenizer.identifier();

            //传递this
            if (symbolTable.kindOf(nameLv1) != Kind.NONE) {
                vmWriter.writePush(VMEnum.ARG, 0);
            }

            //'('
            advance();
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //expressionList
            advance();
            int nArgs = 0;
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                write(wrapBySAB(TAG_EXPRESSIONLIST));
                write(wrapByEAB(TAG_EXPRESSIONLIST));
            } else {
                nArgs = compileExpressionList();
            }

            //call xxx.xxx nArgs
            if (symbolTable.kindOf(nameLv1) == Kind.NONE) {
                //系统API | 当前文件类名 | 外部类名 不需要传递this
                vmWriter.writeCall(nameLv1 + "." + nameLv2, nArgs);
            } else {
                vmWriter.writeCall(nameLv1 + "." + nameLv2, nArgs + 1);
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

        //after finished doStatement discard the return value
        vmWriter.writePop(VMEnum.TEMP, 0);
    }

    private int compileExpressionList() {
        int nArgs = 1;

        write(wrapBySAB(TAG_EXPRESSIONLIST));
        //expression
        compileExpression();
        while (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")"))) {
            //','
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //expression
            advance();
            compileExpression();
            nArgs++;
        }
        write(wrapByEAB(TAG_EXPRESSIONLIST));
        return nArgs;
    }

    //调用完此方法后父程序无需advance
    private void compileWhile() {
        String label1 = LABEL_FLAG + labelCounter++;
        String label2 = LABEL_FLAG + labelCounter++;

        /**
         * （label1)
         * */
        vmWriter.writeLabel(label1);

        write(wrapBySAB(TAG_WHILE));
        //'while'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //'('
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        //expression
        advance();
        compileExpression();
        /**
         * computed(expression)
         * not
         * if-goto label2
         * */
        vmWriter.writeArithmetic("~");
        vmWriter.writeIf(label2);

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
        /**
         * computed(statement)
         * goto label1
         * */
        vmWriter.writeGoto(label1);

        //'}'
        write(wrapBySymbolTag(jackTokenizer.symbol()));
        write(wrapByEAB(TAG_WHILE));
        advance();

        /**
         * （label2)
         * */
        vmWriter.writeLabel(label2);
    }

    private void compileReturn() {
        write(wrapBySAB(TAG_RETURN));
        //'return'
        write(wrapByKeywordTag(jackTokenizer.keyword()));
        //expression?
        advance();
        if (!(jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(";"))) {
            compileExpression();
        } else {
            /**
             * if "return;",have to return a value
             * */
            vmWriter.writePush(VMEnum.CONST, 0);
        }

        /**
         * return;
         */
        vmWriter.writeReturn();
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

        /**
         not
         if-goto L1
         **/
        vmWriter.writeArithmetic("~");
        String label1 = LABEL_FLAG + labelCounter++;
        vmWriter.writeIf(label1);

        //'{'
        advance();
        write(wrapBySymbolTag(jackTokenizer.symbol()));


        //statements
        advance();
        if (jackTokenizer.isStatement()) {
            compileStatements();
        }

        /**
         *
         *goto L2
         * **/
        String label2 = LABEL_FLAG + labelCounter++;
        vmWriter.writeGoto(label2);

        //'}'
        write(wrapBySymbolTag(jackTokenizer.symbol()));


        advance();
        boolean isElse = false;
        if (jackTokenizer.keyword().equals("else")) {
            isElse = true;
            //'else'
            write(wrapByKeywordTag(jackTokenizer.keyword()));
            //'{'
            advance();
            write(wrapBySymbolTag(jackTokenizer.symbol()));

            /**
             * （label1）
             * */
            vmWriter.writeLabel(label1);

            //statements
            advance();
            compileStatements();
            //'}'
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            advance();
        }


        if (!isElse) {
            /**
             * （label1）
             * */
            vmWriter.writeLabel(label1);
        }
        write(wrapByEAB(TAG_IF));

        /**
         * （label2）
         * */
        vmWriter.writeLabel(label2);
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
            String op = jackTokenizer.symbol();
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            //term
            advance();
            compileTerm();

            /**
             *  RPN add | sub | .........
             */
            if (op.equals("*")) {
                vmWriter.writeCall("Math.multiply", 2);
            } else if (op.equals("/")) {
                vmWriter.writeCall("Math.divide", 2);
            } else {
                switch (op) {
                    case "&gt;":
                        vmWriter.writeArithmetic(">");
                        break;
                    case "&lt;":
                        vmWriter.writeArithmetic("<");
                        break;
                    case "&amp;":
                        vmWriter.writeArithmetic("&");
                        break;
                    default:
                        vmWriter.writeArithmetic(op);
                        break;
                }
            }
        }
        write(wrapByEAB(TAG_EXPRESSION));
    }

    private void compileTerm() {
        write(wrapBySAB(TAG_TERM));

        switch (jackTokenizer.tokenType()) {
            case KEYWORD:
                //to be precise it should be call keywordConstant: 'true' | 'false' | 'this' | 'null'
                write(wrapByKeywordTag(jackTokenizer.keyword()));
                switch (jackTokenizer.keyword()) {
                    case "null":
                    case "false":
                        /**
                         * 'null','false' map to constant 0
                         * */
                        vmWriter.writePush(VMEnum.CONST, 0);
                        break;
                    case "true":
                        /**
                         * 'true' map to constant -1
                         * */
                        vmWriter.writePush(VMEnum.CONST, 1);
                        vmWriter.writeArithmetic("_");
                        break;
                    case "this":
                        break;
                }
                advance();
                break;
            case INT_CONSTANT:
                write(wrapByIntegerConstantTag(String.valueOf(jackTokenizer.intVal())));
                //push constant intVal
                vmWriter.writePush(VMEnum.CONST, jackTokenizer.intVal());
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
                    String unaryOp = jackTokenizer.symbol();
                    advance();
                    compileTerm();
                    /**
                     * Right Polish Notation
                     * */
                    if (unaryOp.equals("-")) {
                        vmWriter.writeArithmetic("_");
                    } else {
                        vmWriter.writeArithmetic(unaryOp);
                    }
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
                String nameLv1 = jackTokenizer.identifier();
                write(wrapByIdentifierTag(jackTokenizer.identifier()));
                boolean onlyIsVarName = true;
                advance();
                if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
                        jackTokenizer.symbol().equals(".") || jackTokenizer.symbol().equals("(")) {
                    onlyIsVarName = false;
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
                        String nameLv2 = jackTokenizer.identifier();

                        //'('
                        advance();
                        write(wrapBySymbolTag(jackTokenizer.symbol()));
                        //expressionList
                        advance();

                        int nArgs = 0;
                        //传递this
                        if (symbolTable.kindOf(nameLv1) != Kind.NONE) {
                            vmWriter.writePush(VMEnum.ARG, 0);
                        }

                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                            write(wrapBySAB(TAG_EXPRESSIONLIST));
                            write(wrapByEAB(TAG_EXPRESSIONLIST));
                        } else {
                            nArgs = compileExpressionList();
                        }
                        //')'
                        write(wrapBySymbolTag(jackTokenizer.symbol()));
                        advance();

                        //call xxx.xxx nArgs
                        if (symbolTable.kindOf(nameLv1) == Kind.NONE) {
                            //系统API | 当前文件类名 | 外部类名 不需要传递this
                            vmWriter.writeCall(nameLv1 + "." + nameLv2, nArgs);
                        } else {
                            vmWriter.writeCall(nameLv1 + "." + nameLv2, nArgs + 1);
                        }
                    }
                } else if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("[")) {
                    onlyIsVarName = false;
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                    compileExpression();
                    //']'
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                }

                /**
                 *if term is a varName just push it in stack
                 **/
                if (onlyIsVarName) {
                    switch (symbolTable.kindOf(nameLv1)) {
                        case STATIC:
                            break;
                        case FIELD:
                            break;
                        case ARG:
                            vmWriter.writePush(VMEnum.ARG, symbolTable.indexOf(nameLv1));
                            break;
                        case VAR:
                            vmWriter.writePush(VMEnum.LOCAL, symbolTable.indexOf(nameLv1));
                            break;
                        case NONE:
                            break;
                    }
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