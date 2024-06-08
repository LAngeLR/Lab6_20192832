package com.example.laboratorio6.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResumenFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChartIngresosEgresos;
    private BarChart barChartConsolidado;
    private MaterialButton btnPickMonth;
    private TextView textViewSelectedMonth;


    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private int ingresosColor = ColorTemplate.COLORFUL_COLORS[0]; // Define el color para ingresos
    private int egresosColor = ColorTemplate.COLORFUL_COLORS[1]; // Define el color para egresos

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resumen, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        barChartIngresosEgresos = view.findViewById(R.id.barChartIngresosEgresos);
        barChartConsolidado = view.findViewById(R.id.barChartConsolidado);
        btnPickMonth = view.findViewById(R.id.btnPickMonth);
        textViewSelectedMonth = view.findViewById(R.id.textViewSelectedMonth);


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

        DatePickerDialog.OnDateSetListener dateSetListener = (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            // Crear un nuevo objeto Calendar y establecer sus valores con los del DatePicker
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(Calendar.YEAR, selectedYear);
            selectedCalendar.set(Calendar.MONTH, selectedMonth);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);

            // Obtener la fecha en milisegundos
            long timestamp = selectedCalendar.getTimeInMillis();

            // Crear un objeto Date con el timestamp obtenido
            Date date = new Date(timestamp);

            // Convertir la fecha a un formato compatible con Firestore
            Timestamp fechaFirestore = new Timestamp(date);

            // Obtener el número del mes en formato de 1 a 12
            int mesNum = selectedCalendar.get(Calendar.MONTH) + 1;
            String selectedDate;
            if (mesNum < 10) {
                selectedDate = String.valueOf(mesNum);
            } else {
                selectedDate = String.format(Locale.getDefault(), "%d", mesNum);
            }
            loadDataForMonth(selectedDate);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                dateSetListener,
                year, month, calendar.get(Calendar.DAY_OF_MONTH));

        // Inicializar el DatePicker con la fecha actual y sin listener para ocultar el día
        datePickerDialog.getDatePicker().init(year, month, calendar.get(Calendar.DAY_OF_MONTH), null);

        datePickerDialog.show();
    }





    private void loadDataForMonth(String selectedDate) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String idUsuario = currentUser.getUid();

        db.collection("ingresos")
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("mes", selectedDate)
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
                                .whereEqualTo("mes", selectedDate)
                                .get()
                                .addOnCompleteListener(egresosTask -> {
                                    if (egresosTask.isSuccessful()) {
                                        List<Egresos> egresosList = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : egresosTask.getResult()) {
                                            Egresos egreso = document.toObject(Egresos.class);
                                            egresosList.add(egreso);
                                        }
                                        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
                                        String nombreMes = meses[Integer.parseInt(selectedDate) - 1];
                                        textViewSelectedMonth.setText(nombreMes);
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
        float totalEgresos = 0;

        // Calcular el total de ingresos
        for (Ingresos ingreso : ingresosList) {
            totalIngresos += ingreso.getMonto();
        }

        // Calcular el total de egresos
        for (Egresos egreso : egresosList) {
            totalEgresos += egreso.getMonto();
        }

        // Gráfico de pastel
        List<PieEntry> pieEntries = new ArrayList<>();
        if (totalIngresos == 0 && totalEgresos == 0) {

            pieEntries.add(new PieEntry(1, "Vacio"));
        } else {
            // Mostrar ingresos y egresos por separado si hay datos disponibles
            pieEntries.add(new PieEntry(totalIngresos, "Ingresos"));
            pieEntries.add(new PieEntry(totalEgresos, "Egresos"));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, " ");
        int[] colors;
        if (totalIngresos == 0 && totalEgresos == 0) {
            colors = new int[]{ColorTemplate.COLORFUL_COLORS[0]};
        } else {
            colors = new int[]{ingresosColor, egresosColor};
        }
        pieDataSet.setColors(colors);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        // Gráfico de barras de Ingresos y Egresos
        List<BarEntry> barEntriesIngresosEgresos = new ArrayList<>();
        barEntriesIngresosEgresos.add(new BarEntry(0, totalIngresos));
        barEntriesIngresosEgresos.add(new BarEntry(1, totalEgresos));

        BarDataSet barDataSetIngresosEgresos = new BarDataSet(barEntriesIngresosEgresos, "Ingresos y Egresos");
        barDataSetIngresosEgresos.setColors(new int[]{ingresosColor, egresosColor});

        BarData barDataIngresosEgresos = new BarData(barDataSetIngresosEgresos);
        barDataIngresosEgresos.setBarWidth(0.6f);

        barChartIngresosEgresos.setData(barDataIngresosEgresos);
        barChartIngresosEgresos.setFitBars(true); // make the x-axis fit exactly all bars

        XAxis xAxisIngresosEgresos = barChartIngresosEgresos.getXAxis();
        xAxisIngresosEgresos.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                switch ((int) value) {
                    case 0:
                        return "";
                    case 1:
                        return "";
                    default:
                        return "";
                }
            }
        });

        barChartIngresosEgresos.invalidate();

        // Gráfico de barras Consolidado
        List<BarEntry> barEntriesConsolidado = new ArrayList<>();
        barEntriesConsolidado.add(new BarEntry(0, totalIngresos + totalEgresos));

        BarDataSet barDataSetConsolidado = new BarDataSet(barEntriesConsolidado, "Consolidado");
        barDataSetConsolidado.setColor(ColorTemplate.COLORFUL_COLORS[3]);

        BarData barDataConsolidado = new BarData(barDataSetConsolidado);
        barDataConsolidado.setBarWidth(0.6f);

        barChartConsolidado.setData(barDataConsolidado);
        barChartConsolidado.setFitBars(true); // make the x-axis fit exactly all bars

        XAxis xAxisConsolidado = barChartConsolidado.getXAxis();
        xAxisConsolidado.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return "";
            }
        });

        barChartConsolidado.invalidate();

        // Configurar los ejes Y de los gráficos de barras
        float maxValue = Math.max(totalIngresos, totalEgresos);
        barChartIngresosEgresos.getAxisLeft().setAxisMinimum(0f); // Mínimo
        barChartIngresosEgresos.getAxisLeft().setAxisMaximum(maxValue * 1.2f); // Máximo
        barChartConsolidado.getAxisLeft().setAxisMinimum(0f); // Mínimo
        barChartConsolidado.getAxisLeft().setAxisMaximum((totalIngresos + totalEgresos) * 1.2f); // Máximo

        // Ocultar las etiquetas del eje Y en ambos gráficos de barras
        barChartIngresosEgresos.getAxisLeft().setDrawLabels(false);
        barChartConsolidado.getAxisLeft().setDrawLabels(false);
        barChartIngresosEgresos.getAxisRight().setDrawLabels(false);
        barChartConsolidado.getAxisRight().setDrawLabels(false);
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



