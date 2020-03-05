package com.wwt.core

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.wwt.core.data.DataState
import com.wwt.core.filestore.FireStoreHelperWrapper
import com.wwt.core.network.NetworkInfoProviderWrapper
import com.wwt.core.util.BuildingIdWrapper
import com.wwt.core.util.SharedPreferenceProvider
import com.wwt.core.util.SharedPreferenceProvider.Companion.USER_DATA_EMAIL
import com.wwt.core.util.SharedPreferenceProvider.Companion.USER_DATA_FAVORITES_DENVER
import com.wwt.core.util.SharedPreferenceProvider.Companion.USER_DATA_FAVORITES_HYDERABAD
import com.wwt.core.util.StringProvider

open class FirestoreViewModel(
    private val fireStoreHelper: FireStoreHelperWrapper,
    private val sharedPreference: SharedPreferenceProvider,
    private val stringProvider: StringProvider,
    private val networkInfoProvider: NetworkInfoProviderWrapper,
    private val integerProvider: BuildingIdWrapper
) : BaseViewModel() {

    protected val _favoriteStatus = MutableLiveData<Boolean>()
    val favoriteStatus: LiveData<Boolean>
        get() = _favoriteStatus

    private val _refreshPoiItems = MutableLiveData<Event<Boolean>>()
    val refreshPoiItems: LiveData<Event<Boolean>>
        get() = _refreshPoiItems

    private val _showPoiList = MutableLiveData<Event<MutableList<Any>>>()
    val showPoiList: LiveData<Event<MutableList<Any>>>
        get() = _showPoiList

    private val _showPoiListWithFavorite = MutableLiveData<Event<MutableList<Any>>>()
    val showPoiListWithFavorite: LiveData<Event<MutableList<Any>>>
        get() = _showPoiListWithFavorite

    protected var finalPoiListWithFavorites: MutableList<Any> = ArrayList()

    private var firebaseCallData: LiveData<DataState>? = null
    private var observer: Observer<DataState>? = null

    private fun checkingIfSelectedPoiAddedOrRemovedFromFavorites(updatedFav: String, poi: String) =
        if (updatedFav.contains(poi)) 1 else 0

    private fun showPoiListWithFavorites() {
        _showPoiListWithFavorite.value = Event(finalPoiListWithFavorites)
    }

    private fun getEmail() = sharedPreference.getStringData(USER_DATA_EMAIL)
    private fun getUserId() = sharedPreference.getData(USER_ID)
    private fun getBuildingId() = sharedPreference.getLongData(BUILDING_ID)
    private fun getAnonymousUser() = sharedPreference.getData(ANONYMOUS_USER)
    protected fun isUserLoggedIn() = !getUserId().isNullOrBlank()
    protected fun isAnonymousLoggedIn() = !getAnonymousUser().isNullOrBlank()

    companion object {
        internal const val USER_ID = "USER_ID"
        private const val ANONYMOUS_USER = "anonymous_user"
        private const val BUILDING_ID = "BuildingId"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var favoritePoiListString: StringBuilder = StringBuilder()

    }
}