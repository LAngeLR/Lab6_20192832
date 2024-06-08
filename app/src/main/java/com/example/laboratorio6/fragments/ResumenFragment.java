package com.example.laboratorio6.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.laboratorio6.R;
import com.example.laboratorio6.entity.Ingresos;
import com.example.laboratorio6.entity.Egresos;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ResumenFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private MaterialButton btnPickMonth;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        btnPickMonth = view.findViewById(R.id.btnPickMonth);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        btnPickMonth.setOnClickListener(v -> showMonthPicker());

        loadData();

        return view;
    }

    private void showMonthPicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    // El mes seleccionado es zero-based, así que sumamos 1
                    selectedMonth += 1;

                    // Aquí obtienes la fecha seleccionada en formato "MM-yyyy"
                    String selectedDate = String.format(Locale.getDefault(), "%02d-%04d", selectedMonth, selectedYear);

                    // Ahora, cargas los datos para el mes seleccionado
                    loadDataForMonth(selectedDate);
                },
                year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void loadDataForMonth(String selectedMonth) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String idUsuario = currentUser.getUid();

        db.collection("ingresos")
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("mes", selectedMonth)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Ingresos> ingresosList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ingresos ingreso = document.toObject(Ingresos.class);
                            ingresosList.add(ingreso);
                        }
                        db.collection("egresos")
                                .whereEqualTo("idUsuario", idUsuario)
                                .whereEqualTo("mes", selectedMonth)
                                .get()
                                .addOnCompleteListener(egresosTask -> {
                                    if (egresosTask.isSuccessful()) {
                                        List<Egresos> egresosList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : egresosTask.getResult()) {
                                            Egresos egreso = document.toObject(Egresos.class);
                                            egresosList.add(egreso);
                                        }
                                        updateCharts(ingresosList, egresosList);
                                    } else {
                                        Toast.makeText(getContext(), "Error al cargar egresos", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Error al cargar ingresos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCharts(List<Ingresos> ingresosList, List<Egresos> egresosList) {
        float totalIngresos = 0;
        for (Ingresos ingreso : ingresosList) {
            totalIngresos += ingreso.getMonto();
            float totalEgresos = 0;
            for (Egresos egreso : egresosList) {
                totalEgresos += egreso.getMonto();
            }

            // Gráfico de pastel
            List<PieEntry> pieEntries = new ArrayList<>();
            if (totalIngresos > 0) {
                pieEntries.add(new PieEntry(totalIngresos, "Ingresos"));
                pieEntries.add(new PieEntry(totalEgresos, "Egresos"));
            } else {
                pieEntries.add(new PieEntry(1, "No hay ingresos"));
            }

            PieDataSet pieDataSet = new PieDataSet(pieEntries, "Ingresos vs Egresos");
            pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            PieData pieData = new PieData(pieDataSet);
            pieChart.setData(pieData);
            pieChart.invalidate();

            // Gráfico de barras
            List<BarEntry> barEntries = new ArrayList<>();
            barEntries.add(new BarEntry(0, totalIngresos));
            barEntries.add(new BarEntry(1, totalEgresos));
            barEntries.add(new BarEntry(2, totalIngresos + totalEgresos));

            BarDataSet barDataSet = new BarDataSet(barEntries, "Ingresos y Egresos");
            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            BarData barData = new BarData(barDataSet);

            barChart.setData(barData);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    switch ((int) value) {
                        case 0:
                            return "Ingresos";
                        case 1:
                            return "Egresos";
                        case 2:
                            return "Consolidado";
                        default:
                            return "";
                    }
                }
            });

            barChart.invalidate();
        }
    }
    private void loadData() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String idUsuario = currentUser.getUid();

        db.collection("ingresos")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Ingresos> ingresosList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Ingresos ingreso = document.toObject(Ingresos.class);
                            ingresosList.add(ingreso);
                        }
                        db.collection("egresos")
                                .whereEqualTo("idUsuario", idUsuario)
                                .get()
                                .addOnCompleteListener(egresosTask -> {
                                    if (egresosTask.isSuccessful()) {
                                        List<Egresos> egresosList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : egresosTask.getResult()) {
                                            Egresos egreso = document.toObject(Egresos.class);
                                            egresosList.add(egreso);
                                        }
                                        updateCharts(ingresosList, egresosList);
                                    } else {
                                        Toast.makeText(getContext(), "Error al cargar egresos", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Error al cargar ingresos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


