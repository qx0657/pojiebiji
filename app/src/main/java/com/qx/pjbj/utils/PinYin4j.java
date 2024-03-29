package com.qx.pjbj.utils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PinYin4j {


    public PinYin4j(){
    }
    /**
     * 字符串集合转换字符串(逗号分隔)
     *
     * @author wangsong
     * @param stringSet
     * @return
     */
    public String makeStringByStringSet(Set<String> stringSet) {
        StringBuilder str = new StringBuilder();
        int i = 0;
        for (String s : stringSet) {
            if (i == stringSet.size() - 1) {
                str.append(s);
            } else {
                str.append(s + ",");
            }
            i++;
        }
        return str.toString().toLowerCase();
    }


    /**
     * 获取汉字拼音全拼
     *
     * @author wangsong
     * @param src
     * @return Set<String>
     */
    public Set<String> getAllPinyin(String src) {
        char[] srcChar;
        srcChar = src.toCharArray();
        String[][] temp = new String[src.length()][];
        for (int i = 0; i < srcChar.length; i++) {
            char c = srcChar[i];
            if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
                String[] t = PinyinHelper.toHanyuPinyinStringArray(c);
                temp[i] = new String[t.length];
                for(int j=0;j<t.length;j++){
                    temp[i][j]=t[j].replaceAll("\\d", "");//获取全拼
                }
            } else if (((int) c >= 65 && (int) c <= 90)
                    || ((int) c >= 97 && (int) c <= 122)||c>=48&&c<=57||c==42) {
                temp[i] = new String[] { String.valueOf(srcChar[i]) };
            } else {
                temp[i] = new String[] {"null!"};
            }

        }
        String[] pingyinArray = paiLie(temp);
        return array2Set(pingyinArray);
    }
    /**
     * 获取汉字拼音首字母集合
     *
     * @author wangsong
     * @param src
     * @return Set<String>
     */
    public Set<String> getPinyin(String src) {
        char[] srcChar;
        srcChar = src.toCharArray();
        String[][] temp = new String[src.length()][];
        for (int i = 0; i < srcChar.length; i++) {
            char c = srcChar[i];
            if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
                String[] t = PinyinHelper.toHanyuPinyinStringArray(c);
                temp[i] = new String[t.length];
                for(int j=0;j<t.length;j++){
                    temp[i][j]=t[j].substring(0,1);
                }
            } else if (((int) c >= 65 && (int) c <= 90)
                    || ((int) c >= 97 && (int) c <= 122)||c>=48&&c<=57||c==42) {
                temp[i] = new String[] { String.valueOf(srcChar[i]) };
            } else {
                temp[i] = new String[] {"null!"};
            }

        }
        String[] pingyinArray = paiLie(temp);
        return array2Set(pingyinArray);
    }

    /*
     * 求2维数组所有排列组合情况
     * 比如:{{1,2},{3},{4},{5,6}}共有2中排列,为:1345,1346,2345,2346
     */
    private String[] paiLie(String[][] str){
        int max=1;
        for(int i=0;i<str.length;i++){
            max*=str[i].length;
        }
        String[] result=new String[max];
        for(int i = 0; i < max; i++){
            String s = "";
            int temp = 1;      //注意这个temp的用法。
            for(int j = 0; j < str.length; j++){
                temp *= str[j].length;
                s += str[j][i / (max / temp) % str[j].length];
            }
            result[i]=s;
        }

        return result;
    }
    /**
     * 去掉重复项
     * @param tArray
     * @return
     */
    public static <T extends Object> Set<T> array2Set(T[] tArray) {
        Set<T> tSet = new HashSet<T>(Arrays.asList(tArray));
        // TODO 没有一步到位的方法，根据具体的作用，选择合适的Set的子类来转换。
        return tSet;
    }
}