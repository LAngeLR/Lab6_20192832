package com.example.laboratorio6.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laboratorio6.R;
import com.example.laboratorio6.entity.Ingresos;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IngresosAdapter extends RecyclerView.Adapter<IngresosAdapter.IngresosViewHolder> {

    Context context;
    List<Ingresos> list;

    public IngresosAdapter(Context context, List<Ingresos> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public IngresosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingresos, parent, false);
        return new IngresosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngresosViewHolder holder, int position) {
        Ingresos currentItem = list.get(position);

        holder.titulo.setText(currentItem.getTitulo());
        holder.monto.setText(String.valueOf(currentItem.getMonto()));
        holder.descripcion.setText(currentItem.getDescripcion());

        // Convertir la fecha a una cadena legible antes de mostrarla
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String fechaFormateada = dateFormat.format(currentItem.getFecha());
        holder.fecha.setText(fechaFormateada);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class IngresosViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, monto, descripcion, fecha;

        public IngresosViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.tituloI);
            monto = itemView.findViewById(R.id.montoI);
            descripcion = itemView.findViewById(R.id.descripcionI);
            fecha = itemView.findViewById(R.id.fechaI);
        }
    }
}
