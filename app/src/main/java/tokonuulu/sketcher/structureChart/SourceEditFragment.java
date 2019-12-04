package tokonuulu.sketcher.structureChart;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.source.EditSources;
import tokonuulu.sketcher.source.sourceListAdapter;

public class SourceEditFragment extends Fragment {
    private RecyclerView recyclerView;
    private sourceListAdapter adapter;
    private ArrayList<String> sourceList;
    private FileManager fileManager;
    private String currentProject, currentNode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.activity_edit_sources, container, false);

        recyclerView = rootview.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        fileManager = new FileManager();
        Tabbed tabbed = (Tabbed) getActivity();
        currentProject = tabbed.currentProject;
        currentNode = tabbed.currentNode;
        sourceList = fileManager.loadSourcesForNode(getActivity(), currentProject, currentNode);
        if (sourceList == null)
            sourceList = new ArrayList<>();

        adapter = new sourceListAdapter(getActivity(), currentProject, sourceList);
        recyclerView.setAdapter(adapter);

        Button addSource = rootview.findViewById(R.id.add_source);
        addSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newSource();
            }
        });
        return rootview;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            Tabbed tabbed = (Tabbed) getActivity();
            currentProject = tabbed.currentProject;
            currentNode = tabbed.currentNode;
            sourceList = fileManager.loadSourcesForNode(getActivity(), currentProject, currentNode);
            if (sourceList == null)
                sourceList = new ArrayList<>();

            adapter = new sourceListAdapter(getActivity(), currentProject, sourceList);
            recyclerView.setAdapter(adapter);
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void newSource () {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("New source");
        alertDialog.setMessage("Enter source name");

        final EditText input = new EditText(getActivity());
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
                            fileManager.addNewSource(getActivity(), currentProject, name);
                            fileManager.saveSourcesForNode(getActivity(), currentProject, currentNode, sourceList);
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
    public void onPause() {
        fileManager.saveSourcesForNode(getActivity(), currentProject, currentNode, sourceList);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStackImmediate();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
