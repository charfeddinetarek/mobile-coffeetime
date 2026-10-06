# TP1 + TP2 : CoffeeTime (version avec écrans utiles)

## 1. Réponse à ta question : est-ce que le contenu des pages est imposé ?

**Non.** Le TP2 impose seulement :
- 3 activités du TP1 à améliorer ;
- 2 nouvelles activités avec un **LinearLayout et un RelativeLayout imbriqués** ;
- une **communication entre activités avec un Bundle** (envoi et réception) ;
- défis : une version à une seule activité, et une version multilingue.

Le contenu de chaque écran est libre. Dans la version précédente, la page « Profil » ne faisait que réafficher le nom saisi, et le « Récapitulatif » répétait la commande : ils servaient surtout à illustrer le guide. Dans cette version, **chaque écran a un rôle réel** dans le parcours d'un client.

## 2. Parcours de l'application

Le client s'installe à une table et commande sur son téléphone.

| # | Activité | Origine TP | À quoi elle sert | Layouts |
|---|---|---|---|---|
| 1 | `MainActivity` | TP1 | **Identification** : nom et numéro de table | RelativeLayout (bandeau) + LinearLayout (formulaire) |
| 2 | `MenuActivity` | TP1 | **Menu** : cartes des boissons avec prix, boutons − / + pour choisir les quantités, total en direct | RelativeLayout, LinearLayout, cartes RelativeLayout, commandes LinearLayout |
| 3 | `ProgressActivity` | TP1 | **Suivi de préparation** : barre de progression (Handler), puis bouton « Voir mon ticket » quand la commande est prête | LinearLayout + carte RelativeLayout |
| 4 | `CartActivity` | **TP2** | **Panier** : détail des lignes, total, remarque, mode de paiement, confirmation | LinearLayout avec en-tête RelativeLayout, ligne de total RelativeLayout |
| 5 | `TicketActivity` | **TP2** | **Ticket final** : numéro de commande, client, lignes, total, paiement, bouton « Nouvelle commande » | RelativeLayout avec en-tête et carte LinearLayout |

Ordre d'affichage : `Main` → `Menu` → `Cart` → `Progress` → `Ticket` (puis retour à `Main`).

### Communication par Bundle

```
Main     --(nom, table)------------------------------------------------------> Menu
Menu     --(nom, table, quantités[3])----------------------------------------> Cart
Cart     --(nom, table, quantités[3], total, paiement, note, numéro, temps)--> Progress
Progress --(le même Bundle, transmis tel quel)-------------------------------> Ticket
Ticket   --(nouvelle commande : retour à Main, pile d'activités vidée)
```

Chaque activité **reçoit** le Bundle avec `getIntent().getBundleExtra(...)` et **envoie** un Bundle avec `putExtra` puis `startActivity`. Le Bundle est vérifié (`!= null`) à la réception.

Prix : Espresso 2,50, Cappuccino 3,50, Latte 4,00. Durée de préparation : 5 secondes par boisson (maximum 30 s pour la démo).

---

## 3. Classes utilitaires

### `Keys.java`

```java
public final class Keys {
    private Keys() {}
    public static final String EXTRA_BUNDLE = "data_bundle";
    public static final String NOM      = "nom";
    public static final String TABLE    = "table";
    public static final String QTES     = "qtes";       // int[] : une quantité par boisson
    public static final String TOTAL    = "total";
    public static final String PAIEMENT = "paiement";
    public static final String NOTE     = "note";
    public static final String NUMERO   = "numero";
    public static final String TEMPS    = "temps";      // durée de préparation en secondes
}
```

### `Carte.java` (les boissons et les calculs, partagés par plusieurs écrans)

```java
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
```

---

## 4. Écran 1 : `MainActivity` (identification)

