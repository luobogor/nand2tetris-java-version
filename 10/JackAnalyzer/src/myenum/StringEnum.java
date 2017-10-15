package myenum;

import java.util.Arrays;
import java.util.List;

public class StringEnum {
    public static List<String> keywords;
    public static List<String> symbols;

    static {
        keywords = Arrays.asList("class",
                "constructor",
                "function",
                "method",
                "field",
                "static",
                "var",
                "int",
                "char",
                "boolean",
                "void",
                "true",
                "false",
                "null",
                "this",
                "let",
                "do",
                "if",
                "else",
                "while",
                "return"
        );

        symbols = Arrays.asList(
                "{","}",
                "[","]",
                "(",")",
                ".",
                ",",
                ";",
                "+","-","*","/",
                "&", "|","~",
                "<","=",">"
        );

    }
}
