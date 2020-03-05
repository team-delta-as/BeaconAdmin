package com.wwt.core.util

import android.content.Context
import androidx.annotation.IntegerRes
import com.wwt.core.R

interface BuildingIdWrapper {
    fun getDenverBuildingId(): Long
    fun getHyderabadBuildingId(): Long
    fun getBuildingName(buildingId: Long): String
}

class BuildingProvider(private val context: Context) : BuildingIdWrapper {
    override fun getDenverBuildingId(): Long = getInteger(R.integer.buildingId_denver).toLong()
    override fun getHyderabadBuildingId(): Long =
        getInteger(R.integer.buildingId_hyderabad).toLong()

    override fun getBuildingName(buildingId: Long): String {
        return if (buildingId == getHyderabadBuildingId())
            context.getString(R.string.deltatech_capella)
        else
            context.getString(R.string.wwt_asynchrony_labs)
    }

    private fun getInteger(@IntegerRes resId: Int): Int = context.resources.getInteger(resId)
}

object IntegerProviderFactory {
    lateinit var buildingIdWrapper: BuildingIdWrapper
}