`res/layout/activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Bandeau : RelativeLayout -->
    <RelativeLayout
        android:id="@+id/banner"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_alignParentTop="true"
        android:background="#795548">

        <TextView
            android:id="@+id/tvLogo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerHorizontal="true"
            android:layout_marginTop="32dp"
            android:text="☕"
            android:textSize="48sp" />

        <TextView
            android:id="@+id/tvAppName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_below="@id/tvLogo"
            android:layout_centerHorizontal="true"
            android:text="@string/app_name"
            android:textColor="#FFFFFF"
            android:textSize="28sp"
            android:textStyle="bold" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_below="@id/tvAppName"
            android:layout_centerHorizontal="true"
            android:text="@string/app_subtitle"
            android:textColor="#EFEBE9"
            android:textSize="14sp" />
    </RelativeLayout>

    <!-- Formulaire : LinearLayout vertical -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/banner"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/login_title"
            android:textSize="20sp"
            android:textStyle="bold" />

        <EditText
            android:id="@+id/editNom"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:hint="@string/hint_nom"
            android:inputType="textPersonName" />

        <EditText
            android:id="@+id/editTable"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/hint_table"
            android:inputType="number" />

        <Button
            android:id="@+id/btnCommencer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:backgroundTint="#795548"
            android:text="@string/btn_commencer"
            android:textColor="#FFFFFF" />
    </LinearLayout>
</RelativeLayout>
```

`MainActivity.java`

```java
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
```

---

## 5. Écran 2 : `MenuActivity` (choix des boissons)

`res/layout/activity_menu.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- En-tête : RelativeLayout -->
    <RelativeLayout
        android:id="@+id/header"
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:layout_alignParentTop="true"
        android:background="#795548"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvTitreMenu"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/title_menu"
            android:textColor="#FFFFFF"
            android:textSize="22sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvGreeting"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_below="@id/tvTitreMenu"
            android:textColor="#EFEBE9"
            android:textSize="13sp" />
    </RelativeLayout>

    <!-- Pied de page : total et bouton panier -->
    <RelativeLayout
        android:id="@+id/footer"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:background="#EFEBE9"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvTotalMenu"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentStart="true"
            android:layout_centerVertical="true"
            android:textSize="18sp"
            android:textStyle="bold" />

        <Button
            android:id="@+id/btnPanier"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentEnd="true"
            android:backgroundTint="#795548"
            android:text="@string/btn_panier"
            android:textColor="#FFFFFF" />
    </RelativeLayout>

    <!-- Liste des boissons : LinearLayout contenant des cartes RelativeLayout -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_above="@id/footer"
        android:layout_below="@id/header"
        android:orientation="vertical"
        android:padding="12dp">

        <!-- ===== Carte Espresso ===== -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:background="#EFEBE9"
            android:padding="12dp">

            <TextView
                android:id="@+id/iconEspresso"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentStart="true"
                android:layout_centerVertical="true"
                android:text="☕"
                android:textSize="32sp" />

            <LinearLayout
                android:id="@+id/ctrlEspresso"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <Button
                    android:id="@+id/btnMoinsEspresso"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:minWidth="0dp"
                    android:minHeight="0dp"
                    android:padding="0dp"
                    android:text="@string/btn_minus" />

                <TextView
                    android:id="@+id/tvQteEspresso"
                    android:layout_width="28dp"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:text="0"
                    android:textSize="18sp"
                    android:textStyle="bold" />

                <Button
                    android:id="@+id/btnPlusEspresso"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:minWidth="0dp"
                    android:minHeight="0dp"
                    android:padding="0dp"
                    android:text="@string/btn_plus" />
            </LinearLayout>

            <TextView
                android:id="@+id/nomEspresso"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="12dp"
                android:layout_toStartOf="@id/ctrlEspresso"
                android:layout_toEndOf="@id/iconEspresso"
                android:text="@string/drink_espresso"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/descEspresso"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_below="@id/nomEspresso"
                android:layout_marginStart="12dp"
                android:layout_toStartOf="@id/ctrlEspresso"
                android:layout_toEndOf="@id/iconEspresso"
                android:text="@string/desc_espresso"
                android:textSize="12sp" />

            <TextView
                android:id="@+id/prixEspresso"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_below="@id/descEspresso"
                android:layout_marginStart="12dp"
                android:layout_toEndOf="@id/iconEspresso"
                android:textColor="#795548"
                android:textStyle="bold" />
        </RelativeLayout>

        <!-- ===== Carte Cappuccino ===== -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:background="#EFEBE9"
            android:padding="12dp">

            <TextView
                android:id="@+id/iconCappuccino"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentStart="true"
                android:layout_centerVertical="true"
                android:text="☕"
                android:textSize="32sp" />

            <LinearLayout
                android:id="@+id/ctrlCappuccino"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <Button
                    android:id="@+id/btnMoinsCappuccino"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:minWidth="0dp"
                    android:minHeight="0dp"
                    android:padding="0dp"
                    android:text="@string/btn_minus" />

                <TextView
                    android:id="@+id/tvQteCappuccino"
                    android:layout_width="28dp"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:text="0"
                    android:textSize="18sp"
                    android:textStyle="bold" />

                <Button
                    android:id="@+id/btnPlusCappuccino"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:minWidth="0dp"
                    android:minHeight="0dp"
                    android:padding="0dp"
                    android:text="@string/btn_plus" />
            </LinearLayout>

            <TextView
                android:id="@+id/nomCappuccino"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="12dp"
                android:layout_toStartOf="@id/ctrlCappuccino"
                android:layout_toEndOf="@id/iconCappuccino"
                android:text="@string/drink_cappuccino"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/descCappuccino"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_below="@id/nomCappuccino"
                android:layout_marginStart="12dp"
                android:layout_toStartOf="@id/ctrlCappuccino"
                android:layout_toEndOf="@id/iconCappuccino"
                android:text="@string/desc_cappuccino"
                android:textSize="12sp" />

            <TextView
                android:id="@+id/prixCappuccino"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_below="@id/descCappuccino"
                android:layout_marginStart="12dp"
                android:layout_toEndOf="@id/iconCappuccino"
                android:textColor="#795548"
                android:textStyle="bold" />
        </RelativeLayout>

        <!-- ===== Carte Latte ===== -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:background="#EFEBE9"
            android:padding="12dp">

            <TextView
                android:id="@+id/iconLatte"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentStart="true"
                android:layout_centerVertical="true"
                android:text="☕"
                android:textSize="32sp" />

            <LinearLayout
                android:id="@+id/ctrlLatte"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <Button
                    android:id="@+id/btnMoinsLatte"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:minWidth="0dp"
                    android:minHeight="0dp"
                    android:padding="0dp"
                    android:text="@string/btn_minus" />

                <TextView
                    android:id="@+id/tvQteLatte"
                    android:layout_width="28dp"
                    android:layout_height="wrap_content"
                    android:gravity="center"
                    android:text="0"
                    android:textSize="18sp"
                    android:textStyle="bold" />

                <Button
                    android:id="@+id/btnPlusLatte"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:minWidth="0dp"
                    android:minHeight="0dp"
                    android:padding="0dp"
                    android:text="@string/btn_plus" />
            </LinearLayout>

            <TextView
                android:id="@+id/nomLatte"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="12dp"
                android:layout_toStartOf="@id/ctrlLatte"
                android:layout_toEndOf="@id/iconLatte"
                android:text="@string/drink_latte"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/descLatte"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_below="@id/nomLatte"
                android:layout_marginStart="12dp"
                android:layout_toStartOf="@id/ctrlLatte"
                android:layout_toEndOf="@id/iconLatte"
                android:text="@string/desc_latte"
                android:textSize="12sp" />

            <TextView
                android:id="@+id/prixLatte"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_below="@id/descLatte"
                android:layout_marginStart="12dp"
                android:layout_toEndOf="@id/iconLatte"
                android:textColor="#795548"
                android:textStyle="bold" />
        </RelativeLayout>
    </LinearLayout>
</RelativeLayout>
```

