package com.halicon.muspublic;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReleaseAdapter extends RecyclerView.Adapter<ReleaseAdapter.VideoViewHolder> {
    private List<ReviewInfo> revInf;
    TextView sDate, sText, sUsr;
    RecyclerView cardHolder;
    ImageView art;
    Context context;
    FirebaseFirestore db;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    public ReleaseAdapter(Context context, List<ReviewInfo> revInf, RecyclerView res) {
        this.revInf = revInf;
        this.cardHolder = res;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.relitem,
                parent,
                false);
        return new VideoViewHolder(v);
    }

    public void onBindViewHolder(@NonNull @NotNull VideoViewHolder holder, int position) {
        holder.getReleaseInfo(revInf.get(position));
    }
    @Override
    public int getItemCount() {
        return revInf.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        public VideoViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            db = FirebaseFirestore.getInstance();
            art = itemView.findViewById(R.id.relitemArt);
            sText = itemView.findViewById(R.id.relitemText);
            sUsr = itemView.findViewById(R.id.relitemUsr);
            sDate = itemView.findViewById(R.id.relitemDate);
        }
        void getReleaseInfo(ReviewInfo reviewInfo){
            StrictMode.setThreadPolicy(policy);
            Picasso.get().load(reviewInfo.artUrl).fit().centerCrop()
                    .placeholder(R.drawable.icontemp)
                    .into(art);
            sText.setText(reviewInfo.revText);
            sUsr.setText(reviewInfo.revUsr);
            sUsr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, userProfile.class);
                    intent.putExtra("user", reviewInfo.revUsr);
                    context.startActivity(intent);
                }
            });
            sDate.setText(reviewInfo.date);
            setStars(reviewInfo);
        }
        void setStars(ReviewInfo reviewInfo){
            int starNumber = reviewInfo.stars;
            ImageView stars = itemView.findViewById(R.id.relitemStars);
            if(starNumber == 1){
                stars.setImageResource(R.drawable.onestar);
            }
            if(starNumber == 2){
                stars.setImageResource(R.drawable.twostar);
            }
            if(starNumber == 3){
                stars.setImageResource(R.drawable.threestar);
            }
            if(starNumber == 4){
                stars.setImageResource(R.drawable.fourstar);
            }
            if(starNumber == 5){
                stars.setImageResource(R.drawable.fivestar);
            }
        }
    }
}