package com.example.skjo.retrica.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skjo.retrica.databinding.ItemFilterBinding
import com.example.skjo.retrica.model.FilterInfo

/**
 * Camera Filter 변경 UI RecyclerView Adapter.
 */
class FilterAdapter(
    private val filters: List<FilterInfo>,
    private val onFilterSelected: (FilterInfo) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filters[position])
    }

    override fun getItemCount() = filters.size

    inner class FilterViewHolder(private val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onFilterSelected(filters[adapterPosition])
                }
            }
        }

        fun bind(filterInfo: FilterInfo) {
            binding.tvFilterName.text = filterInfo.name
            // You might want to use a library like Glide or Coil to load thumbnails efficiently
            binding.ivThumbnail.setImageResource(filterInfo.thumbnail)
        }
    }
}
