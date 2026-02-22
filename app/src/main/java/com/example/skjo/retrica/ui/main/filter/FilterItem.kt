package com.example.skjo.retrica.ui.main.filter

import com.example.skjo.retrica.model.FilterType

data class FilterItem(
    val name: Int,
    val type: FilterType,
    val thumbnail: Int
) {
    companion object {
        fun getFilters(): List<FilterItem> {
            return FilterType.entries.map {
                FilterItem(
                    name = it.title,
                    type = it,
                    thumbnail = it.thumbnail
                )
            }
        }
    }
}
