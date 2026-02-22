package com.example.skjo.retrica.ui.main.filter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skjo.retrica.databinding.ItemFilterBinding
import com.example.skjo.retrica.model.FilterType

/**
 * Camera Filter 변경 UI RecyclerView Adapter.
 */
class FilterAdapter(
    private val context: Context,
    private val onItemClicked: (View) -> Unit // 클릭된 아이템의 View를 전달
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val filterItems = FilterItem.getFilters()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val actualPosition = position % filterItems.size
        holder.bind(filterItems[actualPosition])
    }

    override fun getItemCount() = Integer.MAX_VALUE

    fun getFilterItemAt(position: Int): FilterItem {
        val actualPosition = position % filterItems.size
        return filterItems[actualPosition]
    }

    /**
     * 현재 위치에서 가장 가까운 목표 필터의 인덱스를 찾습니다.
     * @param currentPosition 현재 RecyclerView의 포지션
     * @param targetType 중앙에 위치시킬 목표 필터의 타입
     * @return 목표 필터의 가장 가까운 포지션
     */
    fun findClosestPosition(currentPosition: Int, targetType: FilterType): Int {
        val targetIndex = filterItems.indexOfFirst { it.type == targetType }
        if (targetIndex < 0) return currentPosition // 목표 필터를 찾지 못한 경우

        val itemCount = filterItems.size
        val currentActualIndex = currentPosition % itemCount

        // 순방향 거리와 역방향 거리 계산
        val forwardDistance = (targetIndex - currentActualIndex + itemCount) % itemCount
        val backwardDistance = itemCount - forwardDistance

        // 더 가까운 쪽으로 이동할 포지션 반환
        return if (forwardDistance <= backwardDistance) {
            currentPosition + forwardDistance
        } else {
            currentPosition - backwardDistance
        }
    }

    inner class FilterViewHolder(private val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClicked(itemView) // 포지션 대신 View 자체를 전달
                }
            }
        }

        fun bind(filterItem: FilterItem) {
            binding.tvFilterName.text = context.getString(filterItem.name)
            binding.ivThumbnail.setImageResource(filterItem.thumbnail)
        }
    }
}
