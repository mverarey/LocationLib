package com.depotserver.android.locationlib;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mauricio on 02/01/18.
 */
public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.ViewHolder> {

    private List<Direccion> mDataset;
    private final Direccion.OnItemClickListener listener;

    public UbicacionAdapter(List<Direccion> direcciones, Context context, Direccion.OnItemClickListener onItemClickListener) {
        this.mDataset = direcciones;
        this.listener = onItemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView direccion;
        TextView tipo;
        //ImageView icono;
        ImageButton btnEditar, btnAceptar;

        ViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            direccion = (TextView)itemView.findViewById(R.id.direccion);
            tipo = (TextView)itemView.findViewById(R.id.tipo);
            //icono = (ImageView)itemView.findViewById(R.id.icono);
            btnEditar = (ImageButton) itemView.findViewById(R.id.btnEditar);
            btnAceptar = (ImageButton) itemView.findViewById(R.id.btnSeleccionar);
        }

        public void bind(final Direccion item, final Direccion.OnItemClickListener listener) {
            btnAceptar.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemOkClick(item);
                }
            });

            btnEditar.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemEditClick(item);
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_ubicaciones, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        final Direccion direccionFinal = mDataset.get(i);

        viewHolder.direccion.setText(direccionFinal.formateada);
        viewHolder.tipo.setText(direccionFinal.latitud + " " + direccionFinal.longitud);
        //viewHolder.icono.setImageResource(R.drawable. );

        viewHolder.bind(direccionFinal, listener);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


}