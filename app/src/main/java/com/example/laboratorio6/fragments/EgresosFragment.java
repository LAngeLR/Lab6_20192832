package com.example.laboratorio6.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.laboratorio6.Adapter.EgresosAdapter;
import com.example.laboratorio6.Adapter.IngresosAdapter;
import com.example.laboratorio6.R;
import com.example.laboratorio6.databinding.FragmentEgresosBinding;
import com.example.laboratorio6.databinding.FragmentIngresosBinding;
import com.example.laboratorio6.entity.Egresos;
import com.example.laboratorio6.entity.Ingresos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EgresosFragment extends Fragment {

    private FragmentEgresosBinding binding;
    private EgresosAdapter adapter;
    private List<Egresos> list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEgresosBinding.inflate(inflater, container, false);

        // Configurar el OnClickListener para el FloatingActionButton
        binding.fabAgregarIngreso2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment nuevoEgresoFragment = new NuevoEgresoFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, nuevoEgresoFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadEgresos();
    }

    private void setupRecyclerView() {
        list = new ArrayList<>();
        adapter = new EgresosAdapter(getContext(), list);
        binding.recyclerViewIngresos2.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewIngresos2.setAdapter(adapter);
    }

    private void loadEgresos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String idUsuario = currentUser.getUid();

            db.collection("egresos")
                    .whereEqualTo("idUsuario", idUsuario)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            list.clear(); // Limpiar la lista antes de cargar nuevos datos
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Egresos egresos  = document.toObject(Egresos.class);
                                list.add(egresos);
                            }
                            adapter.notifyDataSetChanged(); // Notificar al adaptador de los cambios
                        } else {
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