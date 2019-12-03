package tokonuulu.sketcher.source;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.MainActivity;
import tokonuulu.sketcher.R;

public class sourceListAdapter extends RecyclerView.Adapter<sourceListAdapter.ItemViewHolder> {
    private ArrayList<String> data;
    private Context context;
    private String currentProject;

    public sourceListAdapter(Context context, String project, ArrayList<String> data) {
        this.data = data;
        this.context = context;
        this.currentProject = project;
    }

    @NonNull
    @Override
    public sourceListAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.tmp_list_item, parent, false);
        return new sourceListAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {
        final String name = data.get(position);
        holder.updateView(name);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle("Delete source");
                alertDialog.setMessage("Do you want to remove this source?");

                alertDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FileManager fileManager = new FileManager();
                                SharedPreferences sharedPreferences = context.getSharedPreferences("SKETCHER_MAIN", Context.MODE_PRIVATE);
                                String currentSource = sharedPreferences.getString("cur_source", null);

                                if (currentSource != null && currentSource.equals(name))
                                    sharedPreferences.edit().putString("cur_source", null).commit();

                                fileManager.deleteSource(context, currentProject, name);
                                data.remove(position);
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

                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle("Open source");
                alertDialog.setMessage("Do you want to open this source?");

                alertDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("source", name);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                context.startActivity(intent);
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView sourceName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            sourceName=(TextView)itemView.findViewById(R.id.txtTitle);
        }

        public void updateView (String name){
            sourceName.setText(name);
        }
    }

};