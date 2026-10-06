package com.example.coffeetime;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvProgress, tvEtat;
    private Button btnTicket;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Bundle recu;
    private int progression = 0;
    private int dureeSecondes = 10;
    private boolean enCours = false;

    // 100 étapes : intervalle = durée * 1000 / 100 = durée * 10 ms
    private int intervalleMs() {
        return dureeSecondes * 10;
    }

    private final Runnable miseAJour = new Runnable() {
        @Override
        public void run() {
            if (progression < 100) {
                progression++;
                progressBar.setProgress(progression);
                tvProgress.setText(progression + " %");
            }
            if (progression < 100) {
                handler.postDelayed(this, intervalleMs());
            } else {
                enCours = false;
                tvEtat.setText(R.string.state_ready);
                btnTicket.setVisibility(View.VISIBLE);   // la commande est prête
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        tvEtat = findViewById(R.id.tvEtat);
        btnTicket = findViewById(R.id.btnTicket);
        TextView tvTitre = findViewById(R.id.tvTitre);
        TextView tvInfo = findViewById(R.id.tvInfo);

        // Réception du Bundle envoyé par CartActivity
        recu = getIntent().getBundleExtra(Keys.EXTRA_BUNDLE);
        if (recu == null) {
            finish();
            return;
        }
        dureeSecondes = Math.max(1, recu.getInt(Keys.TEMPS, 10));
        int[] qtes = recu.getIntArray(Keys.QTES);
        int nb = (qtes != null) ? Carte.nbArticles(qtes) : 0;

        tvTitre.setText(getString(R.string.order_number, recu.getInt(Keys.NUMERO, 0)));
        tvInfo.setText(getString(R.string.progress_info, recu.getInt(Keys.TABLE, 0), nb));

        // Transmission du même Bundle vers TicketActivity
        btnTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProgressActivity.this, TicketActivity.class);
                i.putExtra(Keys.EXTRA_BUNDLE, recu);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!enCours && progression < 100) {
            enCours = true;
            handler.postDelayed(miseAJour, intervalleMs());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        enCours = false;
        handler.removeCallbacks(miseAJour);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
