package tokonuulu.sketcher.blocklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import tokonuulu.sketcher.R;

public class AddHeader extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_header);

        final EditText name = findViewById(R.id.name);
        final EditText desc = findViewById(R.id.desc);

        Button enter = findViewById(R.id.enter);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                //Log.v("ADD_FUNC", funcName.getText().toString());

                intent.putExtra("name", name.getText().toString());
                intent.putExtra("desc", desc.getText().toString());

                setResult(RESULT_OK, intent);

                finish();
            }
        });
    }
}
