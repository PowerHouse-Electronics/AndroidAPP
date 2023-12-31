package com.example.powerhouseelectronics;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Profile extends AppCompatActivity implements CustomAlert.OnDialogCloseListener{

    private void navigateToMainActivity() {
        Intent intent = new Intent(Profile.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onDialogClose() {
        navigateToMainActivity();
    }
    Toolbar toolbar;
    Button btnDeleteAcc;

    ImageView Imagen;

    Uri selectedImageUri;

    boolean imageLoaded = false;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences info = getSharedPreferences("Token", MODE_PRIVATE);
        String userName = info.getString("name", "Nombre de usuario");
        TextView userNameTextView = findViewById(R.id.user_name);
        userNameTextView.setText(userName);

        CircleImageView userProfileImageView = findViewById(R.id.user_profile_image);

        String userImageURL = info.getString("image", "");

        Picasso.with(this)
                .load(userImageURL)
                .into(userProfileImageView);

        Button btnRedireccionEdit = findViewById(R.id.btnEditUser);

        btnRedireccionEdit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Profile.this, UserEdit.class);
                startActivity(intent);
            }
        });

        btnDeleteAcc = findViewById(R.id.btnDeleteAcc);
        btnDeleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertConfirm();
            }
        });


        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String image = sharedPreferences.getString("image", "");
        String address = sharedPreferences.getString("address", "");
        String phone = sharedPreferences.getString("phone", "");

        TextView txtName = findViewById(R.id.TxtName);
        TextView txtEmail = findViewById(R.id.txtGmail);
        TextView txtAddress = findViewById(R.id.txtAdress);
        TextView txtPhone = findViewById(R.id.txtPhone);

        txtName.setText(name);
        txtEmail.setText(email);
        txtAddress.setText(address);
        txtPhone.setText(phone);

        CircleImageView imageView = findViewById(R.id.imageView3);
        Picasso.with(this)
                  .load(image)
                  .into(imageView);


        Imagen = (ImageView) findViewById(R.id.imageView3);
    }

    private void UpdateProfileImage(String imagePath) {
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String modifierId = sharedPreferences.getString("id", "");


        OkHttpClient client = new OkHttpClient();

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Log.d("PROFILE_UPDATE", "Error: La imagen no existe en la ruta proporcionada");
            return;
        }

        RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageRequestBody);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("image", imagePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonBody = jsonObject.toString();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(imagePart)
                .addFormDataPart("data", jsonBody)
                .build();

        Request request = new Request.Builder()
                .url("http://173.255.204.68/api/users/image/" + modifierId)
                .put(requestBody)
                .build();

        Log.d("PROFILE_UPDATE", "JSON Request Body: " + jsonBody);
        Log.d("PROFILE_UPDATE", "URL de la solicitud: " + request.url().toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("PROFILE_UPDATE", "Imagen actualizada correctamente");

                } else {
                    Log.d("PROFILE_UPDATE", "Error al actualizar imagen: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("PROFILE_UPDATE", "Error en la solicitud: " + e.getMessage());
            }
        });
    }

    private void AlertConfirm() {
        CustomConfirmAlert.showCustomClearCartDialog(this, new CustomConfirmAlert.OnDialogCloseListener() {
            @Override
            public void onDialogClose() {
                deleteUserAccount();
            }
        });
    }

    private void deleteUserAccount() {
        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String modifierId = sharedPreferences.getString("id", "");

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        String jsonBody = "{\"modifierId\":\"" + modifierId + "\"}";

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url("http://173.255.204.68/api/users/" + modifierId)
                .delete(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        CustomAlert.showCustomSuccessDialog(Profile.this, "¡Usuario ELiminado!", "Cuenta eliminada correctamente", Profile.this);
                    });
                    //Log.d("USER_DELETE", "Usuario eliminado correctamente");
                } else {
                    runOnUiThread(() -> CustomErrorAlert.showCustomErrorDialog(Profile.this, "Error", "Error al eliminar usuario: " + response.code()));
                    //Log.d("USER_DELETE", "Error al eliminar usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("USER_DELETE", "Error en la solicitud: " + e.getMessage());
            }
        });

        logout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences("Token", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");
        String address = sharedPreferences.getString("address", "");

        TextView nameTextView = findViewById(R.id.TxtName);
        TextView emailTextView = findViewById(R.id.txtGmail);
        TextView phoneTextView = findViewById(R.id.txtPhone);
        TextView addressTextView = findViewById(R.id.txtAdress);

        nameTextView.setText(name);
        emailTextView.setText(email);
        phoneTextView.setText(phone);
        addressTextView.setText(address);
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
        SharedPreferences CartItem = getSharedPreferences("CartItems", MODE_PRIVATE);
        SharedPreferences.Editor edit = CartItem.edit();
        edit.remove("CartItemsList");
        editor.apply();
        edit.apply();
    }

        private void logout() {
            removeTokenFromSharedPreferences();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        private void GoProfile (){
            Intent intent = new Intent(Profile.this, Profile.class);
            startActivity(intent);
        }

        private void GoCarrito (){
            Intent intent = new Intent(Profile.this, Carrito.class);
            startActivity(intent);
        }

    public void LoadImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona la aplicación"), 10);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Selecciona la aplicación"), 10);
            } else {
                Toast.makeText(this, "Permiso de lectura del almacenamiento externo denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri path = data.getData();
            Imagen.setImageURI(path);
            if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
                selectedImageUri = data.getData();
                String imagePath = obtenerRutaImg();
                Imagen.setImageURI(selectedImageUri);

                    showConfirmationAlert(imagePath);

            }
        }
    }

    private void showConfirmationAlert(final String imagePath) {
        CustomConfirmAlert.showCustomClearCartDialog(this, new CustomConfirmAlert.OnDialogCloseListener() {
            @Override
            public void onDialogClose() {
                UpdateProfileImage(imagePath);
            }
        });
    }

    private String obtenerRutaImg() {
        if (selectedImageUri == null) {
            return null;
        }
        return getPathFromUri(selectedImageUri);
    }
    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

}