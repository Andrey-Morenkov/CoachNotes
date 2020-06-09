package ru.hryasch.coachnotes.home.data

import android.view.View
import android.widget.TextView
import com.pawegio.kandroid.visible

class HomeSimpleButton private constructor(config: Builder)
{
    init
    {
        config.view?.setOnClickListener(config.onClickListener)
    }

    companion object Builder
    {
        private var view: View? = null
        private var onClickListener: View.OnClickListener? = null

        init
        {
            reset()
        }

        fun setView(view: View) = apply { this.view = view }
        fun setOnViewClickListener(listener: View.OnClickListener) = apply { this.onClickListener = listener }

        fun build(): HomeSimpleButton
        {
            return HomeSimpleButton(this).also { reset() }
        }

        private fun reset()
        {
            view = null
            onClickListener = null
        }
    }
}

class HomeAsyncLoadingButton private constructor(config: Builder)
{
    private val loadingView: View?
    private val countView: TextView?

    init
    {
        config.view?.setOnClickListener(config.onClickListener)
        loadingView = config.loadingView
        countView = config.countView

        loadingState()
    }

    fun setCount(count: Int?)
    {
        count?.let {
            countView?.text = it.toString()
            showingState()
        } ?: run {
            loadingState()
        }
    }

    private fun loadingState()
    {
        loadingView?.visible = true
        countView?.visible = false
    }

    private fun showingState()
    {
        loadingView?.visible = false
        countView?.visible = true
    }

    companion object Builder
    {
        private var view: View? = null
        private var onClickListener: View.OnClickListener? = null
        private var loadingView: View? = null
        private var countView: TextView? = null

        init
        {
            reset()
        }

        fun setView(view: View) = apply { this.view = view }
        fun setOnViewClickListener(listener: View.OnClickListener) = apply { this.onClickListener = listener }
        fun setLoadingView(view: View) = apply { this.loadingView = view }
        fun setCountView(view: TextView) = apply { this.countView = view }

        fun build(): HomeAsyncLoadingButton
        {
            return HomeAsyncLoadingButton(this).also { reset() }
        }

        private fun reset()
        {
            view = null
            onClickListener = null
            loadingView = null
            countView = null
        }
    }
}
