package com.example.laboratorio6;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.laboratorio6.databinding.ActivityNavegationBinding;
import com.example.laboratorio6.fragments.CerrarFragment;
import com.example.laboratorio6.fragments.EgresosFragment;
import com.example.laboratorio6.fragments.IngresosFragment;
import com.example.laboratorio6.fragments.ResumenFragment;


public class NavegationActivity extends AppCompatActivity {

    ActivityNavegationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavegationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new ResumenFragment());

        binding.navMenu.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.itemResumen) {
                replaceFragment(new ResumenFragment());
            } else if (item.getItemId() == R.id.itemIngresos) {
                replaceFragment(new IngresosFragment());
            } else if (item.getItemId() == R.id.itemEngresos) {
                replaceFragment(new EgresosFragment());
            } else if (item.getItemId() == R.id.itemCerrar) {
                replaceFragment(new CerrarFragment());
            }

            return true;

        });

    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

}
