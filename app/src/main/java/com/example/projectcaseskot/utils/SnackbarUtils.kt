package com.example.projectcaseskot.utils

import android.view.View
import android.widget.Toast
import com.example.ProjectCasesKotApplication
import com.example.projectcaseskot.R
import com.google.android.material.snackbar.Snackbar

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/16 19:56
 * @version 1.0
 * desc $
 **/
object SnackbarUtils {
    fun show(view: View, str: String) {
        Snackbar.make(view, str, Snackbar.LENGTH_SHORT)
            .setAction(ProjectCasesKotApplication.context.getString(R.string.i_know)) {
                Toast.makeText(
                    ProjectCasesKotApplication.context, str, Toast.LENGTH_SHORT
                ).show()
            }.show()
    }
}