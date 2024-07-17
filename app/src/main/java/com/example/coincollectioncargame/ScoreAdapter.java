package com.example.coincollectioncargame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {
    private List<Score> scoreList;
    private Context context;
    private OnScoreItemClickListener listener;

    public interface OnScoreItemClickListener {
        void onScoreItemClick(Score score);
    }

    public ScoreAdapter(List<Score> scoreList, Context context, OnScoreItemClickListener listener) {
        this.scoreList = scoreList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_score, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        Score score = scoreList.get(position);
        holder.scoreTextView.setText(String.valueOf(score.getScore()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onScoreItemClick(score);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    public static class ScoreViewHolder extends RecyclerView.ViewHolder {
        TextView scoreTextView;

        public ScoreViewHolder(View itemView) {
            super(itemView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
        }
    }
}
