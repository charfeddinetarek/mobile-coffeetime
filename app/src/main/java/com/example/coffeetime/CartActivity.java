package com.example.coffeetime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class CartActivity extends AppCompatActivity {

    private Bundle recu;
    private int[] qtes = new int[Carte.NB];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        TextView tvLignes = findViewById(R.id.tvLignes);
        TextView tvTotal = findViewById(R.id.tvTotal);
        EditText editNote = findViewById(R.id.editNote);
        RadioGroup radioPaiement = findViewById(R.id.radioPaiement);
        Button btnConfirmer = findViewById(R.id.btnConfirmer);
        Button btnRetour = findViewById(R.id.btnRetour);

        // Réception du Bundle envoyé par MenuActivity
        recu = getIntent().getBundleExtra(Keys.EXTRA_BUNDLE);
        if (recu == null) {          // lancée sans passer par le menu
            finish();
            return;
        }
        int[] q = recu.getIntArray(Keys.QTES);
        if (q != null) qtes = q;

        tvLignes.setText(Carte.lignes(this, qtes));
        tvTotal.setText(getString(R.string.total_label, Carte.total(qtes)));

        btnRetour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Envoi du Bundle vers ProgressActivity
        btnConfirmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton choisi = findViewById(radioPaiement.getCheckedRadioButtonId());
                int duree = Math.min(Carte.DUREE_MAX,
                        Carte.nbArticles(qtes) * Carte.SECONDES_PAR_BOISSON);

                Bundle b = new Bundle();
                b.putString(Keys.NOM, recu.getString(Keys.NOM, ""));
                b.putInt(Keys.TABLE, recu.getInt(Keys.TABLE, 0));
                b.putIntArray(Keys.QTES, qtes);
                b.putDouble(Keys.TOTAL, Carte.total(qtes));
                b.putString(Keys.PAIEMENT, choisi.getText().toString());
                b.putString(Keys.NOTE, editNote.getText().toString().trim());
                b.putInt(Keys.NUMERO, 100 + new Random().nextInt(900));
                b.putInt(Keys.TEMPS, duree);

                Intent i = new Intent(CartActivity.this, ProgressActivity.class);
                i.putExtra(Keys.EXTRA_BUNDLE, b);
                startActivity(i);
                finish();   // la commande est validée : on ne revient pas au panier
            }
        });
    }
}
