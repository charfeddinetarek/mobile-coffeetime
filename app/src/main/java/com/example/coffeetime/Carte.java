package com.example.coffeetime;

import android.content.Context;

public final class Carte {
    private Carte() {}

    public static final int NB = 3;
    public static final int QTE_MAX = 5;
    public static final int SECONDES_PAR_BOISSON = 5;
    public static final int DUREE_MAX = 30;

    public static final int[] NOMS = {
            R.string.drink_espresso, R.string.drink_cappuccino, R.string.drink_latte};
    public static final double[] PRIX = {2.50, 3.50, 4.00};

    public static double total(int[] qtes) {
        double t = 0;
        for (int i = 0; i < NB; i++) t += qtes[i] * PRIX[i];
        return t;
    }

    public static int nbArticles(int[] qtes) {
        int n = 0;
        for (int i = 0; i < NB; i++) n += qtes[i];
        return n;
    }

    /** Une ligne par boisson commandée : "2 × Cappuccino : 7,00 DT" */
    public static String lignes(Context c, int[] qtes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NB; i++) {
            if (qtes[i] > 0) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(c.getString(R.string.line_fmt,
                        qtes[i], c.getString(NOMS[i]), qtes[i] * PRIX[i]));
            }
        }
        return sb.toString();
    }
}
