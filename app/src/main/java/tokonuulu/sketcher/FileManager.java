package tokonuulu.sketcher;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.blox.graphview.Edge;
import de.blox.graphview.Graph;
import de.blox.graphview.Node;
import tokonuulu.sketcher.blockClass.Class;
import tokonuulu.sketcher.blockClass.Function;
import tokonuulu.sketcher.blockClass.GlobalVariable;
import tokonuulu.sketcher.blockClass.Header;
import tokonuulu.sketcher.blockClass.blockClass;
import tokonuulu.sketcher.commentFeed.AudioComment;
import tokonuulu.sketcher.commentFeed.Comment;
import tokonuulu.sketcher.commentFeed.PictureComment;
import tokonuulu.sketcher.commentFeed.TextComment;

public class FileManager {
    private final String sourceExt = ".str";
    private final String nodeSourceListExt = ".sources";
    private final String structureExt = ".str";
    private final String commentExt = ".comments";

    public Boolean saveSourceData (Context context, String currentProject, String currentSource, List<blockClass> data) {
        File folder = new File( context.getFilesDir()+
                File.separator + currentProject);
        if ( !folder.exists() )
            folder.mkdirs();

        try {
            File destination = new File(folder, currentSource + sourceExt);
            if ( !destination.exists() )
                destination.createNewFile();

            final RuntimeTypeAdapterFactory<blockClass> typeFactory = RuntimeTypeAdapterFactory
                    .of(blockClass.class, "typename") // Here you specify which is the parent class and what field particularizes the child class.
                    .registerSubtype(Header.class) // if the flag equals the class name, you can skip the second parameter. This is only necessary, when the "type" field does not equal the class name.
                    .registerSubtype(Function.class)
                    .registerSubtype(GlobalVariable.class)
                    .registerSubtype(Class.class);

            final Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

            Type type = new TypeToken<ArrayList<blockClass>>() {}.getType();
            String resultString = gson.toJson(data, type);

            Log.v("SAVE_FILE", resultString);

            FileWriter fileWriter = new FileWriter(destination);
            fileWriter.write(resultString);
            fileWriter.close();
            return true;
        }
        catch (IOException e) {
            Log.e("SAVE_FILE", "File write failed: " + e.toString());
            return false;
        }
    }

    public void addNewSource(Context context, String projectName, String sourceName) {
        saveSourceData(context, projectName, sourceName, new ArrayList<blockClass>());
    }
    public void deleteSource(Context context, String projectName, String sourceName) {
        File folder = new File( context.getFilesDir()+
                File.separator + projectName);

        if ( !folder.exists() )
            return;

        File destination = new File(folder, sourceName + sourceExt);
        if ( destination.exists() )
            if (destination.delete())
                Log.e("deleteSource", "File is deleted");

        Log.e("deleteSource", String.valueOf(destination.exists()));
    }

    public List<blockClass> loadSourceData (Context context, String currentProject, String currentSource) {
        File folder = new File( context.getFilesDir()+
                File.separator + currentProject);

        if ( !folder.exists() )
            return null;

        try {
            File destination = new File(folder, currentSource + sourceExt);
            if ( !destination.exists() )
                return null;

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader in = null;

            in = new BufferedReader(new FileReader(destination));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

            String resultString = stringBuilder.toString();

            final RuntimeTypeAdapterFactory<blockClass> typeFactory = RuntimeTypeAdapterFactory
                    .of(blockClass.class, "typename") // Here you specify which is the parent class and what field particularizes the child class.
                    .registerSubtype(Header.class) // if the flag equals the class name, you can skip the second parameter. This is only necessary, when the "type" field does not equal the class name.
                    .registerSubtype(Function.class)
                    .registerSubtype(GlobalVariable.class)
                    .registerSubtype(Class.class);

            final Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

            Type type = new TypeToken<ArrayList<blockClass>>() {}.getType();
            List<blockClass> blockClassList = gson.fromJson(resultString, type);

            Log.v("LOAD_FILE", resultString);
            return blockClassList;
        }
        catch (IOException e) {
            Log.e("LOAD_FILE", "File read failed: " + e.toString());
        }
        return null;
    }

