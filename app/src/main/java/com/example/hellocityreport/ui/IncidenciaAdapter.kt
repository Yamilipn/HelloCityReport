package com.example.hellocityreport.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hellocityreport.R
import com.example.hellocityreport.model.Incidencia

/** Adapter para mostrar la lista dinámica de incidencias en el RecyclerView. */
class IncidenciaAdapter(
    private val onClick: (Incidencia) -> Unit
) : ListAdapter<Incidencia, IncidenciaAdapter.IncidenciaViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return IncidenciaViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IncidenciaViewHolder(
        itemView: View,
        private val onClick: (Incidencia) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val textTitulo: TextView = itemView.findViewById(R.id.textTitulo)
        private val textUbicacionFecha: TextView = itemView.findViewById(R.id.textUbicacionFecha)
        private val textEstado: TextView = itemView.findViewById(R.id.textEstado)
        private var actual: Incidencia? = null

        init {
            itemView.setOnClickListener { actual?.let(onClick) }
        }

        fun bind(incidencia: Incidencia) {
            actual = incidencia
            textTitulo.text = incidencia.titulo
            textUbicacionFecha.text = "${incidencia.ubicacion} • ${incidencia.fecha}"
            textEstado.text = incidencia.estado
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Incidencia>() {
            override fun areItemsTheSame(oldItem: Incidencia, newItem: Incidencia): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Incidencia, newItem: Incidencia): Boolean =
                oldItem == newItem
        }
    }
}
