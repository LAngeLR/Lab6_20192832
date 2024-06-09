package com.example.laboratorio6.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.laboratorio6.databinding.FragmentEditarEgresoBinding;
import com.example.laboratorio6.databinding.FragmentEditarIngresoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditarEgresoFragment extends Fragment {

    private FragmentEditarEgresoBinding binding;
    private FirebaseFirestore db;

    public static EditarEgresoFragment newInstance(String idEgreso, String titulo, double monto, String descripcion, Date fecha) {
        EditarEgresoFragment fragment = new EditarEgresoFragment();
        Bundle args = new Bundle();
        args.putString("idEgreso", idEgreso);
        args.putString("titulo", titulo);
        args.putDouble("monto", monto);
        args.putString("descripcion", descripcion);
        args.putSerializable("fecha", fecha);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditarEgresoBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String idUsuario = currentUser.getUid();

        // Obtener los datos del ingreso de los argumentos del fragmento
        Bundle args = getArguments();
        if (args != null) {
            String titulo = args.getString("titulo");
            double monto = args.getDouble("monto");
            String descripcion = args.getString("descripcion");
            Date fecha = (Date) args.getSerializable("fecha");

            // Configurar los elementos de la interfaz de usuario con los datos del ingreso
            binding.editTextTitulo7.setText(titulo);
            binding.editTextMonto7.setText(String.valueOf(monto));
            binding.editTextDescripcion7.setText(descripcion);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaFormateada = dateFormat.format(fecha);
            binding.editTextFecha7.setText(fechaFormateada);
        }

        binding.buttonEditar7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los nuevos valores de los campos de edición
                double nuevoMonto = Double.parseDouble(binding.editTextMonto7.getText().toString());
                String nuevaDescripcion = binding.editTextDescripcion7.getText().toString();

                // Obtener el título del ingreso de los argumentos del fragmento
                String titulo = getArguments().getString("titulo");

                // Realizar la consulta a Firestore para obtener el documento con el título especificado
                db.collection("egresos")
                        .whereEqualTo("idUsuario", idUsuario)
                        .whereEqualTo("titulo", titulo)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    boolean documentoEncontrado = false;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String idEgreso = document.getId();

                                        // Actualizar los datos del ingreso en Firestore
                                        db.collection("egresos").document(idEgreso)
                                                .update("monto", nuevoMonto, "descripcion", nuevaDescripcion)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // Actualización exitosa
                                                            Toast.makeText(getContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                                                            getParentFragmentManager().popBackStack();
                                                        } else {
                                                            // Error al actualizar
                                                            Toast.makeText(getContext(), "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                        documentoEncontrado = true;
                                        break;
                                    }

                                    if (!documentoEncontrado) {
                                        Toast.makeText(getContext(), "No se encontró el egreso con el título especificado", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Error al buscar el egreso", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
