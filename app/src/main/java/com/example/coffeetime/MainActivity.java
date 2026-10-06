package com.example.coffeetime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editNom = findViewById(R.id.editNom);
        EditText editTable = findViewById(R.id.editTable);
        Button btnCommencer = findViewById(R.id.btnCommencer);

        btnCommencer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nom = editNom.getText().toString().trim();
                String tableTxt = editTable.getText().toString().trim();

                if (nom.isEmpty() || tableTxt.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.err_champs_vides,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle b = new Bundle();
                b.putString(Keys.NOM, nom);
                b.putInt(Keys.TABLE, Integer.parseInt(tableTxt));

                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                intent.putExtra(Keys.EXTRA_BUNDLE, b);
                startActivity(intent);
            }
        });
    }
}
