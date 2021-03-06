package com.example.appaulestudio;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class AlertReceiver extends BroadcastReceiver {
    public int id=0;
    public Context context;
    SqliteManager database;
    SharedPreferences preferences;
    boolean logged;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        if(intent.getAction().equals("StudyAround")){
            preferences = context.getSharedPreferences("User_Preferences", Context.MODE_PRIVATE);
            logged = preferences.getBoolean("logged", false);
            showNotification(); //se ascolta l'intent con action StudyAround mostra notifica
        }
        else if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){ //se il telefono si riavvia reset l'alarm
            database=new SqliteManager(context);
            LinkedList<AlarmClass> allarmi_attivi=database.getAlarms();

            if(allarmi_attivi==null){
                Toast.makeText(context, "No alarms", Toast.LENGTH_LONG).show();
            }
            else {
                for(AlarmClass ac:allarmi_attivi){
                    Calendar adesso = Calendar.getInstance();
                    Calendar orario_allarme=Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date_allarme = null;
                    try {
                        date_allarme = df.parse(ac.getOrario_alarm());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    orario_allarme.setTime(date_allarme);
                    if(orario_allarme.after(adesso)==true) reset_allarme(ac.getId_prenotazione(),ac.getOrario_alarm());
                    //Toast.makeText(context, strOrario, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    public void showNotification(){
        //accensione schermo
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Build.VERSION.SDK_INT >= 20 ? pm.isInteractive() : pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "myApp:notificationLock");
            wl.acquire(3000);
        }

        //notification channel
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("gcmAlert", "giacoJacky", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
            channel.setVibrationPattern(new long[] { 1000, 1000, 1000, 1000, 1000});
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);
            channel.enableLights(true);
            mNotificationManager.createNotificationChannel(channel);
        }

        //costruzione notifica
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "gcmAlert")
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Attenzione! La tua prenotazione sta per terminare!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Se ti trovi in aula, sei pregato di iniziare a liberare la postazione. Se non ti trovi in aula, affrettati ad entrare!"))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        //intent
        Intent i=null;
        if(logged==true)  i = new Intent(context.getApplicationContext(), PrenotazioniAttiveActivity.class);
        else i = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);

        //trigger notifica
        mNotificationManager.notify(id, builder.build());
        id++;
    }

//reset alarm dopo reboot
    public void reset_allarme(int id_prenotazione, String orario_alarm){
        Calendar cal_allarme = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date_allarme = null;
            try {
                date_allarme = df.parse(orario_alarm);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal_allarme.setTime(date_allarme);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        intent.setAction("StudyAround");
        intent.putExtra("name", "Attenzione! La tua prenotazione sta per terminare!");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id_prenotazione, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal_allarme.getTimeInMillis(), pendingIntent);
    }

}
