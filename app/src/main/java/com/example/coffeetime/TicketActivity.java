package com.example.coffeetime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TicketActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        TextView tvNumero = findViewById(R.id.tvNumero);
        TextView tvClient = findViewById(R.id.tvClient);
        TextView tvLignes = findViewById(R.id.tvLignes);
        TextView tvTotal = findViewById(R.id.tvTotal);
        TextView tvPaiement = findViewById(R.id.tvPaiement);
        TextView tvNote = findViewById(R.id.tvNote);
        TextView tvMessage = findViewById(R.id.tvMessage);
        Button btnNouvelle = findViewById(R.id.btnNouvelle);

        // Réception du Bundle transmis par ProgressActivity
        Bundle recu = getIntent().getBundleExtra(Keys.EXTRA_BUNDLE);
        if (recu != null) {
            int table = recu.getInt(Keys.TABLE, 0);
            int[] qtes = recu.getIntArray(Keys.QTES);
            if (qtes == null) qtes = new int[Carte.NB];
            String note = recu.getString(Keys.NOTE, "");

            tvNumero.setText(getString(R.string.order_number, recu.getInt(Keys.NUMERO, 0)));
            tvClient.setText(getString(R.string.ticket_customer,
                    recu.getString(Keys.NOM, ""), table));
            tvLignes.setText(Carte.lignes(this, qtes));
            tvTotal.setText(getString(R.string.total_label, recu.getDouble(Keys.TOTAL, 0.0)));
            tvPaiement.setText(getString(R.string.ticket_payment,
                    recu.getString(Keys.PAIEMENT, "")));
            tvMessage.setText(getString(R.string.ticket_message, table));

            if (note.isEmpty()) {
                tvNote.setVisibility(View.GONE);
            } else {
                tvNote.setText(getString(R.string.ticket_note, note));
            }
        }

        // Nouvelle commande : retour à l'accueil, pile d'activités vidée
        btnNouvelle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TicketActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
    }
}
