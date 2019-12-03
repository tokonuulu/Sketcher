package tokonuulu.sketcher;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tokonuulu.sketcher.blockClass.Class;
import tokonuulu.sketcher.blockClass.Function;
import tokonuulu.sketcher.blockClass.GlobalVariable;
import tokonuulu.sketcher.blockClass.Header;
import tokonuulu.sketcher.blockClass.blockClass;
import tokonuulu.sketcher.blocklist.AddClass;
import tokonuulu.sketcher.blocklist.AddFunction;
import tokonuulu.sketcher.blocklist.AddGVariable;
import tokonuulu.sketcher.blocklist.AddHeader;
import tokonuulu.sketcher.blocklist.ItemMoveCallback;
import tokonuulu.sketcher.blocklist.RecyclerViewAdapter;
import tokonuulu.sketcher.commentFeed.CommentFeed;
import tokonuulu.sketcher.start.StartActivity;
import tokonuulu.sketcher.structureChart.StructureChart;

public class MainActivity extends AppCompatActivity {
    final int ADD_FUNCTION = 1;
    final int ADD_HEADER = 2;
    final int ADD_GVARIABLE = 3;
    final int ADD_CLASS = 4;
    final int PROJECT_CHANGE = 5;

    private String currentProject;
    private String currentSource;
    private List<blockClass> blockList;
    private FileManager fileManager;

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView;
    private ActionBar actionBar;

