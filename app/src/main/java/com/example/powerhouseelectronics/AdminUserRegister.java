package com.example.powerhouseelectronics;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUserRegister extends AppCompatActivity implements CustomAlert.OnDialogCloseListener{

    private void navigateToMainActivity() {
        Intent intent = new Intent(AdminUserRegister.this, ViewUsers.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onDialogClose() {
        navigateToMainActivity();
    }

    UsersClass user;
    Toolbar toolbar;

    Button btnRegistrar;

    EditText txtEmail, txtPass, txtNombre, txtDireccion, txtNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_admin_user_register);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", "Nombre de usuario");
        String token = sharedPreferences.getString("token", "");

        TextView userNameTextView = findViewById(R.id.user_name);
        userNameTextView.setText(userName);

        CircleImageView userProfileImageView = findViewById(R.id.user_profile_image);

        String userImageURL = sharedPreferences.getString("image", "");

        Picasso.with(this)
                .load(userImageURL)
                .into(userProfileImageView);

        btnRegistrar = (Button) findViewById(R.id.BtnRegistrar);

        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPass = (EditText) findViewById(R.id.txtPass);
        txtNombre = (EditText) findViewById(R.id.txtNombre);
        txtDireccion = (EditText) findViewById(R.id.txtDireccion);
        txtNum = (EditText) findViewById(R.id.txtNum);
       //selecteditem = (RadioButton) findViewById(R.id.roleGroup.getCheckedRadioButtonId());

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersClassAdmin user = obtenerDatosDelFormulario();
                enviarDatosUsuario(user);
            }
        });
    }

    private UsersClassAdmin obtenerDatosDelFormulario() {

        String name = txtNombre.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPass.getText().toString();
        String address = txtDireccion.getText().toString();
        String phone = txtNum.getText().toString();
        return new UsersClassAdmin(name, email, password, address, phone);
    }

    private void enviarDatosUsuario(UsersClassAdmin user) {
        Api Api = ApiUrl.getRetrofitInstance().create(Api.class);
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String token = sharedPreferences.getString("Token", "");

        String logMessage = "API_CALL - Token: " + token +
                ", Name: " + user.getName() +
                ", Email: " + user.getEmail() +
                ", Password: " + user.getPassword() +
                ", Address: " + user.getAddress() +
                ", Phone: " + user.getPhone();

        Log.d("API_CALL", logMessage);

        Call<Void> call = Api.registerUserAdmin(
                "Bearer " + token,
                RequestBody.create(MediaType.parse("text/plain"), user.getName()),
                RequestBody.create(MediaType.parse("text/plain"), user.getEmail()),
                RequestBody.create(MediaType.parse("text/plain"), user.getPassword()),
                RequestBody.create(MediaType.parse("text/plain"), user.getAddress()),
                RequestBody.create(MediaType.parse("text/plain"), user.getPhone())
        );

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        CustomAlert.showCustomSuccessDialog(AdminUserRegister.this, "¡Registro exitoso!", "Usuario registrado correctamente", AdminUserRegister.this);
                    });
                   // Toast.makeText(getApplicationContext(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        String errorResponse = response.errorBody().string();
                        runOnUiThread(() -> CustomErrorAlert.showCustomErrorDialog(AdminUserRegister.this, "Error", "Error en el registro: " + errorResponse));
                       // Toast.makeText(getApplicationContext(), "Error en el registro: " + errorResponse, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMessage = "Error al realizar la llamada: " + t.getMessage();
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("API_CALL_ERROR", errorMessage);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cliente, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            logout();
            return false;
        }else if (item.getItemId() == R.id.profile){
            GoProfile();
            return false;
        } else if (item.getItemId() == R.id.carrito){
            GoCarrito();
            return false;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void removeTokenFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("token");
        editor.apply();
    }

    private void logout() {
        removeTokenFromSharedPreferences();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void GoProfile() {
        Intent intent = new Intent(AdminUserRegister.this, Profile.class);
        startActivity(intent);
    }

    private void GoCarrito (){
        Intent intent = new Intent(AdminUserRegister.this, Carrito.class);
        startActivity(intent);
    }
}