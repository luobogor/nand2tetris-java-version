package domain;

import java.util.Arrays;
import java.util.List;

public class StringEnum {
    public static List<String> keywords;
    public static List<String> symbols;

    public static final String KEYWORD_CLASS = "class";
    public static final String KEYWORD_CONSTRUCTOR = "constructor";
    public static final String KEYWORD_FUNCTION = "function";
    public static final String KEYWORD_FIELD = "field";
    public static final String KEYWORD_METHOD = "method";
    public static final String KEYWORD_STATIC = "static";
    public static final String KEYWORD_VAR = "var";
    public static final String KEYWORD_INT = "int";
    public static final String KEYWORD_CHAR = "char";
    public static final String KEYWORD_BOOLEAN = "boolean";
    public static final String KEYWORD_VOID = "void";
    public static final String KEYWORD_TRUE = "true";
    public static final String KEYWORD_FALSE = "false";
    public static final String KEYWORD_NULL = "null";
    public static final String KEYWORD_THIS = "this";
    public static final String KEYWORD_LET = "let";
    public static final String KEYWORD_DO = "do";
    public static final String KEYWORD_IF = "if";
    public static final String KEYWORD_ELSE = "else";
    public static final String KEYWORD_WHILE = "while";
    public static final String KEYWORD_RETURN = "return";

    static {
        keywords = Arrays.asList(KEYWORD_CLASS,
                KEYWORD_CONSTRUCTOR,
                KEYWORD_FUNCTION,
                KEYWORD_METHOD,
                KEYWORD_FIELD,
                KEYWORD_STATIC,
                KEYWORD_VAR,
                KEYWORD_INT,
                KEYWORD_CHAR,
                KEYWORD_BOOLEAN,
                KEYWORD_VOID,
                KEYWORD_TRUE,
                KEYWORD_FALSE,
                KEYWORD_NULL,
                KEYWORD_THIS,
                KEYWORD_LET,
                KEYWORD_DO,
                KEYWORD_IF,
                KEYWORD_ELSE,
                KEYWORD_WHILE,
                KEYWORD_RETURN
        );

        symbols = Arrays.asList(
                "{", "}",
                "[", "]",
                "(", ")",
                ".",
                ",",
                ";",
                "+", "-", "*", "/",
                "&", "|", "~",
                "<", "=", ">"
        );

    }
}
