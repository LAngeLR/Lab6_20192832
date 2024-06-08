package com.example.laboratorio6.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.laboratorio6.MainActivity;
import com.example.laboratorio6.databinding.FragmentCerrarBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CerrarFragment extends Fragment {

    private FragmentCerrarBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCerrarBinding.inflate(inflater, container, false);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            binding.textViewUserEmail.setText(userEmail);
        }

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirmación de Salida")
                .setMessage("¿Estás seguro de que quieres salir?")
                .setPositiveButton("Aceptar", (dialog, which) -> logout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void logout() {
        firebaseAuth.signOut();
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
