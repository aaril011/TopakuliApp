package com.project.topakuli.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.button.MaterialButton
import com.project.topakuli.R

object DialogHelper {

    fun showSuccessDialog(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "Selesai",
        onDismiss: (() -> Unit)? = null
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_success_custom, null)
        val builder = AlertDialog.Builder(context).setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<TextView>(R.id.tv_dialog_title).text = title
        dialogView.findViewById<TextView>(R.id.tv_dialog_message).text = message
        val btnAction = dialogView.findViewById<MaterialButton>(R.id.btn_action)
        btnAction.text = buttonText

        btnAction.setOnClickListener {
            dialog.dismiss()
            onDismiss?.invoke()
        }

        dialog.setCancelable(false)
        dialog.show()
    }
}
