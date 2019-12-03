package tokonuulu.sketcher.structureChart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import tokonuulu.sketcher.R;

public class popUpAddNode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up_add_node);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width*.7), (int) (height*.325));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);

        final EditText editText = findViewById(R.id.node_name);

        Button add = findViewById(R.id.add);
        Button cancel = findViewById(R.id.cancel);

        Intent parentIntent = getIntent();
        List<String> nodes = parentIntent.getStringArrayListExtra("nodes");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, nodes);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String name = editText.getText().toString();

                if (!name.isEmpty()) {
                    intent.putExtra("node", name);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

    }
}
