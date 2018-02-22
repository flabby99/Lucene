package common;

public class Util {
    //Helper to get the first two characters of a string
    public static String firstTwo(String str) {
        return str.length() < 2 ? str : str.substring(0, 2);
    }
}
