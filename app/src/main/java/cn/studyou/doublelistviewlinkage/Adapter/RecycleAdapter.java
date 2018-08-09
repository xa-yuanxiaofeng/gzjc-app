package cn.studyou.doublelistviewlinkage.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.studyou.doublelistviewlinkage.R;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyHolder> {

    private Context context;
    private List<String> list;

    public RecycleAdapter(Context context,List list){
        this.context=context;
        this.list=list;
        notifyDataSetChanged();
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return  new MyHolder(LayoutInflater.from(context).inflate(R.layout.recycle_item,parent,false));

    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
         holder.textView.setText(list.get(position));
         holder.textView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

             }
         });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public MyHolder(View itemView){
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.textRecycle);
        }
    }

}