    ActionBarDrawerToggle mDrawerToggle;
    RecyclerView recyclerView;
    RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.app_name,R.string.app_name){
            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        //menuItem.setChecked(true);
                        selectFragment(menuItem);
                        return true;
                    }
                });


        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        //DividerItemDecoration divider = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        //recyclerView.addItemDecoration(divider);

        //populateRecyclerView();

        getCurrentProject();
        getCurrentSource();

        if (currentProject == null) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivityForResult(intent, PROJECT_CHANGE);
        }

        Log.e("MAIN", "project: " + currentProject);
        Log.e("MAIN", "source: " + currentSource);

        fileManager = new FileManager();

        if (currentSource != null) {
            actionBar.setTitle(currentSource);
            blockList = fileManager.loadSourceData(this, currentProject, currentSource);
        } else {
            Toast.makeText(this, "Source is not selected yet", Toast.LENGTH_SHORT).show();
        }
        if (blockList == null)
            blockList = new ArrayList<>();

        mAdapter = new RecyclerViewAdapter(MainActivity.this, currentProject, currentSource, blockList);
        mAdapter.setContext(MainActivity.this);

        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);

        final SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.inflate(R.menu.main_fab_menu);

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int action;
                switch (actionItem.getId()) {
                    case R.id.add_function:
                        action = ADD_FUNCTION;
                        speedDialView.close();
                        break;
                    case R.id.add_header:
                        action = ADD_HEADER;
                        speedDialView.close();
                        break;
                    case R.id.add_gvariable:
                        action = ADD_GVARIABLE;
                        speedDialView.close();
                        break;
                    case R.id.add_class:
                        action = ADD_CLASS;
                        speedDialView.close();
                        break;
                    default:
                        action = -1;
                        speedDialView.close();
                        break;
                }
                if (currentSource != null) {
                    switch (action) {
                        case ADD_FUNCTION:
                            addFunction();
                            break;
                        case ADD_GVARIABLE:
                            addGVariable();
                            break;
                        case ADD_HEADER:
                            addHeader();
                            break;
                        case ADD_CLASS:
                            addClass();
                            break;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please choose the source first", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

    }

    private void addFunction() {
        Intent intent = new Intent(this, AddFunction.class);
        ArrayList<String> functions = new ArrayList<>();
        for (Object data : blockList)
            if ( data instanceof Function )
                functions.add(((Function) data).getFunctionName());

        intent.putStringArrayListExtra("functions", functions);
        startActivityForResult(intent, ADD_FUNCTION);
    }
    private void addClass() {
        Intent intent = new Intent(this, AddClass.class);
        startActivityForResult(intent, ADD_CLASS);
    }

    private void addGVariable() {
        Intent intent = new Intent(this, AddGVariable.class);
        startActivityForResult(intent, ADD_GVARIABLE);
    }

    private void addHeader() {
        Intent intent = new Intent(this, AddHeader.class);
        startActivityForResult(intent, ADD_HEADER);
    }

    private void populateRecyclerView() {
        blockList = new ArrayList();

        blockList.add(new Header("triangle.h"));
        blockList.add(new Header("load.h"));
        blockList.add(new Header("save.h"));
        blockList.add(new Header("show.h"));

        blockList.add(new GlobalVariable("MAX_VALUE", "INT_MAX", "max number of nodes"));
        blockList.add(new GlobalVariable("MIN_VALUE", "0", "min number of nodes"));
        blockList.add(new GlobalVariable("ALLOW_ACCESS", "TRUE", "should we allow access?"));

        blockList.add(new Function("sort_alph", "this function sorts in alph order\nit's important to keep the order of nodes\ndo it last"));
        blockList.add(new Function("do_it", "this is where the recursion begins"));
        blockList.add(new Function("hello_wordl", "this function outputs hello world"));
        blockList.add(new Function("recur", "actual recursive function"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    void reDrawRecycleView () {
        getCurrentProject();
        getCurrentSource();

        if (currentSource != null)
            actionBar.setTitle(currentSource);
        blockList = fileManager.loadSourceData(this, currentProject, currentSource);

        if (blockList == null)
            blockList = new ArrayList<>();

        mAdapter = new RecyclerViewAdapter(MainActivity.this, currentProject, currentSource, blockList);
        mAdapter.setContext(MainActivity.this);

        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(mAdapter);
    }
    /* After selecting a source file in EditSources intent arrives here */
    @Override
    protected void onNewIntent(Intent intent) {
        String name = intent.getStringExtra("source");
        if ( name != null ) {
            Log.e("MAIN", "Source file changed to " + name);

            setCurrentSource(name);
            getCurrentSource();

            Log.e("MAIN", "We are in " + currentProject + " " + currentSource);

            reDrawRecycleView();
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        //saveSource();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        fileManager.saveSourceData(this, currentProject, currentSource, blockList);
        super.onStop();
    }

    @Override
    protected void onResume() {
        Log.e("MAIN", "resumed");
        if (!isSameProject() || !isSameSource()) {
            Log.e("MAIN", "source changed");
            reDrawRecycleView();
        }

        super.onResume();
    }

    public void selectFragment(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.structure_chart:
                Intent strintent = new Intent(this, StructureChart.class);
                strintent.putExtra("project", currentProject);
                startActivity(strintent);
                break;
            case R.id.close_project:
                setCurrentProject(null);
                setCurrentSource(null);
                Intent intent = new Intent(this, StartActivity.class);
                startActivityForResult(intent, PROJECT_CHANGE);
                break;
            case R.id.comment_feed:
                Intent commentIntent = new Intent(this, CommentFeed.class);
                commentIntent.putExtra("project", currentProject);
                commentIntent.putExtra("source", "");
                commentIntent.putExtra("block", "");
                startActivity(commentIntent);
                break;
            default:
                break;
        }

        //menuItem.setChecked(true);

        mDrawerLayout.closeDrawers();
    }

    Boolean compare(String first, String second) {
        if (first == null && second == null)
            return true;
        if (first != null && second != null)
            return first.equals(second);
        else
            return false;
    }

    /* Both might be NULL here */
    Boolean isSameProject() {
        SharedPreferences sharedPreferences = getSharedPreferences("SKETCHER_MAIN", MODE_PRIVATE);
        String project = sharedPreferences.getString("cur_project", null);

        return compare(currentProject, project);
    }

    /* Both might be NULL here */
    Boolean isSameSource() {
        SharedPreferences sharedPreferences = getSharedPreferences("SKETCHER_MAIN", MODE_PRIVATE);
        String source = sharedPreferences.getString("cur_source", null);

        return compare(currentSource, source);
    }

    void getCurrentProject () {
        SharedPreferences sharedPreferences = getSharedPreferences("SKETCHER_MAIN", MODE_PRIVATE);
        currentProject = sharedPreferences.getString("cur_project", null);
    }
    void getCurrentSource () {
        SharedPreferences sharedPreferences = getSharedPreferences("SKETCHER_MAIN", MODE_PRIVATE);
        currentSource = sharedPreferences.getString("cur_source", null);
    }
    void setCurrentProject (String newProject) {
        SharedPreferences sharedPreferences = getSharedPreferences("SKETCHER_MAIN", MODE_PRIVATE);
        sharedPreferences.edit().putString("cur_project", newProject).commit();
    }
    void setCurrentSource (String newSource) {
        SharedPreferences sharedPreferences = getSharedPreferences("SKETCHER_MAIN", MODE_PRIVATE);
        sharedPreferences.edit().putString("cur_source", newSource).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_FUNCTION:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    String desc = data.getStringExtra("desc");
                    ArrayList<String> funcUsed = data.getStringArrayListExtra("used");

                    Log.v("FUNC_ADD", name + " " + desc);

                    blockList.add(new Function(name, desc));
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case ADD_GVARIABLE:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    String desc = data.getStringExtra("desc");
                    String value = data.getStringExtra("value");

                    blockList.add(new GlobalVariable(name, value, desc));
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case ADD_HEADER:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    String desc = data.getStringExtra("desc");

                    blockList.add(0, new Header(name, desc));
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case ADD_CLASS:
                if (resultCode == RESULT_OK) {
                    String name = data.getStringExtra("name");
                    String desc = data.getStringExtra("desc");

                    blockList.add(new Class(name, desc));
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case PROJECT_CHANGE:
                if (resultCode == RESULT_OK) {
                    String project = data.getStringExtra("project");
                    setCurrentProject(project);
                    setCurrentSource(null);

                    reDrawRecycleView();
                }
                break;
        }
    }
}
