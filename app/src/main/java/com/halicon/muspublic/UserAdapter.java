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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<recommendedUser> rUser;
    TextView percentage;
    RecyclerView cardHolder;
    ImageView icon;
    Context context;
    FirebaseFirestore db;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    public UserAdapter(Context context, List<recommendedUser> rUser, RecyclerView res) {
        this.rUser = rUser;
        this.cardHolder = res;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.useritem,
                parent,
                false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, userProfile.class);
                intent.putExtra("user", rUser.get(cardHolder.getChildPosition(v)).username);
                v.getContext().startActivity(intent);
            }
        });
        return new UserAdapter.UserViewHolder(v);
    }

    public void onBindViewHolder(@NonNull @NotNull UserViewHolder holder, int position) {
        holder.getReleaseInfo(rUser.get(position));
    }
    @Override
    public int getItemCount() {
        return rUser.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        public UserViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            db = FirebaseFirestore.getInstance();
            icon = itemView.findViewById(R.id.userImage);
            percentage = itemView.findViewById(R.id.percentage);
        }
        void getReleaseInfo(recommendedUser rUser){
            StrictMode.setThreadPolicy(policy);
            Picasso.get().load(rUser.userpfp).fit().centerCrop()
                    .placeholder(R.drawable.icontemp)
                    .into(icon);

            percentage.setText(String.valueOf(rUser.percentage) + "%");
        }
    }
}