package com.example.coffeetime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private final int[] qtes = new int[Carte.NB];             // quantité par boisson
    private final TextView[] tvQtes = new TextView[Carte.NB];
    private TextView tvTotalMenu;
    private String nom = "";
    private int table = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        tvTotalMenu = findViewById(R.id.tvTotalMenu);
        Button btnPanier = findViewById(R.id.btnPanier);

        // Réception du Bundle envoyé par MainActivity
        Bundle recu = getIntent().getBundleExtra(Keys.EXTRA_BUNDLE);
        if (recu != null) {
            nom = recu.getString(Keys.NOM, "");
            table = recu.getInt(Keys.TABLE, 0);
        }
        tvGreeting.setText(getString(R.string.menu_greeting, nom, table));

        // Une carte par boisson : prix + boutons - / +
        brancher(0, R.id.prixEspresso,   R.id.btnMoinsEspresso,   R.id.tvQteEspresso,   R.id.btnPlusEspresso);
        brancher(1, R.id.prixCappuccino, R.id.btnMoinsCappuccino, R.id.tvQteCappuccino, R.id.btnPlusCappuccino);
        brancher(2, R.id.prixLatte,      R.id.btnMoinsLatte,      R.id.tvQteLatte,      R.id.btnPlusLatte);
        majAffichage();

        // Envoi du Bundle vers CartActivity
        btnPanier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Carte.nbArticles(qtes) == 0) {
                    Toast.makeText(MenuActivity.this, R.string.err_panier_vide,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle b = new Bundle();
                b.putString(Keys.NOM, nom);
                b.putInt(Keys.TABLE, table);
                b.putIntArray(Keys.QTES, qtes);

                Intent i = new Intent(MenuActivity.this, CartActivity.class);
                i.putExtra(Keys.EXTRA_BUNDLE, b);
                startActivity(i);
            }
        });
    }

    private void brancher(final int index, int idPrix, int idMoins, int idQte, int idPlus) {
        TextView tvPrix = findViewById(idPrix);
        tvPrix.setText(getString(R.string.price_fmt, Carte.PRIX[index]));
        tvQtes[index] = findViewById(idQte);

        Button moins = findViewById(idMoins);
        Button plus = findViewById(idPlus);

        moins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qtes[index] > 0) {
                    qtes[index]--;
                    majAffichage();
                }
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qtes[index] < Carte.QTE_MAX) {
                    qtes[index]++;
                    majAffichage();
                }
            }
        });
    }

    private void majAffichage() {
        for (int i = 0; i < Carte.NB; i++) {
            tvQtes[i].setText(String.valueOf(qtes[i]));
        }
        tvTotalMenu.setText(getString(R.string.total_label, Carte.total(qtes)));
    }
}
