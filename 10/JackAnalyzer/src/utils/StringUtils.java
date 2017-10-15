package utils;

public class StringUtils {
    public static String wrapByParenthesis(String str) {
        return "(" + str + ")";
    }
    public static String wrapByDoubleQuotation(String str) {
        return "\"" + str + "\"";
    }

    public static String wrapByKeyword(String str) {
        return wrapByTag(str, "keyword");
    }

    public static String wrapBySymbol(String str) {
        return wrapByTag(str, "symbol");
    }

    public static String wrapByIdentifier(String str) {
        return wrapByTag(str, "identifier");
    }

    public static String wrapByIntegerConstant(String str) {
        return wrapByTag(str, "integerConstant");
    }

    public static String wrapByStringConstant(String str) {
        return wrapByTag(str, "stringConstant");
    }

    private static String wrapByTag(String str, String tag) {
        return "<" + tag + ">" + str + "</" + tag + ">";
    }
}
