package com.example.tomato.recycleview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.tomato.R;
import com.example.tomato.global.Global;

import java.util.Map;

public class TomatoViewHolder extends BaseViewHolder<TomatoTask> { //工廠模式: 實體產品(NYBeefPizza)
    private Context context;
    private TextView tvTaskName;
    private TextView tvTaskTime;
    private ImageView ivTaskEdit;
    private ImageView ivTaskIcon;
    private LinearLayout llTask;
    private LinearLayout llEdit;
    private Map<Integer, TriggerEventHandler> triggerFuncMap;

    protected TomatoViewHolder(View view, Context context, final Map<Integer, TriggerEventHandler> triggerFuncMap) {
        super(view); //把Recycle的變數itemView賦值為自訂的view
        this.context = context;
        tvTaskName = view.findViewById(R.id.vh_tomato_tv_task_name);
        tvTaskTime = view.findViewById(R.id.vh_tomato_tv_task_time);
        llTask = view.findViewById(R.id.vh_tomato_ll);
        llEdit = view.findViewById(R.id.vh_tomato_ll_edit);
        ivTaskEdit = view.findViewById(R.id.vh_tomato_iv_edit);
        ivTaskIcon = view.findViewById(R.id.vh_tomato_iv_icon);
        this.triggerFuncMap = triggerFuncMap;
    }

    @Override
    public void bind(final TomatoTask tomatoTask) {
        tvTaskName.setText(tomatoTask.getTaskName());
        tvTaskTime.setText(tomatoTask.getTaskTimeString());
        String icon = tomatoTask.getTaskIcon();
        int resId = context.getResources().getIdentifier(icon,"drawable", "com.example.tomato");
        ivTaskIcon.setBackground(ContextCompat.getDrawable(context, resId));

        if(tomatoTask.getIsDone()== Global.Parameter.TASK_UNDONE){ //未完成
            ivTaskEdit.setVisibility(View.VISIBLE);
            llTask.setBackgroundColor(context.getResources().getColor(tomatoTask.getTaskColorId(),null));
            //重新bind後, View.OnClickListener會被取代成新的傳入setOnClickListener

            llTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    triggerFuncMap.get(R.id.vh_tomato_ll).countTime(getLayoutPosition());
                }
            });

            llEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    triggerFuncMap.get(R.id.vh_tomato_iv_edit).edit(getLayoutPosition());
                }
            });
        }else{ //已完成
            ivTaskEdit.setVisibility(View.INVISIBLE);
            llTask.setBackgroundColor(context.getResources().getColor(R.color.defaultLightGray,null));
            llTask.setClickable(false);
        }

    }

    //工廠模式: 實體工廠(NYPizzaFactory)
    public static class Factory extends BaseViewHolder.Factory{
        public Factory(@Nullable TriggerFuncBuilder triggerFuncBuilder) {
            super(triggerFuncBuilder);
        }

        @NonNull
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int viewHolderLayoutID = R.layout.viewholder_tomato;
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewHolderLayoutID, parent, false);
            return new TomatoViewHolder(view, parent.getContext(), triggerFuncMap);
        }

        @Override
        public String getType() {
            return TomatoTask.TYPE;
        }
    }
}

