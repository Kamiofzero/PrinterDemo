package com.jolimark.printerdemo.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityPrintTextBinding
import com.jolimark.printerdemo.databinding.ItemVpBinding


class PrintTextActivity : PrintBaseActivity<ActivityPrintTextBinding>() {


    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_print -> {

            }
        }
    }

    private lateinit var mAdapter: VpAdapter
    override fun initView() {
        mAdapter = VpAdapter()
        vb.vp.apply {
            adapter = mAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
    }

    override fun initData() {
        mAdapter.setList(mutableListOf(R.mipmap.color, R.mipmap.gray, R.mipmap.night))
    }


    private inner class VpAdapter : RecyclerView.Adapter<VpAdapter.VpHolder>() {

        private var resourceIds = mutableListOf<Int>()

        fun setList(list: List<Int>) {
            resourceIds.clear()
            resourceIds.addAll(list)
            notifyDataSetChanged()
        }

        inner class VpHolder : RecyclerView.ViewHolder {

            var vb: ItemVpBinding

            constructor(view: View, vpBinding: ItemVpBinding) : super(view) {
                vb = vpBinding
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VpHolder {
            var vb = ItemVpBinding.inflate(LayoutInflater.from(context), parent, false)
            return VpHolder(vb.root, vb)
        }

        override fun getItemCount(): Int {
            return resourceIds.size
        }

        override fun onBindViewHolder(holder: VpHolder, position: Int) {
            holder.vb.iv.setImageResource(resourceIds[position])
        }
    }


}