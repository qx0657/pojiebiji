package com.qx.pjbj.ui.add.getappinfo.view;

import android.text.TextUtils;
import android.widget.Filter;

import com.qx.pjbj.ui.add.getappinfo.data.AppInfo;
import com.qx.pjbj.utils.PinYin4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Create by QianXiao
 * On 2020/6/13
 */
public class SearchFilter extends Filter {
    private final Object mLock = new Object();
    ArrayList<AppInfo> mOriginalValues;
    private AppInfoAdapter adapter;
    /**
     * 用于拼音搜索
     */
    ArrayList<Set<String>> pinYinList,pinYinAllList;

    SearchFilter(AppInfoAdapter adapter) {
        this.adapter = adapter;
        synchronized (mLock) {
            this.mOriginalValues = new ArrayList<>(adapter.appInfoList);
            initPinYinList();
        }
    }

    private void initPinYinList() {
        pinYinList = new ArrayList<Set<String>>();
        pinYinAllList = new ArrayList<Set<String>>();
        PinYin4j pinyin = new PinYin4j();
        for (int i = 0; i < adapter.appInfoList.size(); i++) {
            pinYinList.add(pinyin.getPinyin(adapter.appInfoList.get(i).getAppname()));
            pinYinAllList.add(pinyin.getAllPinyin(adapter.appInfoList.get(i).getAppname()));
        }
    }

    void refreshData(){
        this.mOriginalValues = new ArrayList<>(adapter.appInfoList);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        if(TextUtils.isEmpty(constraint)){
            synchronized (mLock){
                ArrayList<AppInfo> list = new ArrayList<>(mOriginalValues);
                results.values = list;
                results.count = -1;//list.size();
            }
        }else{
            ArrayList<AppInfo> searchresult = new ArrayList<>();
            int i = 0;
            for (AppInfo mOriginalValue : mOriginalValues) {
                if(mOriginalValue.getAppname().toLowerCase().contains(constraint)
                        ||mOriginalValue.getPackagename().toLowerCase().contains(constraint)){
                    searchresult.add(mOriginalValue);
                }else{
                    boolean iscontaineverychar = true;
                    for (int j = 0; j < constraint.length(); j++) {
                        if(!mOriginalValue.getAppname().toLowerCase().contains(String.valueOf(constraint.charAt(j)))){
                            iscontaineverychar = false;
                            break;
                        }
                    }
                    if(iscontaineverychar){
                        searchresult.add(mOriginalValue);
                    }else{
                        //查看姓名拼音首字母是否符合过滤条件
                        Set<String> pinyinSet = pinYinList.get(i);
                        Iterator<String> pinyin = pinyinSet.iterator();
                        boolean ispyszmfuhe = false;
                        while (pinyin.hasNext()) {
                            if (pinyin.next().toString().contains(constraint)) {
                                ispyszmfuhe = true;
                                break;
                            }
                        }
                        if(ispyszmfuhe){
                            searchresult.add(mOriginalValue);
                        }else{
                            //查看姓名拼音全拼是否符合过滤条件
                            Set<String> pinyinAllSet = pinYinAllList.get(i);
                            Iterator<String> pinyinAll = pinyinAllSet.iterator();
                            while (pinyinAll.hasNext()) {
                                if (pinyinAll.next().toString().contains(constraint)) {
                                    searchresult.add(mOriginalValue);
                                    break;
                                }
                            }
                        }

                    }
                }
                i++;
            }
            results.values = searchresult;
            results.count = searchresult.size();
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if(TextUtils.isEmpty(constraint)){
            adapter.appInfoList.clear();
            adapter.appInfoList.addAll(mOriginalValues);
        }else{
            adapter.appInfoList = (List<AppInfo>) results.values;
        }
        adapter.notifyDataSetChanged();
    }
}