`MenuActivity.java`

```java
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
```

---

## 6. Écran 3 : `CartActivity` (panier, nouvelle activité TP2)

`res/layout/activity_cart.xml` (LinearLayout racine, en-tête et ligne de total en RelativeLayout)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- En-tête : RelativeLayout -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="64dp"
        android:background="#795548"
        android:padding="8dp">

        <Button
            android:id="@+id/btnRetour"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentStart="true"
            android:layout_centerVertical="true"
            android:text="@string/btn_retour" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:text="@string/title_cart"
            android:textColor="#FFFFFF"
            android:textSize="20sp" />
    </RelativeLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/cart_items_label"
                android:textSize="16sp"
                android:textStyle="bold" />

            <!-- Lignes du panier -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:background="#EFEBE9"
                android:orientation="vertical"
                android:padding="16dp">

                <TextView
                    android:id="@+id/tvLignes"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:lineSpacingExtra="4dp"
                    android:textSize="16sp" />
            </LinearLayout>

            <!-- Ligne de total : RelativeLayout dans le LinearLayout -->
            <RelativeLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="12dp">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_alignParentStart="true"
                    android:text="@string/cart_total_label"
                    android:textSize="18sp"
                    android:textStyle="bold" />

                <TextView
                    android:id="@+id/tvTotal"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_alignParentEnd="true"
                    android:textColor="#795548"
                    android:textSize="18sp"
                    android:textStyle="bold" />
            </RelativeLayout>

            <EditText
                android:id="@+id/editNote"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:hint="@string/hint_note"
                android:inputType="textCapSentences" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="@string/pay_label"
                android:textSize="16sp"
                android:textStyle="bold" />

            <RadioGroup
                android:id="@+id/radioPaiement"
                android:layout_width="match_parent"
                android:layout_height="wrap_content">

                <RadioButton
                    android:id="@+id/radioCash"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:checked="true"
                    android:text="@string/pay_cash" />

                <RadioButton
                    android:id="@+id/radioCarte"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="@string/pay_card" />
            </RadioGroup>

            <Button
                android:id="@+id/btnConfirmer"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:backgroundTint="#795548"
                android:text="@string/btn_confirmer"
                android:textColor="#FFFFFF" />
        </LinearLayout>
    </ScrollView>
