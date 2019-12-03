package tokonuulu.sketcher.blocklist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.blockClass.Function;
import tokonuulu.sketcher.blockClass.GlobalVariable;
import tokonuulu.sketcher.blockClass.Header;
import tokonuulu.sketcher.blockClass.blockClass;
import tokonuulu.sketcher.blockClass.Class;
import tokonuulu.sketcher.commentFeed.CommentFeed;
import tokonuulu.sketcher.structureChart.StructureChart;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    private final static int TYPE_GLOBAL = 1, TYPE_FUNCTION = 2, TYPE_HEADER = 3, TYPE_CLASS = 4;
    private List<blockClass> data = new ArrayList();
    private Context context;
    private blockClass mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;
    private Activity mActivity;
    private String project, source;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        View rowView;

        public MyViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.txtTitle);
        }
    }

    public RecyclerViewAdapter(Context context) {
        this.context = context;
    }

    public RecyclerViewAdapter(Activity activity, String project, String source, List<blockClass> data) {
        this.mActivity = activity;
        this.source = source;
        this.project = project;
        this.data = data;
    }

    @Override
    public int getItemViewType (int position) {
        if (data.get(position) instanceof GlobalVariable) {
            return TYPE_GLOBAL;
        } else if (data.get(position) instanceof Function) {
            return TYPE_FUNCTION;
        } else if (data.get(position) instanceof Header) {
            return TYPE_HEADER;
        } else if (data.get(position) instanceof Class) {
            return TYPE_CLASS;
        }
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
            case TYPE_GLOBAL:
                layout = R.layout.global_variables_view;
                View gvariableView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(layout, parent, false);
                viewHolder = new GVariableViewHolder(gvariableView);
                break;
            case TYPE_FUNCTION:
                layout = R.layout.function_view;
                View functionView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(layout, parent, false);
                viewHolder = new FunctionViewHolder(functionView);
                break;
            case TYPE_HEADER:
                layout = R.layout.header_view;
                View headerView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(layout, parent, false);
                viewHolder = new HeaderViewHolder(headerView);
                break;
            case TYPE_CLASS:
                layout = R.layout.class_view;
                View classView = LayoutInflater
                        .from(parent.getContext())
                        .inflate(layout, parent, false);
                viewHolder = new ClassViewHolder(classView);
                break;
            default:
                viewHolder = null;
                break;
        }
        return viewHolder;
    }

    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int viewType = holder.getItemViewType();
        String name = null;
        switch (viewType){
            case TYPE_GLOBAL:
                GlobalVariable globalVariable = (GlobalVariable) data.get(position);
                ((GVariableViewHolder) holder).updateView(globalVariable);
                name = globalVariable.getVariableName();
                break;
            case TYPE_FUNCTION:
                Function function =(Function) data.get(position);
                ((FunctionViewHolder) holder).updateView(function);
                name = function.getFunctionName();
                break;
            case TYPE_HEADER:
                Header header = (Header) data.get(position);
                ((HeaderViewHolder) holder).updateView(header);
                name = header.getHeaderName();
                break;
            case TYPE_CLASS:
                Class aClass = (Class) data.get(position);
                ((ClassViewHolder) holder).updateView(aClass);
                name = aClass.getClassName();
                break;
        }
        final String tmp = name;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, CommentFeed.class);
                intent.putExtra("project", project);
                intent.putExtra("source", source);
                Log.e("MAINADAPTER", "sending " + tmp +" as block");
                intent.putExtra("block", tmp);
                mActivity.startActivity(intent);
            }
        });
    }

    /* defining view holder classes */
    public class GVariableViewHolder extends RecyclerView.ViewHolder {

        private TextView variableName,variableValue, variableDesc;

        public GVariableViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            variableName=(TextView)itemView.findViewById(R.id.variable_name);
            variableValue=(TextView)itemView.findViewById(R.id.variable_value);
            variableDesc=(TextView)itemView.findViewById(R.id.variable_description);
        }

        public void updateView (GlobalVariable globalVariable){
            // Attach values for each item
            String Name   = globalVariable.getVariableName();
            String Value  = globalVariable.getVariableValue();
            String Desc   = globalVariable.getVariableDesc();

            variableName.setText(Name);
            variableValue.setText(Value);
            variableDesc.setText(Desc);
        }
    }

    public class FunctionViewHolder extends RecyclerView.ViewHolder {

        private TextView functionName,functionDesc;

        public FunctionViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            functionName = (TextView)itemView.findViewById(R.id.function_name);
            functionDesc =(TextView)itemView.findViewById(R.id.function_description);
        }

        public void updateView (Function function){
            // Attach values for each item
            String Name   = function.getFunctionName();
            String Desc   = function.getFunctionDecs();

            functionName.setText(Name);
            functionDesc.setText(Desc);
        }
    }

    public class ClassViewHolder extends RecyclerView.ViewHolder {

        private TextView className,classDesc;

        public ClassViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            className = (TextView)itemView.findViewById(R.id.class_name);
            classDesc =(TextView)itemView.findViewById(R.id.class_description);
        }

        public void updateView (Class aClass){
            // Attach values for each item
            String Name   = aClass.getClassName();
            String Desc   = aClass.getClassDesc();

            className.setText(Name);
            classDesc.setText(Desc);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView headerName,headerDesc;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            // Initiate view
            headerName = (TextView)itemView.findViewById(R.id.header_name);
            headerDesc =(TextView)itemView.findViewById(R.id.header_description);
        }

        public void updateView (Header header){
            // Attach values for each item
            String Name   = header.getHeaderName();
            String Desc   = header.getHeaderDesc();

            headerName.setText("#include< " + Name + " >");

            if (headerDesc != null ) {
                headerDesc.setText(Desc);
                headerDesc.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public void onViewSwiped(int position) {
        mRecentlyDeletedItem = data.get(position);
        mRecentlyDeletedItemPosition = position;
        data.remove(position);
        notifyItemRemoved(position);
        showUndoSnackbar();
    }

    private void showUndoSnackbar() {
        View view = mActivity.findViewById(R.id.drawer_layout);
        Snackbar snackbar = Snackbar.make(view, "Block removed",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoDelete();
            }
        });
        snackbar.show();
    }

    private void undoDelete() {
        data.add(mRecentlyDeletedItemPosition,
                mRecentlyDeletedItem);
        notifyItemInserted(mRecentlyDeletedItemPosition);
    }
    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (data.get(fromPosition) instanceof Header ||
                data.get(toPosition) instanceof  Header)
            return;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {

                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void setContext (Context context) {
        this.context = context;
    }

    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.WHITE);

    }
}