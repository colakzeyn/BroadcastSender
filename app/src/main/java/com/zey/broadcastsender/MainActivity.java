package com.zey.broadcastsender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    SensorManager sensorManager;
    ListView listview;
    Sensor accelerometer, lightSen;

    CountDownTimer myTimer;
    CountDownTimer myTimer2;
    int saniye;
    int saniyemove=4;

    float lux;
    boolean hareket =false;

    TextView tex;

    private double prevacc=0;
    private int moveCount =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview = findViewById(R.id.listview);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors= sensorManager.getSensorList(Sensor.TYPE_ALL);
        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(),sensors);
        listview.setAdapter(customAdapter);

        accelerometer  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSen = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        tex =findViewById(R.id.textt);

        setTimer(5000);

        sensorManager.registerListener(MainActivity.this ,accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this ,lightSen, SensorManager.SENSOR_DELAY_FASTEST);
        Log.i("bildirim","register oldu");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(MainActivity.this);
    }

    public void setTimer(int time_milis){
        myTimer = new CountDownTimer(time_milis, 1000){
            @Override
            public void onTick(long l) {
                tex.setText("Eğer hareket etmezse uygulama"+l/1000+"saniye sonra broadcast yollayacak");
                saniye=(int)l/1000;
            }

            @Override
            public void onFinish() {
                tex.setText("Durduğuna dair Broadcast mesajı yollandı");
//                System.out.println("5 saniye boyunca sabit durdunuz");///////////
                hareket=false;
                moveCount=0;
                sendBroadcastStop();
            }
        }.start();
    }

    public void setTimer2(int time_milis){      //hareket halinde olmayı tutar
        myTimer2 = new CountDownTimer(time_milis, 1000){
            @Override
            public void onTick(long l) {
//                System.out.println("hareket haline devam ediyorsunuz");
                saniyemove= (int) l/1000;
            }

            @Override
            public void onFinish() {
                if(moveCount>=30 ){
                    // 5 saniye boyunca yeterli hareketliliği gösterdiniz
                    //sendBroadcast _MOVİNG  --> müzik sesi kapansın
                    sendBroadcastMove();
                    hareket=false;
                    moveCount=0;
                }
            }
        }.start();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType()== Sensor.TYPE_LIGHT){
            lux=sensorEvent.values[0];
        }

        if (sensorEvent.sensor.getType()== Sensor.TYPE_ACCELEROMETER){
            float x_acc = sensorEvent.values[0];

            if(Math.abs(x_acc - prevacc)>0.5){
                myTimer.cancel();
                setTimer(5000);
                if (!hareket){
                    setTimer2(5000); ///hareket halinin başlangıcı
                    hareket =true;
                }

                moveCount++;
            }
            prevacc = x_acc;
        }
        if(lux>0.5 && !hareket){
            System.out.println("Şu an hareketsiz ve masada");
         }else if((lux == 0 && !hareket) || (lux == 0 && moveCount < 6)){
            System.out.println("Şu an hareketsiz ve cepte");
        }else if (lux==0 && hareket){
            System.out.println("Şu an hareketli ve cepte");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void sendBroadcastMove (){
        Intent intent= new Intent("com.zey.EXAMPLE_ACTION");
        intent.putExtra("com.zey.EXTRA_TEXT", "kisi hareket halinde");
        sendBroadcast(intent);
    }

    public void sendBroadcastStop (){
        Intent intent= new Intent("com.zey.EXAMPLE_ACTION");
        intent.putExtra("com.zey.EXTRA_TEXT", "kisi duruyor");
        sendBroadcast(intent);
    }

    //sensorleri listelemek için
    class CustomAdapter extends BaseAdapter{
        List<Sensor> sensors;
        Context context;

        public CustomAdapter (Context context, List<Sensor> sensors) {
            this.sensors = sensors;
            this.context = context;
        }

        @Override
        public int getCount() {
            return sensors.size();
        }

        @Override
        public Object getItem(int i) {
            return sensors.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = new TextView(context);
            ((TextView) view ).setText(sensors.get(i).getName());
            return view;
        }
    }



}