</LinearLayout>
```

`CartActivity.java`

```java
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
```

---

## 7. Écran 4 : `ProgressActivity` (suivi de la préparation)

`res/layout/activity_progress.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <!-- Carte de commande : RelativeLayout dans le LinearLayout -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#EFEBE9"
        android:padding="16dp">

        <TextView
            android:id="@+id/iconCafe"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentEnd="true"
            android:layout_centerVertical="true"
            android:text="☕"
            android:textSize="36sp" />

        <TextView
            android:id="@+id/tvTitre"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentStart="true"
            android:textSize="20sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvInfo"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentStart="true"
            android:layout_below="@id/tvTitre"
            android:textSize="14sp" />
    </RelativeLayout>

    <TextView
        android:id="@+id/tvEtat"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="@string/state_preparing"
        android:textSize="16sp" />

    <ProgressBar
        android:id="@+id/progressBar"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:max="100"
        android:progress="0" />

    <TextView
        android:id="@+id/tvProgress"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="0 %" />

    <!-- Invisible tant que la commande n'est pas prête -->
    <Button
        android:id="@+id/btnTicket"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:backgroundTint="#795548"
        android:text="@string/btn_ticket"
        android:textColor="#FFFFFF"
        android:visibility="gone" />
</LinearLayout>
```

`ProgressActivity.java`

```java
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
```

---

## 8. Écran 5 : `TicketActivity` (ticket final, nouvelle activité TP2)

`res/layout/activity_ticket.xml` (RelativeLayout racine, en-tête et carte en LinearLayout)

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- En-tête : LinearLayout horizontal -->
    <LinearLayout
        android:id="@+id/header"
        android:layout_width="match_parent"
        android:layout_height="72dp"
        android:layout_alignParentTop="true"
        android:background="#795548"
        android:gravity="center_vertical"
        android:orientation="horizontal"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="✔"
            android:textColor="#FFFFFF"
            android:textSize="28sp" />

        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_weight="1"
            android:text="@string/title_ticket"
            android:textColor="#FFFFFF"
            android:textSize="20sp" />
    </LinearLayout>

    <!-- Ticket : LinearLayout vertical -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/header"
        android:layout_margin="16dp"
        android:background="#EFEBE9"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvNumero"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="20sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvClient"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="12dp"
            android:layout_marginBottom="12dp"
            android:background="#BCAAA4" />

        <TextView
            android:id="@+id/tvLignes"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:lineSpacingExtra="4dp"
            android:textSize="16sp" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:layout_marginTop="12dp"
            android:layout_marginBottom="12dp"
            android:background="#BCAAA4" />

        <TextView
            android:id="@+id/tvTotal"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="#795548"
            android:textSize="18sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvPaiement"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp" />

        <TextView
            android:id="@+id/tvNote"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp" />

        <TextView
            android:id="@+id/tvMessage"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:textStyle="italic" />
    </LinearLayout>

    <Button
        android:id="@+id/btnNouvelle"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_margin="16dp"
        android:backgroundTint="#795548"
        android:text="@string/btn_nouvelle"
        android:textColor="#FFFFFF" />
</RelativeLayout>
```

`TicketActivity.java`

```java
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
```

---

