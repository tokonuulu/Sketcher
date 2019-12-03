package tokonuulu.sketcher.source;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.R;

public class EditSources extends AppCompatActivity {
    private RecyclerView recyclerView;
    private sourceListAdapter adapter;
    private ArrayList<String> sourceList;
    private FileManager fileManager;
    private String currentProject, currentNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sources);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        fileManager = new FileManager();

        Intent intent = getIntent();
        currentProject = intent.getStringExtra("project");
        currentNode = intent.getStringExtra("node");

        sourceList = fileManager.loadSourcesForNode(this, currentProject, currentNode);
        if (sourceList == null)
            sourceList = new ArrayList<>();

        adapter = new sourceListAdapter(this, currentProject, sourceList);
        recyclerView.setAdapter(adapter);

        Button addSource = findViewById(R.id.add_source);
        addSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSource();
            }
        });
    }

    void newSource () {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("New source");
        alertDialog.setMessage("Enter source name");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_add_box_green_24dp);

        alertDialog.setPositiveButton("Enter",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        if ( !name.isEmpty() ) {
                            sourceList.add(name);
                            fileManager.addNewSource(EditSources.this, currentProject, name);
                        }
                        else
                            Snackbar.make(recyclerView,  "Source name cannot be empty", Snackbar.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
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
    @Override
    protected void onStop() {
        fileManager.saveSourcesForNode(this, currentProject, currentNode, sourceList);
        super.onStop();
    }
}
