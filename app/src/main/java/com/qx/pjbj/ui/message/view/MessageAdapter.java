package com.qx.pjbj.ui.message.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.TimeUtils;
import com.qx.pjbj.R;
import com.qx.pjbj.ui.message.data.Message;

import java.util.List;

/**
 * Create by QianXiao
 * On 2020/8/24
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.attach(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{
        TextView tv_manager_messageitem,
                tv_time_messageitem,
                tv_msg_messageitem;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_manager_messageitem = f(itemView,R.id.tv_manager_messageitem);
            tv_time_messageitem = f(itemView,R.id.tv_time_messageitem);
            tv_msg_messageitem = f(itemView,R.id.tv_msg_messageitem);
        }

        void attach(Message message){
            tv_manager_messageitem.setText("管理员"+message.getFromuser()+"：");
            tv_time_messageitem.setText(TimeUtils.date2String(message.getTime(),"yyyy-MM-dd HH:mm:ss"));
            tv_msg_messageitem.setText(message.getMsg());
        }

        @SuppressWarnings("unchecked")
        private <E> E f(View view,int id){
            return (E) view.findViewById(id);
        }
    }
}
