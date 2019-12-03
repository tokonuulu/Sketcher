package tokonuulu.sketcher.blocklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.hootsuite.nachos.NachoTextView;

import java.util.ArrayList;

import tokonuulu.sketcher.R;

public class AddFunction extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_function);

        final EditText funcName = findViewById(R.id.func_name);
        final EditText funcDesc = findViewById(R.id.func_desc);

        NachoTextView nachoTextView = findViewById(R.id.nacho_text_view);
        Intent parentIntent = getIntent();
        ArrayList<String> suggestions = parentIntent.getStringArrayListExtra("functions");

        //String[] suggestions = new String[]{"Tortilla Chips", "Melted Cheese", "Salsa", "Guacamole", "Mexico", "Jalapeno"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        nachoTextView.setAdapter(adapter);
        nachoTextView.setNachoValidator(null);

        Button enter = findViewById(R.id.enter);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Log.v("ADD_FUNC", funcName.getText().toString());

                intent.putExtra("name", funcName.getText().toString());
                intent.putExtra("desc", funcDesc.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
