package com.example.jhanlu.textgrabber.util;

/**
 * Created by JHanLu on 2018/4/2.
 */

public class GetResult {
    private String result = "";

    public String transform(double [] in) {
        for(int i = 0; i < in.length; i++) {
            if(i == in.length-1) {
                if(in[i] > 0.5) {
                    result += "1";
                } else {
                    result += "0";
                }
            } else {
                if(in[i] > 0.5) {
                    result += "1 ";
                } else {
                    result += "0 ";
                }
            }
        }
        GetDiGit gd = new GetDiGit();

        System.out.println(result);
        return gd.getDigit(result);
    }

}
