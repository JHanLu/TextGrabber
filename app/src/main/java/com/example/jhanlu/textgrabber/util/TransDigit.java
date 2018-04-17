package com.example.jhanlu.textgrabber.util;

/**
 * Created by JHanLu on 2018/4/3.
 */

public class TransDigit {
    public String digitToStirng(String str) {
        char ch = str.charAt(0);
        int index = (int) ch;

        if(index>=48 && index<=57) { // 0-9

            int num = index-47;
            return addSpace(numToString(num, 6));

        } else if(index>=65 && index<=90) { //A-Z

            int num  = index - 54;
            return addSpace(numToString(num, 6));

        } else if(index>=97 && index<=122) { //a-z
            int num = index - 60;
            return addSpace(numToString(num, 6));
        }

        return null;
    }

    public double[] digitToArr(String str) {
        String s = digitToStirng(str);
        String [] strArr = new String[6];
        double [] arr = new double[6];
        strArr = s.split(" ");
        for(int i = 0; i < 6; i++) {
            arr[i] = Double.parseDouble(strArr[i]);
        }
        return arr;
    }

    private String padStart(String str, int length, char ch) {
        return length > 0 ?
                padStart(ch+str, --length, ch):
                str;
    }

    private String numToString(int num, int length) {
        String numString = Integer.toBinaryString(num);
        return numString.length() == length ?
                numString :
                padStart(numString, length - numString.length(), '0');
    }

    private String addSpace(String str) {
        return (str.replace("", " ").trim());

    }
}
