package com.example.my_roomate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.my_roomate.Interface.JsonServer;
import com.example.my_roomate.Model.User;
import com.example.my_roomate.OpenHelper.SQLiteOpenHelper;
import com.example.my_roomate.Utils.utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    private Button btn_continuar, btn_facebook;
    private EditText input_email, input_password;
    private TextView forget_password, registrarme;
    private String res;
    private List<User> resposeUser;

    SQLiteOpenHelper conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);//Ajustamos para que este sea el tema a cargar
        //Para desaparecer el Toolbar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        resposeUser = new ArrayList(); //Array donde se almacenaran mis usuarios que traiga en la api
        setContentView(R.layout.activity_login);
        getUser();


        //Configurar los parametros para realizar consultas a la bd
        conn = new SQLiteOpenHelper(this,utils.dbName,null,utils.db_version);

        //vinculando recursos del layout con la clase
        btn_continuar = findViewById(R.id.btn_continuar_login);
        btn_facebook = findViewById(R.id.btn_facebook_login);
        input_email = findViewById(R.id.input_email_login);
        input_password = findViewById(R.id.input_password_login);
        forget_password = findViewById(R.id.forget_pass);
        registrarme = findViewById(R.id.txt_registrarse_login);



        btn_continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input_email.getText().toString().isEmpty() || input_password.getText().toString().isEmpty()){
                    Toast.makeText(getBaseContext(), "Datos vacios, porfavor introduce tu email y password", Toast.LENGTH_LONG).show();
                }else{
                    login();
                }
            }
        });

        btn_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Proximamente conexión a facebook", Toast.LENGTH_LONG).show();
            }
        });

        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "recuperar contraseña", Toast.LENGTH_LONG).show();
            }
        });

        registrarme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), registro.class);
                startActivity(intent);
            }
        });

    }
    private void login(){
        /*
        SQLiteDatabase db = conn.getReadableDatabase();
        //Se crear un arreglo asigando los valores del input email y password
        String[] args = new String[]{
                input_email.getText().toString(),
                input_password.getText().toString()};
        //hago una consulta con los datos a la tabla user
        String query = String.format("SELECT id_user FROM %s WHERE %s LIKE ? AND %s LIKE ?",
                utils.table_user,utils.email_user,utils.password_user);
        Cursor c = db.rawQuery(query,args);
        c.moveToFirst();

        if(c.getCount()>0) {

            savePreferene(c.getInt(0));
            c.close();
            //EventActivity();
            succesCheck();
        }
        else
            Toast.makeText(getApplicationContext(),"No se encontraron coincidencias",Toast.LENGTH_SHORT).show();
        */
        for(User user: resposeUser){
            if(input_email.getText().toString().equals(user.getEmail()) && input_password.getText().toString().equals(user.getPassword())){
                savePreferene(Integer.parseInt(user.getId_user()),user.getNames(), user.getLast_name(), user.getAddress(), user.getUbication(), user.getPhone(), user.getBio(), user.getCurp(), user.getPhoto_profile(), user.getInterestings()[0],user.getInterestings()[1],user.getInterestings()[2],user.getInterestings()[3],user.getInterestings()[4],user.getInterestings()[5]);
                succesCheck();
                return;
            }
        }
        Toast.makeText(getApplicationContext(),"No se encontraron coincidencias",Toast.LENGTH_SHORT).show();
    }

    //Intent que me llevara al explore en caso de que mis credenciales sean validadas de forma correcta
    private void succesCheck(){
        Intent intent = new Intent(getBaseContext(), menu_nav.class); // donde estoy , donde quiero ir
        startActivity(intent);
    }

    private void savePreferene(int id, String name, String lastName, String addres, String ubication, String phone, String bio, String curp, String photoProfile, String interesting1, String interesting2, String interesting3, String interesting4, String interesting5, String interesting6){
        SharedPreferences preferences = getSharedPreferences(utils.SHARED_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putInt(utils.shared_id_user,id);
        edit.putString(utils.shared_names,name);
        edit.putString(utils.shared_last_name, lastName);
        edit.putString(utils.shared_address,addres);
        edit.putString(utils.shared_ubication, ubication);
        edit.putString(utils.shared_phone,phone);
        edit.putString(utils.shared_bio,bio);
        edit.putString(utils.shared_curp,curp);
        edit.putString(utils.shared_photo_profile,photoProfile);
        edit.putString(utils.shared_interestings1,interesting1);
        edit.putString(utils.shared_interestings2,interesting2);
        edit.putString(utils.shared_interestings3,interesting3);
        edit.putString(utils.shared_interestings4,interesting4);
        edit.putString(utils.shared_interestings5,interesting5);
        edit.putString(utils.shared_interestings6,interesting6);
        edit.commit();
    }

    private void getUser(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://my-json-server.typicode.com/Megajjks/dbroomate/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //llamando a la interfaz
        JsonServer jsonServer = retrofit.create(JsonServer.class);
        //llamamos al método de la interfaz que vamos a usar
        Call<List<User>> call = jsonServer.getUsers();
        //creando la respuesta (si hubo error o fue exitoso)
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                //si la respuesta llego pero hubo un problema como error de auth
                if(!response.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"codigo: " +response.code(),Toast.LENGTH_SHORT).show();
                    return;
                }

                //sitodo salio bien pues traemos la respuesta
                List<User> listaUser = response.body();
                //acomodamos el contenido que vamos a traer en el response
                for (User user: listaUser ){
                    resposeUser.add(new User(user.getId_user(),user.getNames(),user.getLast_name(),user.getAddress(),user.getUbication(),user.getPhone(),user.getBio(),user.getCurp(),user.getEmail(),user.getPassword(),user.getPhoto_profile(),user.getInterestings()));
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