## 9. `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:theme="@style/Theme.AppCompat.Light.DarkActionBar">

        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".MenuActivity"     android:exported="false" />
        <activity android:name=".CartActivity"     android:exported="false" />
        <activity android:name=".ProgressActivity" android:exported="false" />
        <activity android:name=".TicketActivity"   android:exported="false" />
    </application>
</manifest>
```

Utilise le thème de ton projet (celui généré par Android Studio) à la place de `Theme.AppCompat.Light.DarkActionBar` si tu en as déjà un.

---

## 10. Défi 2 : version multilingue (`strings.xml`)

`res/values/strings.xml` (français, langue par défaut)

```xml
<resources>
    <string name="app_name">CoffeeTime</string>
    <string name="app_subtitle">Commandez depuis votre table</string>
    <string name="login_title">Identifiez-vous</string>
    <string name="hint_nom">Votre nom</string>
    <string name="hint_table">Numéro de table</string>
    <string name="btn_commencer">Commencer</string>
    <string name="err_champs_vides">Veuillez remplir tous les champs</string>

    <string name="title_menu">Notre menu</string>
    <string name="menu_greeting">Bonjour %1$s, table %2$d</string>
    <string name="drink_espresso">Espresso</string>
    <string name="drink_cappuccino">Cappuccino</string>
    <string name="drink_latte">Latte</string>
    <string name="desc_espresso">Café court et intense</string>
    <string name="desc_cappuccino">Espresso, lait chaud et mousse</string>
    <string name="desc_latte">Café doux avec beaucoup de lait</string>
    <string name="price_fmt">%1$.2f DT</string>
    <string name="total_label">Total : %1$.2f DT</string>
    <string name="btn_panier">Voir mon panier</string>
    <string name="err_panier_vide">Ajoutez au moins une boisson</string>
    <string name="btn_minus" translatable="false">−</string>
    <string name="btn_plus" translatable="false">+</string>

    <string name="btn_retour">Retour</string>
    <string name="title_cart">Mon panier</string>
    <string name="cart_items_label">Vos boissons</string>
    <string name="cart_total_label">Total</string>
    <string name="line_fmt">%1$d × %2$s : %3$.2f DT</string>
    <string name="hint_note">Remarque (ex. : sans sucre)</string>
    <string name="pay_label">Mode de paiement</string>
    <string name="pay_cash">Espèces</string>
    <string name="pay_card">Carte bancaire</string>
    <string name="btn_confirmer">Confirmer la commande</string>

    <string name="order_number">Commande n°%1$d</string>
    <string name="progress_info">Table %1$d, %2$d boisson(s)</string>
    <string name="state_preparing">Votre commande est en préparation…</string>
    <string name="state_ready">Votre commande est prête !</string>
    <string name="btn_ticket">Voir mon ticket</string>

    <string name="title_ticket">Ticket de commande</string>
    <string name="ticket_customer">Client : %1$s (table %2$d)</string>
    <string name="ticket_payment">Paiement : %1$s</string>
    <string name="ticket_note">Remarque : %1$s</string>
    <string name="ticket_message">Nous l\'apportons à la table %1$d.</string>
    <string name="btn_nouvelle">Nouvelle commande</string>
