package com.qx.pjbj.ui.add.getappinfo.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qx.pjbj.R;
import com.qx.pjbj.ui.add.getappinfo.data.AppInfo;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/7/29
 */
public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.AppInfoViewHolder> implements Filterable {
    public List<AppInfo> appInfoList;
    private OnSelectCompleteListener onSelectCompleteListener;
    private SearchFilter filter;

    @Override
    public Filter getFilter() {
        return filter;
    }

    public interface OnSelectCompleteListener{
        void onSelect(AppInfo appInfo);
    }

    public AppInfoAdapter(List<AppInfo> appInfoList, OnSelectCompleteListener onSelectCompleteListener) {
        this.appInfoList = appInfoList;
        this.onSelectCompleteListener = onSelectCompleteListener;
        //初始化搜索器
        filter = new SearchFilter(this);
    }

    @NonNull
    @Override
    public AppInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appinfo, parent, false);
        AppInfoViewHolder holder = new AppInfoViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AppInfoViewHolder holder, int position) {
        AppInfo appInfo = appInfoList.get(position);
        holder.itemView.setOnClickListener(v->onSelectCompleteListener.onSelect(appInfo));
        holder.attach(appInfo);
    }

    @Override
    public int getItemCount() {
        return appInfoList.size();
    }

    class AppInfoViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        ImageView iv_appicon_item_appinfo;
        TextView tv_appname_item_appinfo,tv_apppkname_item_appinfo;

        public AppInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            iv_appicon_item_appinfo = f(R.id.iv_appicon_item_appinfo);
            tv_appname_item_appinfo = f(R.id.tv_appname_item_appinfo);
            tv_apppkname_item_appinfo = f(R.id.tv_apppkname_item_appinfo);
        }

        @SuppressWarnings("unchecked")
        private <E> E f(int id){
            return (E) itemView.findViewById(id);
        }

        public void attach(AppInfo appInfo) {
            tv_appname_item_appinfo.setText(appInfo.getAppname());
            tv_apppkname_item_appinfo.setText(appInfo.getPackagename());
            iv_appicon_item_appinfo.setImageDrawable(appInfo.getIcon());
        }

    }
}
