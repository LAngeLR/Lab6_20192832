package com.example.laboratorio6.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.laboratorio6.R;
import com.example.laboratorio6.databinding.FragmentNuevoEgresoBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NuevoEgresoFragment extends Fragment {

    private FragmentNuevoEgresoBinding binding;

    private EditText editTextTitulo;
    private EditText editTextMonto;
    private EditText editTextDescripcion;
    private DatePicker datePicker;
    private Button buttonCrear;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNuevoEgresoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        editTextTitulo = binding.editTextTitulo1;
        editTextMonto = binding.editTextMonto1;
        editTextDescripcion = binding.editTextDescripcion1;
        datePicker = binding.datePicker1;
        buttonCrear = binding.buttonCrear1;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearEgreso();
            }
        });

        return view;
    }

    private void crearEgreso() {
        String titulo = editTextTitulo.getText().toString().trim();
        String montoStr = editTextMonto.getText().toString().trim();
        String descripcion = editTextDescripcion.getText().toString().trim();

        if (titulo.isEmpty() || montoStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto = Double.parseDouble(montoStr);

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        // Ajustar el valor del mes, ya que en el DatePicker es zero-based
        month += 1;

        // Crear un nuevo objeto Calendar y establecer sus valores con los del DatePicker
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        // Obtener la fecha en milisegundos
        long timestamp = calendar.getTimeInMillis();

        // Crear un objeto Date con el timestamp obtenido
        Date date = new Date(timestamp);

        // Convertir la fecha a un formato compatible con Firestore
        Timestamp fechaFirestore = new Timestamp(date);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String idUsuario = currentUser.getUid();

            Map<String, Object> egresoData = new HashMap<>();
            egresoData.put("idUsuario", idUsuario);
            egresoData.put("titulo", titulo);
            egresoData.put("monto", monto);
            egresoData.put("descripcion", descripcion);
            egresoData.put("fecha", fechaFirestore); // Guardar la fecha como un timestamp

            db.collection("egresos")
                    .document()
                    .set(egresoData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Egreso creado exitosamente", Toast.LENGTH_SHORT).show();
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, new EgresosFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Error al crear el egreso", Toast.LENGTH_SHORT).show();
                            // Manejo de errores
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
