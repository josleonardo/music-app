package umn.ac.id.musicplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LocalMusicAdapter extends RecyclerView.Adapter<LocalMusicAdapter.MusicViewHolder> {
    private final List<Music> musicList;
    private final Context context;

    public LocalMusicAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music currentMusic = musicList.get(position);

        holder.title.setText(currentMusic.getTitle());
        holder.artist.setText(currentMusic.getArtist());
        holder.duration.setText(currentMusic.getDuration());

        Glide.with(context).load(currentMusic.getAlbumArtUri()).placeholder(R.drawable.ic_music).error(R.drawable.ic_music).into(holder.albumArt);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView albumArt;
        TextView title, artist, duration;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            albumArt = itemView.findViewById(R.id.imageAlbumArt);
            title = itemView.findViewById(R.id.textMusicTitle);
            artist = itemView.findViewById(R.id.textArtist);
            duration = itemView.findViewById(R.id.textDuration);
        }
    }
}
