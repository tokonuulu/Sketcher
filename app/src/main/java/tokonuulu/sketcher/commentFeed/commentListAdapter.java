package tokonuulu.sketcher.commentFeed;

import android.content.Context;
import android.content.DialogInterface;
import android.content.UriMatcher;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import tokonuulu.sketcher.R;

public class commentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int TYPE_TEXT = 1, TYPE_AUDIO = 2, TYPE_VIDEO = 3;
    private List<Comment> data;
    private List<Comment> wholeList;
    private Context context;

    public commentListAdapter(Context context) {
        this.context = context;
    }

    public commentListAdapter(Context context, List<Comment> data, List<Comment> wholeList) {
        this.context = context;
        this.data = data;
        this.wholeList = wholeList;
    }

    @Override
    public int getItemViewType (int position) {
        if (data.get(position) instanceof TextComment) {
            return TYPE_TEXT;
        } else if (data.get(position) instanceof AudioComment) {
            return TYPE_AUDIO;
        } /*else if (data.get(position) instanceof Header) {
            return TYPE_HEADER;
        }*/
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Attach layout for single cell
        int layout = 0;
        RecyclerView.ViewHolder viewHolder;
        // Identify viewType returned by getItemViewType(...)
        // and return ViewHolder Accordingly
        switch (viewType){
            case TYPE_TEXT:
                layout = R.layout.comment_text_view;
                View Text = LayoutInflater
                        .from(parent.getContext())
                        .inflate(layout, parent, false);
                viewHolder = new commentListAdapter.TextViewHolder(Text);
                break;
            case TYPE_AUDIO:
                layout = R.layout.comment_audio_view;
                View Audio = LayoutInflater
                        .from(parent.getContext())
                        .inflate(layout, parent, false);
                viewHolder = new commentListAdapter.AudioViewHolder(Audio);
                break;
            default:
                viewHolder = null;
                break;
        }
        return viewHolder;
    }

    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int viewType = holder.getItemViewType();
        switch (viewType){
            case TYPE_TEXT:
                TextComment textComment = (TextComment) data.get(position);
                ((commentListAdapter.TextViewHolder) holder).updateView(textComment);
                break;
            case TYPE_AUDIO:
                AudioComment audioComment = (AudioComment) data.get(position);
                ((commentListAdapter.AudioViewHolder) holder).updateView(audioComment);
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle("Delete comment");
                alertDialog.setMessage("Do you want to remove this comment?");

                alertDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                data.remove(position);
                                wholeList.remove(data.get(position));
                                notifyItemRemoved(position);
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
            }
        });
    }

    /* defining view holder classes */
    public class TextViewHolder extends RecyclerView.ViewHolder {

        private TextView source, date, text;

        public TextViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            source=(TextView)itemView.findViewById(R.id.source);
            date=(TextView)itemView.findViewById(R.id.date);
            text=(TextView)itemView.findViewById(R.id.text);
        }

        public void updateView (TextComment textComment){
            // Attach values for each item
            String Source = textComment.getSource();
            if ( !textComment.block.isEmpty() )
                Source = Source + " : " + textComment.block;

            String Date   = textComment.getDate();
            String Text   = textComment.getText();

            source.setText(Source);
            if ( Source.equals("") )
                source.setText("Project");
            date.setText(Date);
            text.setText(Text);
        }
    }


    public class AudioViewHolder extends RecyclerView.ViewHolder {

        private TextView source, date;
        private SeekBar seekBar;
        private ImageView playButton;
        private MediaPlayer mediaPlayer;
        private String Source, Date, audioPath;
        private Runnable runnable;
        private Handler handler;

        public AudioViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            source = (TextView)itemView.findViewById(R.id.source);
            date = (TextView)itemView.findViewById(R.id.date);
            seekBar = (SeekBar) itemView.findViewById(R.id.seekbar);
            playButton = (ImageView) itemView.findViewById(R.id.playbutton);

            handler = new Handler();

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mediaPlayer.seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        public void updateView (AudioComment audioComment){
            // Attach values for each item
            Source = audioComment.getSource();
            if ( !audioComment.block.isEmpty() )
                Source = Source + " : " + audioComment.block;
            Date   = audioComment.getDate();
            audioPath   = audioComment.getAudioName();

            source.setText(Source);
            if ( Source.equals("") )
                source.setText("Project");
            date.setText(Date);

            mediaPlayer = new MediaPlayer();
            try {
                Log.e("audio path", audioPath);
                mediaPlayer.setDataSource(audioPath);
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("MediaPlayer", "player failed");
            }

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    //mediaPlayer.start();
                    //changeSeekbar();
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    seekBar.setProgress(seekBar.getMax());
                }
            });

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("Playbutton", "clicked!");
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        playButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                    } else {
                        mediaPlayer.start();
                        seekBar.setProgress(0);
                        playButton.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                        changeSeekbar();
                    }
                }
            });
        }

        void changeSeekbar () {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());

            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        changeSeekbar();
                    }
                };
                handler.postDelayed(runnable, 500);
            }
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }
}
