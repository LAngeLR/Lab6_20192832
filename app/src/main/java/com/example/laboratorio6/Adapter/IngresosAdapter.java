package com.example.laboratorio6.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laboratorio6.MainActivity;
import com.example.laboratorio6.R;
import com.example.laboratorio6.entity.Egresos;
import com.example.laboratorio6.entity.Ingresos;
import com.example.laboratorio6.fragments.CerrarFragment;
import com.example.laboratorio6.fragments.EditarIngresoFragment;
import com.example.laboratorio6.fragments.NuevoIngresoFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IngresosAdapter extends RecyclerView.Adapter<IngresosAdapter.IngresosViewHolder> {

    Context context;
    List<Ingresos> list;

    FirebaseFirestore db;

    public IngresosAdapter(Context context, List<Ingresos> list) {
        this.context = context;
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
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
        holder.btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = holder.getAdapterPosition();

                if (itemPosition != RecyclerView.NO_POSITION) {
                    // Obtener el elemento de la lista en la posición específica
                    Ingresos ingreso = list.get(itemPosition);

                    // Obtener el ID del ingreso
                    String ingresoId = ingreso.getId();

                    // Obtener el contexto del adaptador
                    Context context = holder.itemView.getContext();

                    // Crear una instancia del fragmento EditarIngresoFragment con los datos del ingreso y el ID del documento
                    EditarIngresoFragment editarIngresoFragment = EditarIngresoFragment.newInstance(
                            ingresoId,
                            ingreso.getTitulo(),
                            ingreso.getMonto(),
                            ingreso.getDescripcion(),
                            ingreso.getFecha()
                    );

                    // Obtener el administrador de fragmentos adecuado desde el contexto del adaptador
                    FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();

                    // Iniciar la transacción de fragmentos para reemplazar el fragmento actual con el fragmento EditarIngresoFragment
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, editarIngresoFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        holder.btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = holder.getAdapterPosition();

                if (itemPosition != RecyclerView.NO_POSITION) {
                    // Obtener el elemento de la lista en la posición específica
                    Ingresos ingreso = list.get(itemPosition);

                    // Obtener el título del ingreso
                    String ingresoTitulo = ingreso.getTitulo();

                    // Buscar el documento en Firestore por el título
                    db.collection("ingresos")
                            .whereEqualTo("titulo", ingresoTitulo)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    // Obtener el ID del primer documento que coincide con el título
                                    String ingresoId = task.getResult().getDocuments().get(0).getId();

                                    // Eliminar el documento de Firestore
                                    db.collection("ingresos").document(ingresoId)
                                            .delete()
                                            .addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    // Eliminar el ingreso de la lista local
                                                    list.remove(itemPosition);
                                                    notifyItemRemoved(itemPosition);
                                                    notifyItemRangeChanged(itemPosition, list.size());
                                                    Toast.makeText(context, "Ingreso eliminado correctamente", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, "Error al eliminar el ingreso", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(context, "Ingreso no encontrado", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

    }





    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class IngresosViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, monto, descripcion, fecha;
        Button btnEditar, btnEliminar;

        public IngresosViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.tituloI);
            monto = itemView.findViewById(R.id.montoI);
            descripcion = itemView.findViewById(R.id.descripcionI);
            fecha = itemView.findViewById(R.id.fechaI);
            btnEditar = itemView.findViewById(R.id.btnEditar); // Asignar el botón de editar
            btnEliminar = itemView.findViewById(R.id.btnEliminar); // Asignar el botón de eliminar
        }
    }

}