    public ArrayList<String> loadSourcesForNode (Context context, String currentProject, String currentNode) {
        File folder = new File( context.getFilesDir()+
                File.separator + currentProject);

        if ( !folder.exists() )
            return null;

        try {
            File destination = new File(folder, currentNode + nodeSourceListExt);
            if ( !destination.exists() )
                return null;

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader in = null;

            in = new BufferedReader(new FileReader(destination));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

            String resultString = stringBuilder.toString();

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> sourceList = gson.fromJson(resultString, type);

            Log.v("LOAD_SOURCE_FILE_NODE", resultString);
            return sourceList;
        }
        catch (IOException e) {
            Log.e("LOAD_SOURCE_FILE_NODE", "File write failed: " + e.toString());
        }
        return null;
    }

    public Boolean saveSourcesForNode (Context context, String currentProject, String currentNode, List<String> data) {
        File folder = new File( context.getFilesDir()+
                File.separator + currentProject);
        if ( !folder.exists() )
            folder.mkdirs();

        try {
            File destination = new File(folder, currentNode + nodeSourceListExt);
            if ( !destination.exists() )
                destination.createNewFile();

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            String resultString = gson.toJson(data, type);

            Log.v("SAVE_SOURCE_FILE_NODE", resultString);

            FileWriter fileWriter = new FileWriter(destination);
            fileWriter.write(resultString);
            fileWriter.close();
            return true;
        }
        catch (IOException e) {
            Log.e("SAVE_SOURCE_FILE_NODE", "File write failed: " + e.toString());
            return false;
        }
    }

    public Boolean saveStructureData(Context context, String projectName, Graph graph) {
        File folder = new File( context.getFilesDir()+
                File.separator + projectName);
        if ( !folder.exists() )
            folder.mkdirs();

        try {
            Gson gson = new Gson();
            List<String> nodesList = new ArrayList<>();
            List<Pair<String, String>> edgesList = new ArrayList<>();

            if (graph.getNodeCount() != 0) {
                for (Node node : graph.getNodes())
                    nodesList.add(node.getData().toString());

                for (Edge edge : graph.getEdges())
                    edgesList.add(new Pair<String, String>(edge.getSource().getData().toString(), edge.getDestination().getData().toString()));
            }

            String nodesListJSON = gson.toJson(nodesList);
            String edgesListJSON = gson.toJson(edgesList);

            Log.e("SAVE_STR", nodesListJSON);
            Log.e("SAVE_STR", edgesListJSON);

            File nodes = new File(folder, "nodes" + structureExt);
            File edges = new File(folder, "edges" + structureExt);
            if ( !nodes.exists() )
                nodes.createNewFile();
            if ( !edges.exists() )
                edges.createNewFile();

            FileWriter nodeFileWriter = new FileWriter(nodes);
            nodeFileWriter.write(nodesListJSON);
            nodeFileWriter.close();

            FileWriter edgesFileWriter = new FileWriter(edges);
            edgesFileWriter.write(edgesListJSON);
            edgesFileWriter.close();
            return true;
        }
        catch (IOException e) {
            Log.e("SAVE_STR", "File write failed: " + e.toString());
            return false;
        }
    }

