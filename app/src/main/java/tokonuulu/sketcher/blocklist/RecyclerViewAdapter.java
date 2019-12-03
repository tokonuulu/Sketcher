package tokonuulu.sketcher.blocklist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.blockClass.Function;
import tokonuulu.sketcher.blockClass.GlobalVariable;
import tokonuulu.sketcher.blockClass.Header;
import tokonuulu.sketcher.blockClass.blockClass;
import tokonuulu.sketcher.structureChart.StructureChart;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    private final static int TYPE_GLOBAL = 1, TYPE_FUNCTION = 2, TYPE_HEADER = 3;
    private List<blockClass> data = new ArrayList();
    private Context context;

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

    public RecyclerViewAdapter(List<blockClass> data) {
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
            default:
                viewHolder = null;
                break;
        }
        return viewHolder;
    }

    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        int viewType = holder.getItemViewType();
        switch (viewType){
            case TYPE_GLOBAL:
                GlobalVariable globalVariable = (GlobalVariable) data.get(position);
                ((GVariableViewHolder) holder).updateView(globalVariable);
                break;
            case TYPE_FUNCTION:
                Function function =(Function) data.get(position);
                ((FunctionViewHolder) holder).updateView(function);
                break;
            case TYPE_HEADER:
                Header header = (Header) data.get(position);
                ((HeaderViewHolder) holder).updateView(header);
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle("Delete block");
                alertDialog.setMessage("Do you want to remove this block?");

                alertDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
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