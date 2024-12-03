package com.example.tomato.recycleview;

import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<? extends BaseViewHolder.Factory> factories; //設定每個viewType(ViewHolder的樣式總數)的工廠
    private List<BaseInfo> baseInfoList; //設定每個position(Item的總數)所存放的BaseInfo
    private Map<String, Integer> typeMap; //設定每個ViewHolder的viewType在factories List中所對應之index

    public CustomAdapter(List<? extends BaseViewHolder.Factory> factories){
        this.factories = factories;

        /*開始設定typeMap*/
        this.typeMap = new HashMap<>();
        int count = factories.size();
        for(int i=0;i<count;i++){
            BaseViewHolder.Factory factory = factories.get(i);// factories中每個工廠
            typeMap.put(factory.getType(), i);// 工廠的TYPE 對應到的index
        }
        /*結束設定typeMap*/
    }

    @Override
    public int getItemViewType(int position) {

        String type = baseInfoList.get(position).getType(); //該position中的baseInfo 其對應之ViewHolder的viewType
        return typeMap.get(type); //找到該 ViewHolder的viewType在factories List中所對應之index
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //透過外部設訂的factories list對應到的目前的factory, 直接創建並回傳對應的BaseViewHolder
        BaseViewHolder.Factory factory = factories.get(viewType); // viewType == index
        return factory.onCreateViewHolder(parent, viewType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) { //onCreateViewHolder時已將holder區分為UserNameViewHolder/TomatoViewHolder
        holder.bind(baseInfoList.get(position)); // 多型, holder屬於哪種BaseViewHolder的子類別, 則呼叫該類別中的holder.bind -> 為什麼可以丟BaseInfo進去?
    }

    @Override
    public int getItemCount() {
        return baseInfoList.size();
    }

    /* data binding */
    public void bindDataSource(List<BaseInfo> baseInfos){
        this.baseInfoList = baseInfos;
        notifyDataSetChanged();
    }

    public void addData(BaseInfo info){
        this.baseInfoList.add(info); // 如果baseInfoList為<? extends BaseInfo>, 則無法add BaseInfo(因為若baseInfoList<>裡面裝的是BaseInfo的子類, 則無法插入BaseInfo)
        notifyItemInserted(baseInfoList.size() - 1);
    }

    public void addDatas(List<BaseInfo> baseInfos){
        for(int i=0; i<baseInfos.size(); i++){
            this.baseInfoList.add(baseInfos.get(i));
            notifyItemInserted(baseInfoList.size() - 1);
        }
    }

    public void insertData(int index, BaseInfo info){
        this.baseInfoList.add(index, info); // 如果baseInfoList為<? extends BaseInfo>, 則無法add BaseInfo(因為若baseInfoList<>裡面裝的是BaseInfo的子類, 則無法插入BaseInfo)
        notifyDataSetChanged();
    }

    public void replaceData(int index, BaseInfo info){
        this.baseInfoList.set(index, info);
        notifyItemChanged(index);
    }

    public void removeData(int index){
        this.baseInfoList.remove(index);
        notifyDataSetChanged();
    }

    public void updateData(){
        notifyDataSetChanged();
    }

}
