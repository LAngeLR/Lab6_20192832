package com.example.laboratorio6;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.laboratorio6.MainActivity;
import com.example.laboratorio6.R;
import com.example.laboratorio6.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Agrega un TextWatcher a los EditText de las contraseñas
        binding.etPassword.addTextChangedListener(passwordWatcher);
        binding.etConfirmPassword.addTextChangedListener(passwordWatcher);

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String correo = binding.etEmail.getText().toString().trim();
                String contraseña = binding.etConfirmPassword.getText().toString().trim();


                if (TextUtils.isEmpty(correo) || TextUtils.isEmpty(contraseña)) {
                    Toast.makeText(RegisterActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (!binding.etPassword.getText().toString().equals(binding.etConfirmPassword.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(correo, contraseña)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // Crear los datos del usuario
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("correo", correo);

                                    // Añadir el documento a la colección "usuarios" en Firestore
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection("usuarios")
                                            .document(user.getUid())
                                            .set(userData)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // Documento agregado correctamente a Firestore
                                                        Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente.", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // Error al agregar el documento a Firestore
                                                        Toast.makeText(RegisterActivity.this, "Error al registrar usuario en Firestore.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    // Error al crear el usuario con correo y contraseña
                                    Toast.makeText(RegisterActivity.this, "Error al registrar usuario en Firebase Authentication.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    //Codigo obtenido de chatgpt para verificar que las contraseñas sean iguales
    private final TextWatcher passwordWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (binding.etPassword.getText().toString().equals(binding.etConfirmPassword.getText().toString())) {
                setEditTextBackground(binding.etPassword, R.drawable.edit_text_background);
                setEditTextBackground(binding.etConfirmPassword, R.drawable.edit_text_background);
            } else {
                setEditTextBackground(binding.etPassword, R.drawable.edit_text_background_error);
                setEditTextBackground(binding.etConfirmPassword, R.drawable.edit_text_background_error);
            }
        }
    };

    private void setEditTextBackground(EditText editText, int backgroundDrawable) {
        Drawable background = getResources().getDrawable(backgroundDrawable);
        editText.setBackground(background);
    }

    public void iniciaCuenta(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
