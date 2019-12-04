package tokonuulu.sketcher.structureChart;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import de.blox.graphview.BaseGraphAdapter;
import de.blox.graphview.Graph;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;
import de.blox.graphview.ViewHolder;
import de.blox.graphview.tree.BuchheimWalkerAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerConfiguration;
import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.source.EditSources;

public class StructureChardFragment extends Fragment {
    private int nodeCount = 1;
    private Graph graph;
    private Node currentNode;
    private FileManager fileManager;
    BaseGraphAdapter<ViewHolder> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View rootview = inflater.inflate(R.layout.activity_structure_chart, container, false);

        /* Initiating speed dial */
        final SpeedDialView speedDialView = rootview.findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.fab_menu);

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
                    default:
                        action = -1;
                        break;
                }
                if (action == 1) addNode();
                else if (action == 2) removeNode();
                return true;
            }
        });

        final GraphView graphView = rootview.findViewById(R.id.graph);

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
        final Tabbed tabbed = (Tabbed) getActivity();

        graph = fileManager.loadStructureData(getActivity(), tabbed.currentProject);
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
                tabbed.currentNode = currentNode.getData().toString();
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

        return rootview;
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
            Toast.makeText(getActivity(), "Parent has not been selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("New block");
        alertDialog.setMessage("Enter block's name");

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
            Toast.makeText(getActivity(), "Block has not been selected!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        Tabbed tabbed = (Tabbed) getActivity();
        fileManager.saveStructureData(getActivity(), tabbed.currentProject, graph);
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
