package com.halicon.muspublic;

import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VideoViewHolder> {
    private List<ReviewInfo> revInf;
    TextView sArtist, sText, sUsr, sDate, sAlbum;
    RecyclerView cardHolder;
    String id;
    ImageView art, delete;
    Context context;
    FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    public ReviewAdapter(Context context, List<ReviewInfo> revInf, RecyclerView res) {
        this.revInf = revInf;
        this.cardHolder = res;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.revitem,
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
            sDate = itemView.findViewById(R.id.relitemDate2);
            art = itemView.findViewById(R.id.revArt);
            sAlbum = itemView.findViewById(R.id.revTitle);
            sArtist = itemView.findViewById(R.id.revArtist);
            delete = itemView.findViewById(R.id.delicon);
            sText = itemView.findViewById(R.id.relText);
            sUsr = itemView.findViewById(R.id.revUsr);
        }
        void getReleaseInfo(ReviewInfo reviewInfo){
            StrictMode.setThreadPolicy(policy);
            art.setImageResource(R.drawable.icontemp);
            Picasso.get().load(reviewInfo.artUrl).fit().centerCrop()
                    .placeholder(R.drawable.icontemp)
                    .into(art);
            sAlbum.setText(reviewInfo.revAlbum);
            sArtist.setText(reviewInfo.revArtist);
            sText.setText(reviewInfo.revText);
            sUsr.setText(reviewInfo.revUsr);
            sAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.collection("releases")
                            .whereEqualTo("Art", reviewInfo.artUrl)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("lol", document.getId() + " => " + document.getData());
                                            Intent intent = new Intent(context, ReleasePage.class);
                                            intent.putExtra("album", document.getString("Title"));
                                            intent.putExtra("artist", document.getString("Artist"));
                                            intent.putExtra("art", document.getString("Art"));
                                            intent.putExtra("id", document.getId());
                                            context.startActivity(intent);
                                        }
                                    } else {
                                        Log.d("error!", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            });
            sUsr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, userProfile.class);
                    intent.putExtra("user", reviewInfo.revUsr);
                    context.startActivity(intent);
                }
            });
            if(reviewInfo.revUsr.equals(user.getDisplayName())){
                delete.setVisibility(View.VISIBLE);
            }else{
                delete.setVisibility(View.GONE);
            }
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Click", "click");
                    db.collection("reviews")
                            .whereEqualTo("User", user.getDisplayName())
                            .whereEqualTo("Date", reviewInfo.date)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for(QueryDocumentSnapshot document : task.getResult()){
                                        Log.d("Success", "yuh");
                                        id = document.getId();
                                        db.collection("reviews").document(id).delete();
                                    }
                                }
                            });
                }
            });
            if(reviewInfo.date != null){
                Date date1 = new Date ();
                date1.setTime((long)Integer.valueOf(reviewInfo.date)*1000);
                sDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(date1));
            }
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