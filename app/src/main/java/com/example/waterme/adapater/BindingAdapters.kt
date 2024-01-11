package com.example.waterme.adapater

import android.graphics.Color
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView

class BindingAdapters {
    @BindingAdapter("isActive")
    fun isActive(view: MaterialCardView, data: Boolean?) {

        if(data == true){
            view.setBackgroundColor(Color.parseColor("#FF0000"))
        }
        else{
            view.setBackgroundColor(Color.parseColor("#00FF00"))
        }
    }
}