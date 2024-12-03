package com.example.tomato.recycleview;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseViewHolder<T extends BaseInfo> extends RecyclerView.ViewHolder{ //工廠模式: 抽象產品(Pizza)
    protected BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(T baseModel); //工廠模式: 抽象產品中的抽象方法, 模擬複雜的產品創建/創建後的加工流程, 實體產品中實現此方法(getName)

    //工廠模式: 抽象工廠
    public abstract static class Factory implements BaseInfo{ //工廠模式: 抽象工廠(PizzaFactory)
        protected Map<Integer, TriggerEventHandler> triggerFuncMap;

        public Factory(@Nullable TriggerFuncBuilder triggerFuncBuilder){
            if(triggerFuncBuilder != null)
                this.triggerFuncMap = triggerFuncBuilder.getMap();
        }

        @NonNull
        public abstract BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType); //產生ViewHolder的接口, 工廠模式: (createPizza)
    }

    public static class TriggerFuncBuilder { // 建立按鍵監聽的接口
        private Map<Integer, TriggerEventHandler> triggerFuncMap;
        public TriggerFuncBuilder(){
            this.triggerFuncMap = new HashMap<>();
        }
        public TriggerFuncBuilder add(Integer resourceId, TriggerEventHandler handler){ //新增元件的Id與對應的自訂反應
            this.triggerFuncMap.put(resourceId, handler);
            return this;
        }
        private Map<Integer, TriggerEventHandler> getMap(){
            return this.triggerFuncMap;
        }
    }

    public static abstract class TriggerEventHandler{ // 自訂之介面與方法, 在new CustomAdapter, 定義樣式時自己實現: 按下去後Activity上的反應
        public void edit(int position){}; //不是抽象方法
        public void show(){}; //不是抽象方法
        public void hide(){}; //不是抽象方法
        public void done(int position){}; //不是抽象方法
        public void countTime(int position){}; //不是抽象方法
    }
}
