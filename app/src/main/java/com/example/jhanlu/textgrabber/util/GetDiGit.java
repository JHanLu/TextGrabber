package com.example.jhanlu.textgrabber.util;

/**
 * Created by JHanLu on 2018/4/3.
 */

public class GetDiGit {
    public String getDigit(String in) {
        int total = 0;
        String [] arr = new String[6];
        arr = in.split(" ");
        for(int i = 0; i < 6; i++) {
            if("1".equals(arr[i])) {
                total += Math.pow(2, 5-i);
            }
        }

        if(total >= 1 && total <= 10) { // 阿拉伯数字
            return String.valueOf(total-1);
        }

        else if(total >= 11 && total <= 36) { // 大写字母
            int firstEnglish;
            char firstE = 'A', lastE = 'Z';
            firstEnglish = (int)firstE;

            char digit;
            digit = (char)(total-11+firstEnglish);
            return String.valueOf(digit);
        }

        else if(total >= 37 && total <= 62) { // 大写字母
            int firstEnglish;
            char firstE = 'a', lastE = 'z';
            firstEnglish = (int)firstE;

            char digit;
            digit = (char)(total-37+firstEnglish);
            return String.valueOf(digit);
        }

        return "can not recognize";
    }
}
