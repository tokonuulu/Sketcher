package tokonuulu.sketcher.structureChart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.blox.graphview.BaseGraphAdapter;
import de.blox.graphview.Edge;
import de.blox.graphview.Graph;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;
import de.blox.graphview.ViewHolder;
import de.blox.graphview.energy.FruchtermanReingoldAlgorithm;
import de.blox.graphview.layered.SugiyamaAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerConfiguration;
import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.MainActivity;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.source.EditSources;

public class StructureChart extends AppCompatActivity {
    private int nodeCount = 1;
    private Graph graph;
    private Node currentNode;
    private String currentProject;
    private FileManager fileManager;
    BaseGraphAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_structure_chart);

        /* Initiating speed dial */
        final SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.fab_menu);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        currentProject = getIntent().getStringExtra("project");
        Log.e("StructureChart", "project retrived from intent " + currentProject);

        /*
        * TODO: need to implement data sync
        * */

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int action;
                switch (actionItem.getId()) {
                    case R.id.add_node:
                        action = 1;
                        speedDialView.close();
                        break;
                    case R.id.remove_node:
                        action = 2;
                        speedDialView.close();
                        break;
                    case R.id.open_sources:
                        action = 3;
                        speedDialView.close();
                        break;
                    default:
                        action = -1;
                        break;
                }
                if (action == 1) addNode();
                else if (action == 2) removeNode();
                else if (action == 3) openSources();
                return true;
            }
        });

        final GraphView graphView = findViewById(R.id.graph);

        // example tree
        /*final Node node1 = new Node("Main");
        final Node node2 = new Node("Load");
        final Node node3 = new Node("Process");
        final Node node4 = new Node("Show");
        final Node node5 = new Node("YE boy");

        graph.addEdge(node1, node2);
        graph.addEdge(node1, node3);
        graph.addEdge(node1, node4);*/

        fileManager = new FileManager();
        graph = fileManager.loadStructureData(this, currentProject);
        if (graph == null)
            graph = new Graph();

        Log.e("after loading", "the size of graph is "+ graph.getNodeCount());

        // you can set the graph via the constructor or use the adapter.setGraph(Graph) method
        adapter = new BaseGraphAdapter<ViewHolder>(graph) {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.node_layout, parent, false);
                return new SimpleViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, final Object data, final int position) {
                ((SimpleViewHolder)viewHolder).textView.setText(data.toString());
            }
        };

        graphView.setAdapter(adapter);
        graphView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentNode = adapter.getNode(position);
                Snackbar.make(graphView,  currentNode.getData().toString() + " has been selected", Snackbar.LENGTH_SHORT).show();
            }
        });

        /*graphView.setOnItemClickListener((parent, view, position, id) -> {
            currentNode = adapter.getNode(position);
            Toast.makeText(this, "Clicked on " + currentNode.getData().toString(), Toast.LENGTH_SHORT).show();
        });*/

        // set the algorithm here
        final BuchheimWalkerConfiguration configuration = new BuchheimWalkerConfiguration.Builder()
                .setSiblingSeparation(100)
                .setLevelSeparation(300)
                .setSubtreeSeparation(300)
                .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
                .build();
        adapter.setAlgorithm(new BuchheimWalkerAlgorithm(configuration));
    }

    private void openSources() {
        if (currentNode == null && graph.getNodeCount() != 0) {
            Toast.makeText(StructureChart.this, "Parent has not been selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EditSources.class);
        intent.putExtra("project", currentProject);
        intent.putExtra("node", currentNode.getData().toString());
        startActivity(intent);
    }

    class SimpleViewHolder extends ViewHolder {
        TextView textView;

        SimpleViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    private String getNodeText() {
        return "Node " + nodeCount++;
    }

    void addNode() {

        if (currentNode == null && graph.getNodeCount() != 0) {
            Toast.makeText(StructureChart.this, "Parent has not been selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StructureChart.this);
        alertDialog.setTitle("New block");
        alertDialog.setMessage("Enter block's name");

        final EditText input = new EditText(StructureChart.this);
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
                        final Node newNode = new Node(name);
                        if (currentNode != null) {
                            graph.addEdge(currentNode, newNode);
                        } else {
                            graph.addNode(newNode);
                            adapter.notifyInvalidated();
                            //Toast.makeText(StructureChart.this, "Parent has not been selected!", Toast.LENGTH_SHORT).show();
                        }
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
    void removeNode() {
        if (currentNode != null) {
            graph.removeNode(currentNode);
            currentNode = null;
        } else {
            Toast.makeText(StructureChart.this, "Block has not been selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        fileManager.saveStructureData(this, currentProject, graph);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}