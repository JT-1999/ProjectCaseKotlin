package com.example.projectcaseskot.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectcaseskot.R
import com.ykbjson.lib.screening.DLNAPlayer
import com.ykbjson.lib.screening.bean.DeviceInfo

/**
 * projectName ProjectCasesKot
 * @author JT
 * @since 2020/9/27 14:46
 * @version 1.0
 * desc $
 **/
class DeviceAdapter(
    val context: Context,
    val DeviceList: List<DeviceInfo>,
    val DlnaPlayer: DLNAPlayer
) :
    RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val DeviceImage: ImageView = view.findViewById(R.id.itemImage)
        val DeviceText: TextView = view.findViewById(R.id.itemText)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.device_item, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val selectDevice = DeviceList[position]
            DlnaPlayer.connect(selectDevice)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mDevice = DeviceList[position]
        holder.DeviceText.text = mDevice.name
        Glide.with(context).load(R.drawable.colorful).into(holder.DeviceImage)
    }

    override fun getItemCount() = DeviceList.size

}
