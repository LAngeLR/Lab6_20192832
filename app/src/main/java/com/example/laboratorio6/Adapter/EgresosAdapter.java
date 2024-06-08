package com.example.laboratorio6.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laboratorio6.R;
import com.example.laboratorio6.entity.Egresos;
import com.example.laboratorio6.entity.Ingresos;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EgresosAdapter extends RecyclerView.Adapter<EgresosAdapter.EgresosViewHolder>{

    Context context;
    List<Egresos> list;

    public EgresosAdapter(Context context, List<Egresos> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public EgresosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_egresos, parent, false);
        return new EgresosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EgresosViewHolder holder, int position) {
        Egresos currentItem = list.get(position);

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

    public static class EgresosViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, monto, descripcion, fecha;

        public EgresosViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.titulo2);
            monto = itemView.findViewById(R.id.monto2);
            descripcion = itemView.findViewById(R.id.descripcion2);
            fecha = itemView.findViewById(R.id.fecha2);
        }
    }

}
