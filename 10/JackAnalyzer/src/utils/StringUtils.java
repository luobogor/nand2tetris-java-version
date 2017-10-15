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

    public static String wrapBySymbolTag(String str) {
        return wrapByTag(str, "symbol");
    }

    public static String wrapByIdentifierTag(String str) {
        return wrapByTag(str, "identifier");
    }

    public static String wrapByIntegerConstantTag(String str) {
        return wrapByTag(str, "integerConstant");
    }

    public static String wrapByStringConstantTag(String str) {
        return wrapByTag(str, "stringConstant");
    }

    public static String wrapByTag(String str, String tag) {
        return wrapBySAB(tag) + str + wrapByEAB(tag);
    }

    public static String wrapBySAB(String str) {
        return "<" + str + ">";
    }

    public static String wrapByEAB(String str) {
        return "</" + str + ">";
    }


}
