package tokonuulu.sketcher.commentFeed;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.deskode.recorddialog.RecordDialog;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.blox.graphview.Node;
import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.source.sourceListAdapter;
import tokonuulu.sketcher.structureChart.StructureChart;

public class CommentFeed extends AppCompatActivity {
    private RecyclerView recyclerView;
    private commentListAdapter adapter;
    private List<Comment> commentList;
    private List<Comment> targetList;
    final int ADD_TEXT = 1, ADD_AUDIO = 2, ADD_PICTURE = 3;
    private FileManager fileManager;
    private String currentProject;
    private String Source;
    private String Block;
    private Spinner source, block;
    private ArrayList<String> sourceList;
    private ArrayList<String> blockList;

    private ArrayAdapter<String> sourceAdapter;
    private ArrayAdapter<String> blockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_feed);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);


        fileManager = new FileManager();

        Intent intent = getIntent();
        currentProject = intent.getStringExtra("project");
        Source = intent.getStringExtra("source");
        Block = intent.getStringExtra("block");

        /* Load comment data */
        commentList = fileManager.loadCommentData(this, currentProject);
        if (commentList == null)
            commentList = new ArrayList<>();


        /* Setting up source spinner adapter */
        sourceList = getSourceSet();
        if (Source != null && !sourceList.contains(Source))
                sourceList.add(0, Source);
        sourceAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, sourceList);
        sourceAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        source = findViewById(R.id.source);
        source.setAdapter(sourceAdapter);
        setSelectedSource();

        /* Setting up block spinner adapter */
        blockList = getBlockSet((String)source.getSelectedItem());
        if (Block != null && !blockList.contains(Block))
            blockList.add(0, Block);
        blockAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, blockList);
        blockAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        block = findViewById(R.id.block);
        block.setAdapter(blockAdapter);
        setSelectedBlock();

        /* Setting up the list */
        sortCommentsBy(Source, Block);

        source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choosen = (String) parent.getItemAtPosition(position);
                if (!Source.equals(choosen)) {
                    Source = choosen;
                    blockList = getBlockSet(choosen);
                    Block = "";
                    drawBlockList();
                    sortCommentsBy(Source, (String) block.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        block.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Block = (String) parent.getItemAtPosition(position);
                sortCommentsBy(Source, Block);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.comment_fab_menu);

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int action;
                switch (actionItem.getId()) {
                    case R.id.add_text:
                        action = ADD_TEXT;
                        speedDialView.close();
                        break;
                    case R.id.add_audio:
                        action = ADD_AUDIO;
                        speedDialView.close();
                        break;
                    case R.id.add_picture:
                        action = ADD_PICTURE;
                        speedDialView.close();
                        break;
                    default:
                        action = -1;
                        speedDialView.close();
                        break;
                }

                if (action == ADD_TEXT) addTextComment();
                else if (action == ADD_AUDIO) addAudioComment();
                else if (action == ADD_PICTURE) addPictureComment();

                return true;
            }
        });

    }

    void drawBlockList () {
        blockAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, blockList);
        blockAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        block = findViewById(R.id.block);
        block.setAdapter(blockAdapter);
        setSelectedBlock();
    }

    ArrayList<String> getSourceSet () {
        Set<String> sourceSet = new HashSet<String>();

        for (Comment comment : commentList)
            sourceSet.add(comment.source);

        if (!sourceSet.contains(""))
            sourceSet.add("");
        return new ArrayList<String>(sourceSet);
    }
    ArrayList<String> getBlockSet (String source) {
        Set<String> blockSet = new HashSet<>();

        for (Comment comment : commentList) {
            if (comment.source.equals(source))
                blockSet.add(comment.block);
        }

        if(!blockSet.contains(""))
            blockSet.add("");
        return new ArrayList<String>(blockSet);
    }

    public void setSelectedSource () {
        for (int i=0; i<sourceAdapter.getCount(); i++)
            if (sourceAdapter.getItem(i).equals(Source)) {
                source.setSelection(i);
                return;
            }
    }

    public void setSelectedBlock () {
        for (int i=0; i<blockAdapter.getCount(); i++)
            if (blockAdapter.getItem(i).equals(Block)) {
                block.setSelection(i);
                return;
            }
    }

    @Override
    protected void onStop() {
        fileManager.saveCommentData(this, currentProject, commentList);
        super.onStop();
    }

    void sortCommentsBy (String source, String block) {
        List<Comment> newList = new ArrayList<>();
        for (Comment comment : commentList) {
            if ( !source.equals("") ) {
                if (comment.source.equals(source))
                    if (block.equals(""))
                        newList.add(comment);
                    else if (comment.block.equals(block))
                        newList.add(comment);
            }
            else newList.add(comment);
        }
        targetList = newList;
        adapter = new commentListAdapter(this, targetList, commentList);
        recyclerView.setAdapter(adapter);
    }

    void addPictureComment () {
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, ADD_PICTURE);
    }

    void addTextComment () {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CommentFeed.this);
        alertDialog.setTitle("New comment");
        alertDialog.setMessage("Enter your comment");

        final EditText input = new EditText(CommentFeed.this);
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
                        Calendar calendar = Calendar.getInstance();
                        String date = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                        commentList.add(new TextComment(Source, Block, date, name));
                        targetList.add(new TextComment(Source, Block, date, name));
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

    void addAudioComment () {

            RecordDialog recordDialog = RecordDialog.newInstance("Record Audio");
        recordDialog.setMessage("Press to record");
        recordDialog.show(CommentFeed.this.getFragmentManager(),"TAG");
        recordDialog.setPositiveButton("Save", new RecordDialog.ClickListener() {
            @Override
            public void OnClickListener(String path) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                File from = new File(path);
                File folder = new File( getFilesDir()+
                        File.separator + currentProject);
                File to = new File(folder, timeStamp);
                try {
                    Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Toast.makeText(CommentFeed.this, "Save audio: " + to, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e("COMMENT_FEED", "could not move the file");
                }
                Calendar calendar = Calendar.getInstance();
                String date = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                commentList.add(new AudioComment(Source, Block, date, to.getPath()));
                targetList.add(new AudioComment(Source, Block, date, to.getPath()));
                adapter.notifyDataSetChanged();
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ADD_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                //Get the column index of MediaStore.Images.Media.DATA
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //Gets the String value in the column
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                Calendar calendar = Calendar.getInstance();
                String date = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());

                Log.e("IMAGE FILE", imgDecodableString);

                commentList.add(0, new PictureComment(Source, Block, date, imgDecodableString));
                targetList.add(0, new PictureComment(Source, Block, date, imgDecodableString));
                adapter.notifyDataSetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
