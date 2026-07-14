package com.tetsushozawa.paincompass

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView

object SpinnerHelper {
    fun setupSpinner(
        context: Context,
        spinner: Spinner,
        optionsResId: Int,
        onSelected: (String) -> Unit
    ) {
        val options = context.resources.getStringArray(optionsResId)
        val adapter = object : ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item,
            options
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                return super.getView(position, convertView, parent).applySpinnerTextStyle()
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                return super.getDropDownView(position, convertView, parent).applySpinnerTextStyle()
            }

            private fun View.applySpinnerTextStyle(): View {
                setBackgroundColor(Color.WHITE)
                if (this is TextView) {
                    setTextColor(context.getColor(R.color.text_primary))
                    textSize = 16f
                }
                return this
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (spinner.selectedItemPosition != position) {
                    spinner.setSelection(position)
                }
                onSelected(options[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                if (options.isNotEmpty()) {
                    onSelected(options[0])
                }
            }
        }
        if (options.isNotEmpty()) {
            spinner.setSelection(0)
            onSelected(options[0])
        }
    }
}
