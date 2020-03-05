package com.wwt.core

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer

abstract class MVVMActivity<T : ViewDataBinding, V : BaseViewModel> : DeltaConnectActivity() {

    private lateinit var mViewDataBinding: T

    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getViewModel(): V

    abstract fun getBindingVariable(): Int

    fun getViewDataBinding(): T = mViewDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
        observerLoadingIndicator()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        getViewModel().attachView(savedInstanceState == null)
    }

    private fun observerLoadingIndicator() {
        getViewModel().setLoadingIndicatorVisibility.observe(this, Observer {
//            loadingIndicator?.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    private fun performDataBinding() {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        mViewDataBinding.setVariable(getBindingVariable(), getViewModel())
        mViewDataBinding.executePendingBindings()
    }

    fun performDataReBinding() {
        mViewDataBinding.setVariable(getBindingVariable(), getViewModel())
        mViewDataBinding.executePendingBindings()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getViewModel().detachView()
    }
}