package com.example.laboratorio6.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laboratorio6.Adapter.IngresosAdapter;
import com.example.laboratorio6.R;
import com.example.laboratorio6.databinding.FragmentIngresosBinding;
import com.example.laboratorio6.entity.Ingresos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class IngresosFragment extends Fragment {

    private FragmentIngresosBinding binding;
    private IngresosAdapter adapter;
    private List<Ingresos> list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIngresosBinding.inflate(inflater, container, false);

        // Configurar el OnClickListener para el FloatingActionButton
        binding.fabAgregarIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment nuevoIngresoFragment = new NuevoIngresoFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, nuevoIngresoFragment)
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
        loadIngresos();
    }

    private void setupRecyclerView() {
        list = new ArrayList<>();
        adapter = new IngresosAdapter(getContext(), list);
        binding.recyclerViewIngresos.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewIngresos.setAdapter(adapter);
    }

    private void loadIngresos() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String idUsuario = currentUser.getUid();

            db.collection("ingresos")
                    .whereEqualTo("idUsuario", idUsuario)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            list.clear(); // Limpiar la lista antes de cargar nuevos datos
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Ingresos ingreso = document.toObject(Ingresos.class);
                                list.add(ingreso);
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