</resources>
```

`res/values-en/strings.xml` (anglais)

```xml
<resources>
    <string name="app_name">CoffeeTime</string>
    <string name="app_subtitle">Order from your table</string>
    <string name="login_title">Sign in</string>
    <string name="hint_nom">Your name</string>
    <string name="hint_table">Table number</string>
    <string name="btn_commencer">Start</string>
    <string name="err_champs_vides">Please fill in all fields</string>

    <string name="title_menu">Our menu</string>
    <string name="menu_greeting">Hello %1$s, table %2$d</string>
    <string name="drink_espresso">Espresso</string>
    <string name="drink_cappuccino">Cappuccino</string>
    <string name="drink_latte">Latte</string>
    <string name="desc_espresso">Short and intense coffee</string>
    <string name="desc_cappuccino">Espresso, hot milk and foam</string>
    <string name="desc_latte">Mild coffee with lots of milk</string>
    <string name="price_fmt">%1$.2f DT</string>
    <string name="total_label">Total: %1$.2f DT</string>
    <string name="btn_panier">View my cart</string>
    <string name="err_panier_vide">Add at least one drink</string>

    <string name="btn_retour">Back</string>
    <string name="title_cart">My cart</string>
    <string name="cart_items_label">Your drinks</string>
    <string name="cart_total_label">Total</string>
    <string name="line_fmt">%1$d × %2$s: %3$.2f DT</string>
    <string name="hint_note">Note (e.g. no sugar)</string>
    <string name="pay_label">Payment method</string>
    <string name="pay_cash">Cash</string>
    <string name="pay_card">Card</string>
    <string name="btn_confirmer">Confirm order</string>

    <string name="order_number">Order #%1$d</string>
    <string name="progress_info">Table %1$d, %2$d drink(s)</string>
    <string name="state_preparing">Your order is being prepared…</string>
    <string name="state_ready">Your order is ready!</string>
    <string name="btn_ticket">View my receipt</string>

    <string name="title_ticket">Order receipt</string>
    <string name="ticket_customer">Customer: %1$s (table %2$d)</string>
    <string name="ticket_payment">Payment: %1$s</string>
    <string name="ticket_note">Note: %1$s</string>
    <string name="ticket_message">We will bring it to table %1$d.</string>
    <string name="btn_nouvelle">New order</string>
</resources>
```

Les layouts utilisent déjà `@string/...`, donc l'application suit la langue du téléphone. Pour tester : Paramètres, Langue, English.

---

## 11. Défi 1 : version à une seule activité

Une seule activité `SingleActivity` change d'écran avec `setContentView()` en réutilisant les 5 layouts XML. Les données sont gardées dans des attributs de la classe, donc plus besoin de Bundle ni d'Intent.

```java
public class SingleActivity extends AppCompatActivity {

    private String nom = "", paiement = "", note = "";
    private int table = 0, numero = 0, duree = 10;
    private int[] qtes = new int[Carte.NB];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        afficherAccueil();
    }

    private void afficherAccueil() {
        setContentView(R.layout.activity_main);
        EditText editNom = findViewById(R.id.editNom);
        EditText editTable = findViewById(R.id.editTable);
        findViewById(R.id.btnCommencer).setOnClickListener(v -> {
            nom = editNom.getText().toString().trim();
            String t = editTable.getText().toString().trim();
            if (nom.isEmpty() || t.isEmpty()) {
                Toast.makeText(this, R.string.err_champs_vides, Toast.LENGTH_SHORT).show();
                return;
            }
            table = Integer.parseInt(t);
            afficherMenu();
        });
    }

    private void afficherMenu() {
        setContentView(R.layout.activity_menu);
        // même logique que MenuActivity ; au clic sur « Voir mon panier » : afficherPanier();
    }

    private void afficherPanier() {
        setContentView(R.layout.activity_cart);
        // même logique que CartActivity ; au clic sur « Confirmer » : afficherProgression();
    }

    private void afficherProgression() {
        setContentView(R.layout.activity_progress);
        // même logique Handler que ProgressActivity ; au clic sur « Voir mon ticket » : afficherTicket();
    }

    private void afficherTicket() {
        setContentView(R.layout.activity_ticket);
        // même logique que TicketActivity ; « Nouvelle commande » : réinitialiser les champs puis afficherAccueil();
    }

    @Override
    public void onBackPressed() {
        afficherAccueil();   // gestion simple du retour
    }
}
```

Inconvénient à mentionner dans ton rapport : on perd la pile d'activités (le bouton Retour natif) et le cycle de vie propre à chaque écran, donc il faut tout gérer soi-même. C'est pourquoi la version multi-activités reste la bonne pratique.

---

## 12. Checklist de rendu

- [ ] 3 activités du TP1 améliorées : `MainActivity`, `MenuActivity`, `ProgressActivity`
- [ ] 2 nouvelles activités : `CartActivity`, `TicketActivity`, avec LinearLayout et RelativeLayout imbriqués
- [ ] Bundle utilisé à l'envoi et à la réception dans chaque transition, avec test `!= null`
- [ ] Toutes les activités déclarées dans le manifeste
- [ ] Défi 1 : version à une seule activité
- [ ] Défi 2 : `strings.xml` en français et en anglais

## 13. Idées pour la suite du trimestre

Menu plus long avec `RecyclerView`, historique des commandes dans SQLite/Room, mémorisation du client avec `SharedPreferences`, notification quand la commande est prête, passage à `ConstraintLayout`.
