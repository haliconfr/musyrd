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

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.VideoViewHolder> {
    private List<SearchRes> searchRes;
    TextView sAlbum, sArtist;
    RecyclerView cardHolder;
    ImageView art;
    Context context;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    public SearchUserAdapter(Context context, List<SearchRes> searchResults, RecyclerView res) {
        this.searchRes = searchResults;
        this.cardHolder = res;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.usersearchitem,
                        parent,
                        false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardHolder.indexOfChild(v);
                Intent intent = new Intent(context, userProfile.class);
                intent.putExtra("user", searchRes.get(cardHolder.getChildPosition(v)).sAlbum);
                v.getContext().startActivity(intent);
            }
        });
        return new VideoViewHolder(v);
    }

    public void onBindViewHolder(@NonNull @NotNull VideoViewHolder holder, int position) {
        holder.setVideoData(searchRes.get(position));
    }
    @Override
    public int getItemCount() {
        return searchRes.size();
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        public VideoViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }

        void setVideoData(SearchRes searchRes) {
            art = itemView.findViewById(R.id.userItemImage);
            sAlbum = itemView.findViewById(R.id.userItemName);
            sArtist = itemView.findViewById(R.id.userRevCount);
            sAlbum.setText(searchRes.sAlbum);
            sArtist.setText(searchRes.number + " reviews");
            Picasso.get().load(searchRes.sArtUrl).fit().centerCrop()
                    .placeholder(R.drawable.icontemp)
                    .into(art);
        }
    }
}