    public Graph loadStructureData(Context context, String projectName) {

        File folder = new File( context.getFilesDir()+
                File.separator + projectName);
        if ( !folder.exists() )
            return null;

        try {
            File nodes = new File(folder, "nodes" + structureExt);
            File edges = new File(folder, "edges" + structureExt);
            if ( !nodes.exists() )
                return null;

            Graph graph = new Graph();
            Gson gson = new Gson();

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader in = null;
            Type type;

            /* add the edges if there are any */
            stringBuilder = new StringBuilder();
            in = new BufferedReader(new FileReader(edges));
            while ((line = in.readLine()) != null) stringBuilder.append(line);
            String edgesJSON = stringBuilder.toString();

            ArrayList<Pair<String, String>> edgesList;
            type = new TypeToken<ArrayList<Pair<String, String>>>() {}.getType();
            edgesList = gson.fromJson(edgesJSON, type);

            if (edgesList != null) {
                for (Pair<String, String> edge : edgesList)
                    graph.addEdge(new Node(edge.first), new Node(edge.second));
            }
            else {
                /* Add all the nodes */
                in = new BufferedReader(new FileReader(nodes));
                while ((line = in.readLine()) != null) stringBuilder.append(line);
                String nodesJSON = stringBuilder.toString();
                ArrayList<String> nodesList;
                type = new TypeToken<ArrayList<String>>() {
                }.getType();
                nodesList = gson.fromJson(nodesJSON, type);
                if (nodesList != null)
                    for (String node : nodesList)
                        graph.addNode(new Node(node));

                Log.v("LOAD_STR", nodesJSON);
            }

            Log.v("LOAD_STR", edgesJSON);

            return graph;
        }
        catch (IOException e) {
            Log.e("LOAD_STR", "File read failed: " + e.toString());
        }
        return null;
    }

    public Boolean saveCommentData (Context context, String currentProject, List<Comment> data) {
        File folder = new File( context.getFilesDir()+
                File.separator + currentProject);
        if ( !folder.exists() )
            folder.mkdirs();

        try {
            File destination = new File(folder, commentExt);
            if ( !destination.exists() )
                destination.createNewFile();

            final RuntimeTypeAdapterFactory<Comment> typeFactory = RuntimeTypeAdapterFactory
                    .of(Comment.class, "typename") // Here you specify which is the parent class and what field particularizes the child class.
                    .registerSubtype(TextComment.class) // if the flag equals the class name, you can skip the second parameter. This is only necessary, when the "type" field does not equal the class name.
                    .registerSubtype(AudioComment.class)
                    .registerSubtype(PictureComment.class);

            final Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

            Type type = new TypeToken<ArrayList<Comment>>() {}.getType();
            String resultString = gson.toJson(data, type);

            Log.v("SAVE_COMMENT_FILE", resultString);

            FileWriter fileWriter = new FileWriter(destination);
            fileWriter.write(resultString);
            fileWriter.close();
            return true;
        }
        catch (IOException e) {
            Log.e("SAVE_COMMENT_FILE", "File write failed: " + e.toString());
            return false;
        }
    }

    public List<Comment> loadCommentData (Context context, String currentProject) {
        File folder = new File( context.getFilesDir()+
                File.separator + currentProject);

        if ( !folder.exists() )
            return null;

        try {
            File destination = new File(folder, commentExt);
            if ( !destination.exists() )
                return null;

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader in = null;

            in = new BufferedReader(new FileReader(destination));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

            String resultString = stringBuilder.toString();

            final RuntimeTypeAdapterFactory<Comment> typeFactory = RuntimeTypeAdapterFactory
                    .of(Comment.class, "typename") // Here you specify which is the parent class and what field particularizes the child class.
                    .registerSubtype(TextComment.class) // if the flag equals the class name, you can skip the second parameter. This is only necessary, when the "type" field does not equal the class name.
                    .registerSubtype(AudioComment.class)
                    .registerSubtype(PictureComment.class);

            final Gson gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

            Type type = new TypeToken<ArrayList<Comment>>() {}.getType();
            List<Comment> commentList = gson.fromJson(resultString, type);

            Log.v("LOAD_COMMENT_FILE", resultString);
            return commentList;
        }
        catch (IOException e) {
            Log.e("LOAD_COMMENT_FILE", "File read failed: " + e.toString());
        }
        return null;
    }
}
