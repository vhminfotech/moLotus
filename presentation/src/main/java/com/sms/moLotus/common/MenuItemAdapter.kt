package com.sms.moLotus.common

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.recyclerview.widget.RecyclerView
import com.sms.moLotus.R
import com.sms.moLotus.common.base.QkAdapter
import com.sms.moLotus.common.base.QkViewHolder
import com.sms.moLotus.common.util.Colors
import com.sms.moLotus.common.util.extensions.resolveThemeColor
import com.sms.moLotus.common.util.extensions.setVisible
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.menu_list_item.*
import kotlinx.android.synthetic.main.menu_list_item.view.*
import javax.inject.Inject

data class MenuItem(val title: String, val actionId: Int)

class MenuItemAdapter @Inject constructor(private val context: Context, private val colors: Colors) : QkAdapter<MenuItem>() {

    val menuItemClicks: Subject<Int> = PublishSubject.create()

    private val disposables = CompositeDisposable()

    var selectedItem: Int? = null
        set(value) {
            val old = data.map { it.actionId }.indexOfFirst { it == field }
            val new = data.map { it.actionId }.indexOfFirst { it == value }

            field = value

            old.let { notifyItemChanged(it) }
            new.let { notifyItemChanged(it) }
        }

    fun setData(@ArrayRes titles: Int, @ArrayRes values: Int = -1) {
        val valueInts = if (values != -1) context.resources.getIntArray(values) else null

        data = context.resources.getStringArray(titles)
                .mapIndexed { index, title -> MenuItem(title, valueInts?.getOrNull(index) ?: index) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.menu_list_item, parent, false)

        val states = arrayOf(
                intArrayOf(android.R.attr.state_activated),
                intArrayOf(-android.R.attr.state_activated))

        val text = parent.context.resolveThemeColor(android.R.attr.textColorTertiary)
        view.check.imageTintList = ColorStateList(states, intArrayOf(colors.theme().theme, text))

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val menuItem = getItem(adapterPosition)
                menuItemClicks.onNext(menuItem.actionId)
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val menuItem = getItem(position)

        holder.title.text = menuItem.title
        holder.check.isActivated = (menuItem.actionId == selectedItem)
        holder.check.setVisible(selectedItem != null)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

}