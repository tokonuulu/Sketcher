package tokonuulu.sketcher.sourceView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import io.github.kbiakov.codeview.CodeView;
import io.github.kbiakov.codeview.adapters.Options;
import io.github.kbiakov.codeview.highlight.ColorTheme;
import tokonuulu.sketcher.FileManager;
import tokonuulu.sketcher.R;
import tokonuulu.sketcher.blockClass.Class;
import tokonuulu.sketcher.blockClass.Function;
import tokonuulu.sketcher.blockClass.GlobalVariable;
import tokonuulu.sketcher.blockClass.Header;
import tokonuulu.sketcher.blockClass.blockClass;

public class SourceView extends AppCompatActivity {
    private String currentProject;
    private String currentSource;
    private String sourceCode;
    CodeView codeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        currentProject = intent.getStringExtra("project");
        currentSource = intent.getStringExtra("source");

        codeView = findViewById(R.id.code_view);
        //sourceCode = "#include <iostream>\nusing namespace std;\nint main() {\n\tint a, b;\n\tcin >> a >> b;\n\tcout << a+b << endl;\n\treturn 0;\n}";

        FileManager fileManager = new FileManager();
        List<blockClass> blockList = fileManager.loadSourceData(this, currentProject, currentSource);
        if (blockList == null)
            blockList = new ArrayList<>();

        sourceCode = "";
        if (!blockList.isEmpty()) {
            Boolean namespace = false;
            for (blockClass block : blockList) {
                if (block instanceof Header)
                    sourceCode = sourceCode + "#include < " + ((Header) block).getHeaderName() + " >\n";
                else {
                    if ( !namespace ) {
                        sourceCode = sourceCode + "using namespace std;\n";
                        namespace = true;
                    }
                    if (block instanceof Function)
                        sourceCode = sourceCode + "void " + ((Function) block).getFunctionName() + " () {\n\n}\n";
                    if (block instanceof GlobalVariable)
                        sourceCode = sourceCode + "int " + ((GlobalVariable) block).getVariableName() +
                                " = " + ((GlobalVariable) block).getVariableValue() + "\n";
                    if (block instanceof Class)
                        sourceCode = sourceCode + "class " + ((Class) block).getClassName() + " {\nprivate:\npublic:\n};\n";
                }
            }
        }

        codeView.setOptions(Options.Default.get(this)
                .withLanguage("cpp")
                .withCode(sourceCode)
                .withTheme(ColorTheme.MONOKAI));
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
