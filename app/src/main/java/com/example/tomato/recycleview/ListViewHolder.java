package com.example.tomato.recycleview;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.tomato.R;

import java.util.Map;

public class ListViewHolder extends BaseViewHolder<ListTask> { //工廠模式: 實體產品(NYBeefPizza)
    private Context context;
    private CheckBox cbTask;
    private TextView tvTask;
    private ImageView ivTaskEdit;
    private LinearLayout llTask;
    private LinearLayout llEdit;
    private Map<Integer, TriggerEventHandler> triggerFuncMap;

    protected ListViewHolder(View view, Context context, Map<Integer, TriggerEventHandler> triggerFuncMap) {
        super(view); //把Recycle的變數itemView賦值為自訂的view
        this.context = context;
        cbTask = view.findViewById(R.id.vh_list_checkbox);
        tvTask = view.findViewById(R.id.vh_list_tv_task);
        llTask = view.findViewById(R.id.vh_list_ll);
        llEdit = view.findViewById(R.id.vh_list_ll_edit);
        ivTaskEdit = view.findViewById(R.id.vh_list_iv_edit);
        this.triggerFuncMap = triggerFuncMap;
    }

    @Override
    public void bind(final ListTask listTask) {
        tvTask.setText(listTask.getTaskName());
        if(!listTask.getIsDone()){ //未完成
            cbTask.setChecked(false);
            cbTask.setVisibility(View.VISIBLE);
            ivTaskEdit.setVisibility(View.VISIBLE);
            llTask.setBackgroundColor(context.getResources().getColor(R.color.defaultWhite,null));
//            llTask.setBackgroundColor(Color.parseColor(listTask.getTaskColorId()));

            //重新bind後, View.OnClickListener會被取代成新的傳入setOnClickListener
            llEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    triggerFuncMap.get(R.id.vh_list_iv_edit).edit(getLayoutPosition());
                }
            });

            cbTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //執行完就停止執行緒了
                    if(cbTask.isChecked()){
                        new Handler().postDelayed(new Runnable() {  //需要等打勾動畫跑完再改動recycle view, 否則會crash
                            @Override
                            public void run() { //每次按下後1000豪秒後執行
                                triggerFuncMap.get(R.id.vh_list_checkbox).done(getLayoutPosition());
                            }
                        }, 300);
                    }
                }
            });

        }else{ //已完成
//            cbTask.setChecked(true);
            cbTask.setClickable(false);
            cbTask.setVisibility(View.INVISIBLE);
            ivTaskEdit.setVisibility(View.INVISIBLE);
            llTask.setBackgroundColor(context.getResources().getColor(R.color.defaultLightGray,null));
        }
    }

    //工廠模式: 實體工廠(NYPizzaFactory)
    public static class Factory extends BaseViewHolder.Factory{
        public Factory(@Nullable TriggerFuncBuilder triggerFuncBuilder) {
            super(triggerFuncBuilder);
        }

        @NonNull
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int viewHolderLayoutID = R.layout.viewholder_list;
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewHolderLayoutID, parent, false);
            return new ListViewHolder(view, parent.getContext(), triggerFuncMap);
        }

        @Override
        public String getType() {
            return ListTask.TYPE;
        }
    }
}

