package com.yt.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MasechetAdapter extends RecyclerView.Adapter<MasechetAdapter.MasechetViewHolder> {
    private List<String> masechetList;
    private OnItemClickListener onItemClickListener;

    public MasechetAdapter(List<String> masechetList) {
        this.masechetList = masechetList;
    }

    @Override
    public MasechetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MasechetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MasechetViewHolder holder, int position) {
        holder.bind(masechetList.get(position));
    }

    @Override
    public int getItemCount() {
        return masechetList.size();
    }

    public class MasechetViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public MasechetViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }

        public void bind(String masechet) {
            textView.setText(masechet);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
