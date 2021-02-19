package com.qx.pjbj.ui.main.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.TimeUtils;
import com.qx.pjbj.R;
import com.qx.pjbj.data.PjNote;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/7/23
 */
public class PjnoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PjNote> pjNoteList;
    //普通布局的type
    static final int TYPE_ITEM = 0;
    //脚布局
    static final int TYPE_FOOTER = 1;

    //上拉加载更多
    static final int PULL_LOAD_MORE = 0;
    //正在加载更多
    static final int LOADING_MORE = 1;
    //没有更多
    static final int NO_MORE = 2;

    //脚布局当前的状态,默认为正在加载更多
    int footer_state = 1;

    private OnItemOnClickListener onItemOnClickListener;

    public interface OnItemOnClickListener{
        void OnClick(PjNote pjNote,View view);
    }

    public void setOnItemOnClickListener(OnItemOnClickListener onItemOnClickListener) {
        this.onItemOnClickListener = onItemOnClickListener;
    }

    public PjnoteAdapter(List<PjNote> pjNoteList) {
        this.pjNoteList = pjNoteList;
        changeState(2);
    }

    public void addData(List<PjNote> lst){
        this.pjNoteList.addAll(lst);
        notifyDataSetChanged();
    }

    /**
     * 改变脚布局的状态的方法,在activity根据请求数据的状态来改变这个状态
     *
     * @param state
     */
    public void changeState(int state) {
        this.footer_state = state;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_ITEM){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pjnote, parent, false);
            return new PjnoteViewHolder(view);
        }else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_load_more_layout, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PjnoteViewHolder) {
            PjNote pjNote = pjNoteList.get(position);
            ((PjnoteViewHolder) holder).attach(pjNote);
            holder.itemView.setOnClickListener(v -> onItemOnClickListener.OnClick(pjNote,v));
        }else{
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            if (position == 0) {//如果第一个就是脚布局,,那就让他隐藏
                footViewHolder.mProgressBar.setVisibility(View.GONE);
                footViewHolder.tv_line1.setVisibility(View.GONE);
                footViewHolder.tv_line2.setVisibility(View.GONE);
                footViewHolder.tv_state.setText("");
            }
            switch (footer_state) {//根据状态来让脚布局发生改变
                case PULL_LOAD_MORE://上拉加载
                    footViewHolder.mProgressBar.setVisibility(View.GONE);
                    footViewHolder.tv_state.setVisibility(View.GONE);
                    footViewHolder.tv_line1.setVisibility(View.GONE);
                    footViewHolder.tv_line2.setVisibility(View.GONE);
                    break;
                case LOADING_MORE:
                    footViewHolder.mProgressBar.setVisibility(View.VISIBLE);
                    footViewHolder.tv_line1.setVisibility(View.GONE);
                    footViewHolder.tv_line2.setVisibility(View.GONE);
                    footViewHolder.tv_state.setVisibility(View.VISIBLE);
                    footViewHolder.tv_state.setText("正在加载...");
                    break;
                case NO_MORE:
                    footViewHolder.mProgressBar.setVisibility(View.GONE);
                    footViewHolder.tv_line1.setVisibility(View.VISIBLE);
                    footViewHolder.tv_line2.setVisibility(View.VISIBLE);
                    footViewHolder.tv_state.setVisibility(View.VISIBLE);
                    footViewHolder.tv_state.setText("我也是有底线的");
                    footViewHolder.tv_state.setTextColor(Color.parseColor("#969696"));
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return pjNoteList==null ? 0:pjNoteList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        //如果position加1正好等于所有item的总和,说明是最后一个item,将它设置为脚布局
        if (position+1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    /**
     * 条目布局
     */
    class PjnoteViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView tv_game_name_item,tv_game_packagename_item,tv_game_type_item,tv_author_item,tv_updatetime_item,tv_look_item,tv_good_item;
        LinearLayout ll_lookandgood_item,ll_shenheing_item;

        public PjnoteViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tv_game_name_item = f(itemView,R.id.tv_game_name_item);
            tv_game_packagename_item = f(itemView,R.id.tv_game_packagename_item);
            tv_game_type_item = f(itemView,R.id.tv_game_type_item);
            tv_author_item = f(itemView,R.id.tv_author_item);
            tv_updatetime_item = f(itemView,R.id.tv_updatetime_item);
            tv_look_item = f(itemView,R.id.tv_look_item);
            tv_good_item = f(itemView,R.id.tv_good_item);
            ll_lookandgood_item = f(itemView,R.id.ll_lookandgood_item);
            ll_shenheing_item = f(itemView,R.id.ll_shenheing_item);
        }

        public void attach(PjNote pjNote){
            tv_game_name_item.setText(pjNote.getGamename());
            tv_game_packagename_item.setText(pjNote.getPackagename());
            tv_game_type_item.setText(pjNote.getType());
            tv_author_item.setText(pjNote.getAuthor());
            tv_updatetime_item.setText(TimeUtils.date2String(pjNote.getUpdatetime(),"yyyy-MM-dd HH:mm:ss"));
            if(pjNote.isPass()){
                ll_lookandgood_item.setVisibility(View.VISIBLE);
                ll_shenheing_item.setVisibility(View.GONE);
                tv_look_item.setText(String.valueOf(pjNote.getLook()));
                tv_good_item.setText(String.valueOf(pjNote.getGood()));
            }else{
                ll_lookandgood_item.setVisibility(View.GONE);
                ll_shenheing_item.setVisibility(View.VISIBLE);
            }
        }

        @SuppressWarnings("unchecked")
        private <E> E f(View view,int id){
            return (E) view.findViewById(id);
        }
    }

    /**
     * 脚布局
     */
    class FootViewHolder extends RecyclerView.ViewHolder{
        private ProgressBar mProgressBar;
        private TextView tv_state;
        private TextView tv_line1;
        private TextView tv_line2;

        public FootViewHolder(@NonNull View itemView) {
            super(itemView);
            mProgressBar = f(itemView,R.id.progressbar);
            tv_state = f(itemView,R.id.foot_view_item_tv);
            tv_line1 = f(itemView,R.id.tv_line1);
            tv_line2 = f(itemView,R.id.tv_line2);
        }

        @SuppressWarnings("unchecked")
        private <E> E f(View view,int id){
            return (E) view.findViewById(id);
        }
    }
}
