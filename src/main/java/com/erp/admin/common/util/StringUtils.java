package com.erp.admin.common.util;

public abstract class StringUtils {
    public static boolean isEmpty(String value) {
        int strLen;
        if ((value == null) || ((strLen = value.length()) == 0)) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumeric(Object obj) {
        if (obj == null) {
            return false;
        }
        char[] chars = obj.toString().toCharArray();
        int length = chars.length;
        if (length < 1) {
            return false;
        }
        int i = 0;
        if ((length > 1) && (chars[0] == '-')) {
        }
        for (i = 1; i < length; i++) {
            if (!Character.isDigit(chars[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean areNotEmpty(String...values) {
        boolean result = true;
        if ((values == null) || (values.length == 0)) {
            result = false;
        } else {
            String[] arrayOfString = values;
            int j = values.length;
            for (int i = 0; i < j; i++) {
                String value = arrayOfString[i];
                result &= !isEmpty(value);
            }
        }
        return result;
    }

    public static String unicodeToChinese(String unicode) {
        StringBuilder out = new StringBuilder();
        if (!isEmpty(unicode)) {
            for (int i = 0; i < unicode.length(); i++) {
                out.append(unicode.charAt(i));
            }
        }
        return out.toString();
    }

    public static String stripNonValidXMLCharacters(String input) {
        if ((input == null) || ("".equals(input))) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if ((current == '\t') || (current == '\n') || (current == '\r') || ((current >= ' ') && (current <= 55295))
                    || ((current >= 57344) && (current <= 65533)) || ((current >= 65536) && (current <= 1114111))) {
                out.append(current);
            }
        }
        return out.toString();
    }
    
    /**
     * es 处理专属
     * @param suggest
     * @return
     */
    public static String[] processString(String suggest){
    	String[] result = suggest.split(",");
    	StringBuffer sb = new StringBuffer();
    	for (String string : result) {
    		sb.append(string.split("\\|")[0]).append(",");
		}
    	return sb.toString().split(",");
    	
    }
}
