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

public class AddGVariable extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gvariable);

        final EditText name = findViewById(R.id.name);
        final EditText desc = findViewById(R.id.desc);
        final EditText value = findViewById(R.id.value);

        Button enter = findViewById(R.id.enter);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                //Log.v("ADD_FUNC", funcName.getText().toString());

                intent.putExtra("name", name.getText().toString());
                intent.putExtra("desc", desc.getText().toString());
                intent.putExtra("value", value.getText().toString());

                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }
}
