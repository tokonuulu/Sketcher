package tokonuulu.sketcher.start;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Bundle;
import android.view.FocusFinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;

import de.blox.graphview.Node;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.structureChart.StructureChart;
import yanzhikai.textpath.AsyncTextPathView;
import yanzhikai.textpath.calculator.PathCalculator;
import yanzhikai.textpath.painter.AsyncPathPainter;
import yogesh.firzen.filelister.FileListerDialog;
import yogesh.firzen.filelister.OnFileSelectedListener;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        AsyncTextPathView asyncTextPathView = findViewById(R.id.app_name);
        asyncTextPathView.startAnimation(0,1);

        asyncTextPathView.setPathPainter(new AsyncPathPainter() {
            @Override
            public void onDrawPaintPath(float x, float y, Path paintPath) {
                paintPath.addCircle(x,y,6, Path.Direction.CCW);
            }
        });

        final Button newProject = findViewById(R.id.new_project);
        Button openProject = findViewById(R.id.open_project);

        final FileListerDialog fileListerDialog = FileListerDialog.createFileListerDialog(this);
        fileListerDialog.setDefaultDir(getFilesDir());
        fileListerDialog.setFileFilter(FileListerDialog.FILE_FILTER.DIRECTORY_ONLY);
        fileListerDialog.setOnFileSelectedListener(new OnFileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                Intent intent = new Intent();
                intent.putExtra("project", file.getName());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        openProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileListerDialog.show();
            }
        });
        newProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newProject();
            }
        });
    }

    void newProject () {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StartActivity.this);
        alertDialog.setTitle("New project");
        alertDialog.setMessage("Enter project name");

        final EditText input = new EditText(StartActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_add_box_green_24dp);

        alertDialog.setPositiveButton("Enter",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        String name = input.getText().toString();
                        intent.putExtra("project", name);
                        setResult(RESULT_OK, intent);
                        finish();
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
    public void onBackPressed() {

    }
}
