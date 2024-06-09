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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laboratorio6.R;
import com.example.laboratorio6.entity.Egresos;
import com.example.laboratorio6.entity.Ingresos;
import com.example.laboratorio6.fragments.EditarEgresoFragment;
import com.example.laboratorio6.fragments.EditarIngresoFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EgresosAdapter extends RecyclerView.Adapter<EgresosAdapter.EgresosViewHolder>{

    Context context;
    List<Egresos> list;

    FirebaseFirestore db;

    public EgresosAdapter(Context context, List<Egresos> list) {
        this.context = context;
        this.list = list;

        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public EgresosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_egresos, parent, false);
        return new EgresosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EgresosAdapter.EgresosViewHolder holder, int position) {
        Egresos currentItem = list.get(position);

        holder.titulo.setText(currentItem.getTitulo());
        holder.monto.setText(String.valueOf(currentItem.getMonto()));
        holder.descripcion.setText(currentItem.getDescripcion());

        // Convertir la fecha a una cadena legible antes de mostrarla
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String fechaFormateada = dateFormat.format(currentItem.getFecha());

        holder.fecha.setText(fechaFormateada);
        holder.btnEditar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = holder.getAdapterPosition();

                if (itemPosition != RecyclerView.NO_POSITION) {
                    // Obtener el elemento de la lista en la posición específica
                    Egresos egresos = list.get(itemPosition);

                    // Obtener el ID del ingreso
                    String egresoId = egresos.getId();

                    // Obtener el contexto del adaptador
                    Context context = holder.itemView.getContext();

                    // Crear una instancia del fragmento EditarIngresoFragment con los datos del ingreso y el ID del documento
                    EditarEgresoFragment editarEgresoFragment = EditarEgresoFragment.newInstance(
                            egresoId,
                            egresos.getTitulo(),
                            egresos.getMonto(),
                            egresos.getDescripcion(),
                            egresos.getFecha()
                    );

                    // Obtener el administrador de fragmentos adecuado desde el contexto del adaptador
                    FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();

                    // Iniciar la transacción de fragmentos para reemplazar el fragmento actual con el fragmento EditarIngresoFragment
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, editarEgresoFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        holder.btnEliminar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = holder.getAdapterPosition();

                if (itemPosition != RecyclerView.NO_POSITION) {
                    // Obtener el elemento de la lista en la posición específica
                    Egresos egresos = list.get(itemPosition);

                    // Obtener el título del ingreso
                    String egresoTitulo = egresos.getTitulo();

                    // Buscar el documento en Firestore por el título
                    db.collection("egresos")
                            .whereEqualTo("titulo", egresoTitulo)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    // Obtener el ID del primer documento que coincide con el título
                                    String egresoId = task.getResult().getDocuments().get(0).getId();

                                    // Eliminar el documento de Firestore
                                    db.collection("egresos").document(egresoId)
                                            .delete()
                                            .addOnCompleteListener(deleteTask -> {
                                                if (deleteTask.isSuccessful()) {
                                                    // Eliminar el ingreso de la lista local
                                                    list.remove(itemPosition);
                                                    notifyItemRemoved(itemPosition);
                                                    notifyItemRangeChanged(itemPosition, list.size());
                                                    Toast.makeText(context, "Egreso eliminado correctamente", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, "Error al eliminar el egreso", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else {
                                    Toast.makeText(context, "Egreso no encontrado", Toast.LENGTH_SHORT).show();
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

    public static class EgresosViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, monto, descripcion, fecha;
        Button btnEditar2, btnEliminar2;

        public EgresosViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.titulo2);
            monto = itemView.findViewById(R.id.monto2);
            descripcion = itemView.findViewById(R.id.descripcion2);
            fecha = itemView.findViewById(R.id.fecha2);
            btnEditar2 = itemView.findViewById(R.id.btnEditar2); // Asignar el botón de editar
            btnEliminar2 = itemView.findViewById(R.id.btnEliminar2); // Asignar el botón de eliminar
        }
    }

}
