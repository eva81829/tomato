package com.example.tomato.recycleview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tomato.R;

import java.util.Map;

public class ShowDoneTaskViewHolder extends BaseViewHolder<ButtonShow> { //工廠模式: 實體產品(NYBeefPizza)
    private Button btnShow;
    private Map<Integer, TriggerEventHandler> triggerFuncMap;

    protected ShowDoneTaskViewHolder(View view, Map<Integer, TriggerEventHandler> triggerFuncMap) {
        super(view); //把Recycle的變數itemView賦值為自訂的view
        btnShow = view.findViewById(R.id.vh_btn_show);
        this.triggerFuncMap = triggerFuncMap;
    }

    @Override
    public void bind(final ButtonShow buttonShow) {
        if(buttonShow.getTaskIsShowed()){
            btnShow.setText(R.string.hide_done_task);
        }else{
            btnShow.setText(R.string.show_done_task);
        }

        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonShow.getTaskIsShowed()){
                    triggerFuncMap.get(R.id.vh_btn_show).hide();
                    buttonShow.setTaskIsShowed(false);
                    btnShow.setText(R.string.show_done_task);
                }else {
                    triggerFuncMap.get(R.id.vh_btn_show).show();
                    buttonShow.setTaskIsShowed(true);
                    btnShow.setText(R.string.hide_done_task);
                }
            }
        });
    }

    //工廠模式: 實體工廠(NYPizzaFactory)
    public static class Factory extends BaseViewHolder.Factory{
        public Factory(@Nullable TriggerFuncBuilder triggerFuncBuilder) {
            super(triggerFuncBuilder);
        }

        @NonNull
        public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int viewHolderLayoutID = R.layout.viewholder_show_done_task;
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(viewHolderLayoutID, parent, false);
            return new ShowDoneTaskViewHolder(view, triggerFuncMap);
        }

        @Override
        public String getType() {
            return ButtonShow.TYPE;
        }
    }
}

