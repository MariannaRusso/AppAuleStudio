package com.example.appaulestudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

public class IscrizioneActivity extends AppCompatActivity {

    final static String URL_INFOGRUPPIFROMCODICE = "http://pmsc9.altervista.org/progetto/info_gruppo_from_codice.php";

    EditText codice_gruppo;
    Button iscriviti;
    String str_codice_gruppo;
   // ArrayAdapter adapter;
    TextView text_dialog, infoCodice;
    TextView output;
    //String[] array_info;
    String nomeProf, cognomeProf, nomeCorso;

    ImageView close_dialog;

    public void initUI() {

        iscriviti = findViewById(R.id.iscriviti);
        codice_gruppo = findViewById(R.id.codice_gruppo);
        //array_info = new String[3];
        nomeProf=""; cognomeProf=""; nomeCorso="";
        output= findViewById(R.id.output);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iscrizione);
        this.initUI();

        iscriviti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                str_codice_gruppo=codice_gruppo.getText().toString().trim();
                //output.setText(str_codice_gruppo);
                new get_info_dialog().execute();
                //output.setText(nomeProf+nomeCorso);

                //output.append(nomeProf+" "+cognomeProf+" "+nomeCorso);
                /*
                *//*text_dialog.setText("Ti stai iscrivendo al gruppo/n"+"Corso: "+nomeCorso+
                        "/nProfessore: "+nomeProf+" "+cognomeProf);*//*
                d.show();*/

            }
        });

    }




    private class get_info_dialog extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            try {
                //definisco le variabili
                String params;
                URL url;
                HttpURLConnection urlConnection; //serve per aprire connessione
                DataOutputStream dos;
                InputStream is;
                BufferedReader reader;
                StringBuilder sb;
                String line;
                String result;
                JSONArray jArrayInfo;


                url = new URL(URL_INFOGRUPPIFROMCODICE); //passo la richiesta post che mi restituisce i corsi dal db
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(3000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setRequestMethod("POST");  //dico che la richiesta è di tipo POST
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);


                //devo impostare i parametri, devo passare la matricola del docente e il codice dell'uni
                //creo una stringa del tipo nome-valore, sono quelli dei parametri del codice post (li passo alla pagina php)
                params = "codice_gruppo=" + URLEncoder.encode(str_codice_gruppo, "UTF-8");


                dos = new DataOutputStream(urlConnection.getOutputStream());
                dos.writeBytes(params);
                dos.flush();
                dos.close();
                urlConnection.connect();
                is = urlConnection.getInputStream();


                reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                sb = new StringBuilder();
                line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                jArrayInfo = new JSONArray(result);  //questa decodifica crea un array di elementi json


                //faccio un ciclo for per tutti gli elementi all'interno dell'array json che sono corsi
                //per ogni corso mi prendo le relative informazioni relative ad esso e mi creo un array di oggetti corso
                //che poi metterò nella listview
                //array_info = new String[jArrayInfo.length()];

                for (int i = 0; i < jArrayInfo.length(); i++) {
                    JSONObject json_data = jArrayInfo.getJSONObject(i);
                   /* array_info[i] = (json_data.getString("nome") +
                            json_data.getString("cognome") +
                            json_data.getString("nome_corso"));*/
                   //array_info[i]=json_data[i];
                    nomeProf=json_data.getString("nome");
                    cognomeProf=json_data.getString("cognome");
                    nomeCorso=json_data.getString("nome_corso");
                }

                //return array_info;
                return null;
            } catch (Exception e) {
                Log.e("log_tag", "Error " + e.toString());
                return null;
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final Dialog d = new Dialog(IscrizioneActivity.this);
            d.setTitle("Conferma iscrizione");

            d.setCancelable(false);

            d.setContentView(R.layout.dialog_conferma_iscrizione);

            d.getWindow().setBackgroundDrawableResource(R.drawable.forma_dialog);


            text_dialog= d.findViewById(R.id.text_dialog);
            close_dialog = d.findViewById(R.id.close_dialog);
            text_dialog.setText("Ti stai iscrivendo al gruppo per il corso di "+nomeCorso+
                    " tenuto dal professor "+nomeProf+" "+cognomeProf);
            d.show();
            close_dialog.setOnClickListener(new ImageView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    d.cancel();
                }
            });

        }
    }


    }


