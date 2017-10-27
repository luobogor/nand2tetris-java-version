import domain.Kind;
import domain.TokenType;
import domain.VMEnum;
import sun.misc.VM;

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
    //
    private String thisFunName;
    private String thisFunType;
    private String thisClassName;
    //

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
        this.thisClassName = jackTokenizer.identifier();

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
            symbolTable.localAtSubroutineScope();

            write(wrapBySAB(TAG_SUBROUTINE_DEC));

            //'constructor' | 'method' | 'function'
            write(wrapByKeywordTag(jackTokenizer.keyword()));
            this.thisFunType = jackTokenizer.keyword();

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
            this.thisFunName = jackTokenizer.identifier();

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
            } else if (jackTokenizer.isPrimitiveType() || jackTokenizer.tokenType()==TokenType.IDENTIFIER) {
                compileParameterList();
                //返回后当前token必定是')'
            } else {
                throw new RuntimeException("expect primitive type or identify or empty string to declare paramList");
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
        String type;
        //arg type
        if (jackTokenizer.tokenType() == TokenType.KEYWORD) {
            write(wrapByKeywordTag(jackTokenizer.keyword()));
            type = jackTokenizer.keyword();
        } else {
            write(wrapByIdentifierTag(jackTokenizer.identifier()));
            type = jackTokenizer.identifier();
        }


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
        /**
         * function xx.xx nArgs
         * */
        vmWriter.writeFunction(this.thisFunName, symbolTable.varCount(Kind.VAR));
        if (this.thisFunType.equals(KEYWORD_CONSTRUCTOR)) {
            /**
             * constructor allocate memory to new object
             * */
            symbolTable.localAtClassScope();

            vmWriter.writePush(VMEnum.CONST, symbolTable.varCount(Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMEnum.POINTER, 0);

            symbolTable.localAtSubroutineScope();
        } else if (this.thisFunType.equals(KEYWORD_METHOD)) {
            /**
             * THIS = arg 0
             * */
            vmWriter.writePush(VMEnum.ARG, 0);
            vmWriter.writePop(VMEnum.POINTER, 0);

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
        String name = jackTokenizer.identifier();

        //('['expression']')?
        boolean isArray = false;
        advance();
        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol() == "[") {
            isArray = true;
            /**
             * push arr
             * */
            pushVar(name);

            //'['
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            advance();
            /**
             * push idx
             * */
            compileExpression();
            //']'
            write(wrapBySymbolTag(jackTokenizer.symbol()));
            advance();
            /***
             * add
             * */
            vmWriter.writeArithmetic("+");
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
            case ARG:
                if (!isArray) {
                    vmWriter.writePop(VMEnum.ARG, symbolTable.indexOf(name));
                } else {
                    handleArray();
                }
                break;
            case VAR:
                if (!isArray) {
                    vmWriter.writePop(VMEnum.LOCAL, symbolTable.indexOf(name));
                } else {
                    handleArray();
                }
                break;
            case NONE:
                //search at classLevel
                symbolTable.localAtClassScope();
                switch (symbolTable.kindOf(name)) {
                    case STATIC:
                        vmWriter.writePop(VMEnum.STATIC, symbolTable.indexOf(name));
                        break;
                    case FIELD:
                        if (!isArray) {
                            vmWriter.writePop(VMEnum.THIS, symbolTable.indexOf(name));
                        } else {
                            handleArray();
                        }
                        break;
                    case NONE:
                        throw new RuntimeException("unknown pop kind");
                }
                symbolTable.localAtSubroutineScope();
                break;
        }

    }

    private void pushVar(String name) {
        if (symbolTable.kindOf(name) != Kind.NONE) {
            if (symbolTable.kindOf(name)==Kind.ARG) {
                vmWriter.writePush(VMEnum.ARG, symbolTable.indexOf(name));
            } else if (symbolTable.kindOf(name) == Kind.VAR) {
                vmWriter.writePush(VMEnum.LOCAL, symbolTable.indexOf(name));
            }
        } else {
            symbolTable.localAtClassScope();
            if (symbolTable.kindOf(name) != Kind.NONE) {
                vmWriter.writePush(VMEnum.THIS, symbolTable.indexOf(name));
            }
            symbolTable.localAtSubroutineScope();
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
            //invoke function | method
            if (this.thisFunType.equals(KEYWORD_CONSTRUCTOR)) {
                //case: constructor invoke method
                vmWriter.writePush(VMEnum.POINTER, 0);
            } else if (thisFunType.equals(KEYWORD_METHOD)) {
                //case: method invoke method
                /**
                 * push obj
                 * */
                vmWriter.writePush(VMEnum.ARG, 0);
            }

            int nArgs = 0;
            //expressionList
            advance();
            if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                write(wrapBySAB(TAG_EXPRESSIONLIST));
                write(wrapByEAB(TAG_EXPRESSIONLIST));
            } else {
                nArgs = compileExpressionList();
            }

            /**
             *call xxx nArgs
             * */
            if (!this.thisFunType.equals(KEYWORD_FUNCTION)) {
                vmWriter.writeCall(this.thisClassName + "." + nameLv1, nArgs + 1);
            }
        } else if (jackTokenizer.symbol().equals(".")) {
            //subroutineName
            advance();
            write(wrapByIdentifierTag(jackTokenizer.identifier()));

            String nameLv2 = jackTokenizer.identifier();

            /**
             * push obj
             * */
            pushObj(nameLv1);

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
            /**
             *call xxx.xxx nArgs
             * */
            call(nameLv1, nameLv2, nArgs);
            //setBreakPoint();
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
        if (jackTokenizer.tokenType()==TokenType.KEYWORD && jackTokenizer.keyword().equals("else")) {
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
                        vmWriter.writePush(VMEnum.POINTER, 0);
                        break;
                }
                advance();
                break;
            case INT_CONSTANT:
                write(wrapByIntegerConstantTag(String.valueOf(jackTokenizer.intVal())));
                /**
                 * push constant intVal
                 * */
                vmWriter.writePush(VMEnum.CONST, jackTokenizer.intVal());
                advance();
                break;
            case STRING_CONSTANT:
                /**
                 *handel string
                 * */
                write(wrapByStringConstantTag(jackTokenizer.stringVal()));
                String stringConstant = jackTokenizer.stringVal();

                vmWriter.writePush(VMEnum.CONST, stringConstant.length());
                vmWriter.writeCall("String.new", 1);
                for (int i = 0; i < stringConstant.length(); i++) {
                    char c = stringConstant.charAt(i);
                    vmWriter.writePush(VMEnum.CONST,c);
                    vmWriter.writeCall("String.appendChar", 2);
                }

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
                        //subroutineCall
                        //eg xxx()
                        //invoke function | method
                        if (this.thisFunType.equals(KEYWORD_CONSTRUCTOR)) {
                            //case: constructor invoke method
                            vmWriter.writePush(VMEnum.POINTER, 0);
                        } else if (thisFunType.equals(KEYWORD_METHOD)) {
                            //case: method invoke method
                            /**
                             * push obj
                             * */
                            vmWriter.writePush(VMEnum.ARG, 0);
                        }

                        int nArgs = 0;
                        //expressionList
                        advance();
                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                            write(wrapBySAB(TAG_EXPRESSIONLIST));
                            write(wrapByEAB(TAG_EXPRESSIONLIST));
                            advance();
                        } else {
                            nArgs = compileExpressionList();
                        }

                        /**
                         *call xxx nArgs
                         * */
                        if (!this.thisFunType.equals(KEYWORD_FUNCTION)) {
                            vmWriter.writeCall(this.thisClassName + "." + nameLv1, nArgs + 1);
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
                        /**
                         * push obj
                         * */
                        pushObj(nameLv1);

                        if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals(")")) {
                            write(wrapBySAB(TAG_EXPRESSIONLIST));
                            write(wrapByEAB(TAG_EXPRESSIONLIST));
                        } else {
                            nArgs = compileExpressionList();
                        }
                        //')'
                        write(wrapBySymbolTag(jackTokenizer.symbol()));
                        advance();

                        /**
                         *call xxx.xxx nArgs
                         * */
                        call(nameLv1, nameLv2, nArgs);
                    }
                } else if (jackTokenizer.tokenType() == TokenType.SYMBOL && jackTokenizer.symbol().equals("[")) {
                    onlyIsVarName = false;
                    /**
                     * push arr
                     * */
                    pushVar(nameLv1);

                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();
                    /**
                     * push idx
                     * */
                    compileExpression();
                    //']'
                    write(wrapBySymbolTag(jackTokenizer.symbol()));
                    advance();

                    /***
                     * add
                     * */
                    vmWriter.writeArithmetic("+");
                    /**
                     * pop pointer 1
                     * */
                    vmWriter.writePop(VMEnum.POINTER, 1);
                    /**
                     * push that 0
                     * */
                    vmWriter.writePush(VMEnum.THAT, 0);
                }

                /**
                 *if term is a varName just push it in stack
                 **/
                if (onlyIsVarName) {
                    //search at subroutineLevel
                    switch (symbolTable.kindOf(nameLv1)) {
                        case ARG:
                            vmWriter.writePush(VMEnum.ARG, symbolTable.indexOf(nameLv1));
                            break;
                        case VAR:
                            vmWriter.writePush(VMEnum.LOCAL, symbolTable.indexOf(nameLv1));
                            break;
                        case NONE:
                            //search at classLevel
                            symbolTable.localAtClassScope();
                            switch (symbolTable.kindOf(nameLv1)) {
                                case STATIC:
                                    vmWriter.writePush(VMEnum.STATIC, symbolTable.indexOf(nameLv1));
                                    break;
                                case FIELD:
                                    vmWriter.writePush(VMEnum.THIS, symbolTable.indexOf(nameLv1));
                                    break;
                                case NONE:
                                    throw new RuntimeException("unknown push kind");
                            }
                            symbolTable.localAtSubroutineScope();
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

    private void call(String nameLv1,String nameLv2,int nArgs) {
        /**
         *call xxx.xxx nArgs
         * */
        if (symbolTable.kindOf(nameLv1) != Kind.NONE) {
            vmWriter.writeCall(symbolTable.typeOf(nameLv1) + "." + nameLv2, nArgs + 1);
        } else {
            symbolTable.localAtClassScope();
            if (symbolTable.kindOf(nameLv1) != Kind.NONE) {
                //call classLevel variable.method
                vmWriter.writeCall(symbolTable.typeOf(nameLv1) + "." + nameLv2, nArgs + 1);
            } else {
                //系统API | 当前文件类名 | 外部类名 不需要传递this
                vmWriter.writeCall(nameLv1 + "." + nameLv2, nArgs);
            }
            symbolTable.localAtSubroutineScope();
        }
    }

    private void setBreakPoint() {
        vmWriter.writePush(VMEnum.CONST, 1);
        vmWriter.writePop(VMEnum.TEMP,7);
        vmWriter.writePush(VMEnum.CONST, 0);
        vmWriter.writePop(VMEnum.TEMP,7);
    }

    private void pushObj(String nameLv1) {
        if (symbolTable.kindOf(nameLv1) != Kind.NONE) {
            vmWriter.writePush(VMEnum.LOCAL, symbolTable.indexOf(nameLv1));
        } else {
            symbolTable.localAtClassScope();
            if (symbolTable.kindOf(nameLv1) != Kind.NONE) {
                //classLevel variable
                vmWriter.writePush(VMEnum.THIS, symbolTable.indexOf(nameLv1));
            }
            symbolTable.localAtSubroutineScope();
        }
    }

    private void handleArray() {
        /**handle arr**/
        vmWriter.writePop(VMEnum.TEMP, 0);
        vmWriter.writePop(VMEnum.POINTER,1);
        vmWriter.writePush(VMEnum.TEMP,0);
        vmWriter.writePop(VMEnum.THAT, 0);
    }
}
