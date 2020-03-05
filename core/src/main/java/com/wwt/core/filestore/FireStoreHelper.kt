package com.wwt.core.filestore

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.wwt.core.data.DataState
import com.wwt.core.data.FailureResult
import com.wwt.core.data.SuccessResult
import com.wwt.core.data.TeamMemberProfileModelDto
import com.wwt.core.network.NetworkInfoProviderFactory
import com.wwt.core.network.NetworkInfoProviderWrapper
import com.wwt.core.ui.asset.AssetDataSource
import com.wwt.core.util.*
import com.wwt.core.util.SharedPreferenceProvider.Companion.LOCAL_IMAGE_URL
import com.wwt.core.util.SharedPreferenceProvider.Companion.SAVED_TEAM_MEMBERS_INFO
import com.wwt.core.util.SharedPreferenceProvider.Companion.USER_DATA_EMAIL
import com.wwt.core.util.SharedPreferenceProvider.Companion.USER_DATA_FAVORITES_DENVER
import com.wwt.core.util.SharedPreferenceProvider.Companion.USER_DATA_FAVORITES_HYDERABAD
import java.util.*

interface FireStoreHelperWrapper {
    fun createUsersData(
        email: String,
        userName: String,
        preferredName: String,
        userProfileUri: String = ""
    ): LiveData<DataState>

    fun updateUserProfile(
        email: String,
        userName: String,
        preferredName: String?,
        userProfileUri: String = "",
        sendBroadcast: Boolean = false
    ): LiveData<DataState>

    fun updateUserProfile(
        email: String,
        userName: String? = null,
        preferredName: String? = null,
        userProfileUri: String? = null,
        favPOI: String? = null,
        buildingName: String = "",
        offlineSync: Boolean = false
    ): LiveData<DataState>

    fun getTeamInfo(teamMembersProfileCallback: AssetDataSource.LoadTeamMembersProfileCallback)
    fun getFavoriteLocations(buildingName: String): LiveData<DataState>
    fun syncUserProfile()
    fun syncPoiList()
    fun saveProfilePicture(data: Uri, name: String?, callback: Callback)
    fun downloadImageFromFirebase(
        path: String,
        directory: String,
        fileName: String,
        param: GetPoiImageCallback
    )

    fun managePoi(favoritesPoiString: String, favPOI: String): String

    fun downloadProfilePicFromFirebase(
        userId: String,
        url: String,
        callback: ImageDownloadCallback
    )
}

class FireStoreHelper(
    private val fireStoreProvider: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val dateUtil: DateUtil = DateUtil(),
    private val sharedPreferenceProvider: SharedPreferenceProvider = SharedPreferenceFactory.sharedPreferenceProvider,
    private val broadcastProvider: BroadcastProvider = BroadcastProviderFactory.broadcastProvider,
    private val networkInfoProvider: NetworkInfoProviderWrapper = NetworkInfoProviderFactory.networkInfoProvider,
    private val stringProvider: StringProvider = StringProviderFactory.stringProvider,
    private val fileProvider: FileProvider = FileProviderFactory.fileProvider
) : FireStoreHelperWrapper {
    private var bitmap: Bitmap? = null
    fun getCollectionInstance(collectionName: String): CollectionReference {
        if (networkInfoProvider.isConnected()) {
            fireStoreProvider.enableNetwork()
        } else {
            firebaseStorage.maxOperationRetryTimeMillis = 0L
            fireStoreProvider.disableNetwork()
        }
        return fireStoreProvider.collection(collectionName)
    }

    override fun createUsersData(
        email: String,
        userName: String,
        preferredName: String,
        userProfileUri: String
    ): LiveData<DataState> {
        return Transformations.switchMap(
            isEmailExists(EMAIL_PRIMARYKEY_PATH, email.toLowerCase())
        ) { emailExistsResult ->
            val result: MutableLiveData<DataState> = MutableLiveData()
            when (emailExistsResult) {
                is SuccessResult<*> -> {
                    if (emailExistsResult.data is Boolean && !emailExistsResult.data) {
                        val timeStamp =
                            dateUtil.getFormattedTimeStamp(Date(System.currentTimeMillis()))
                        getDocumentReference().apply {
                            this.set(
                                generateUserInfo(
                                    email,
                                    userName,
                                    this.id,
                                    timeStamp,
                                    preferredName,
                                    userProfileUri, "", "", false
                                )
                            )
                            sharedPreferenceProvider.saveData(USER_ID, this.id)
                        }
                    }
                }
                else -> {
                    result.postValue(
                        FailureResult<FirebaseErrorResult>(
                            FirebaseErrorResult.NetworkError
                        )
                    )
                }
            }
            result.postValue(emailExistsResult)
            return@switchMap result
        }
    }

    private fun isEmailExists(path: String, emailId: String): LiveData<DataState> {
        val result: MutableLiveData<DataState> = MutableLiveData()
        getCollectionInstance(COLLECTION_NAME_USERS).whereEqualTo(path, emailId)
            .get()
            .addOnCompleteListener { task ->
                task
                    .addOnSuccessListener { querySnapshot ->
                        saveDocumentId(querySnapshot)
                        result.postValue(SuccessResult(!querySnapshot.isEmpty))
                    }
                    .addOnFailureListener {
                        result.postValue(
                            FailureResult<FirebaseErrorResult>(
                                FirebaseErrorResult.NetworkError
                            )
                        )
                    }
            }
        return result
    }

    private fun isDocumentExists(userId: String): LiveData<DataState> {
        val result: MutableLiveData<DataState> = MutableLiveData()
        if (userId.isEmpty()) {
            result.postValue(
                FailureResult<FirebaseErrorResult>(
                    FirebaseErrorResult.EmptyUserIdError
                )
            )
            return result
        }
        val documentReference =
            getCollectionInstance(COLLECTION_NAME_USERS).document(userId)
        documentReference.get().addOnCompleteListener { task ->
            task
                .addOnSuccessListener { document -> result.postValue(SuccessResult(document)) }
                .addOnFailureListener {
                    result.postValue(
                        FailureResult<FirebaseErrorResult>(
                            FirebaseErrorResult.NetworkError
                        )
                    )
                }
        }
        return result
    }

    override fun updateUserProfile(
        email: String,
        userName: String,
        preferredName: String?,
        userProfileUri: String,
        sendBroadcast: Boolean
    ): LiveData<DataState> {
        val result: MutableLiveData<DataState> = MutableLiveData()
        sharedPreferenceProvider.getData(USER_ID)?.let { userId ->
            return Transformations.switchMap(
                isDocumentExists(userId)
            ) { docRes ->
                when (docRes) {
                    is SuccessResult<*> -> {
                        val docSnapshot = docRes.data as DocumentSnapshot
                        val timeStamp = docSnapshot.get("timeStamp") as String
                        val isTeamMember = getIsTeamMemberValueFromDocument(docSnapshot)
                        val nickName: String = preferredName?.let { it }
                            ?: getPersonalInfoFieldsFromDocument(docSnapshot, "preferred_name")
                        val favoritesHyd = getFavoriteInfoFromDocument(docSnapshot, HYD_BUILDING)
                        val favoritesDenver = getFavoriteInfoFromDocument(docSnapshot, DENVER_BUILDING)
                        val profileUrl: String = userProfileUri.run {
                            if (isNotBlank()) this
                            else getPersonalInfoFieldsFromDocument(docSnapshot, "image_url")
                        }
                        updateUserProfileData(userName, nickName, profileUrl, favoritesHyd, favoritesDenver, sendBroadcast)
                        getCollectionInstance(COLLECTION_NAME_USERS).document(userId)
                            .set(
                                generateUserInfo(
                                    email,
                                    userName,
                                    userId,
                                    timeStamp,
                                    nickName,
                                    profileUrl,
                                    favoritesHyd,
                                    favoritesDenver,
                                    isTeamMember
                                )
                            )
                        result.postValue(SuccessResult(false))
                    }
                    else -> {
                        result.postValue(
                            FailureResult<FirebaseErrorResult>(
                                FirebaseErrorResult.NetworkError
                            )
                        )
                    }
                }
                return@switchMap result
            }
        }
        result.postValue(
            FailureResult<FirebaseErrorResult>(
                FirebaseErrorResult.EmptyUserIdError
            )
        )
        return result
    }

    override fun updateUserProfile(
        email: String,
        userName: String?,
        preferredName: String?,
        userProfileUri: String?,
        favPOI: String?,
        buildingName: String,
        offlineSync: Boolean
    ): LiveData<DataState> {
        val result: MutableLiveData<DataState> = MutableLiveData()
        sharedPreferenceProvider.getData(USER_ID)?.let { userId ->
            return Transformations.switchMap(
                isDocumentExists(userId)
            ) { docRes ->
                when (docRes) {
                    is SuccessResult<*> -> {
                        val docSnapshot = docRes.data as DocumentSnapshot
                        val timeStamp = docSnapshot.get("timeStamp") as String

                        val name: String = userName?.let { it }
                            ?: getPersonalInfoFieldsFromDocument(docSnapshot, "name")

                        val nickName: String = preferredName?.let { it }
                            ?: getPersonalInfoFieldsFromDocument(docSnapshot, "preferred_name")

                        var favoritesHyd = getFavoriteInfoFromDocument(docSnapshot, HYD_BUILDING)

                        var favoritesDen = getFavoriteInfoFromDocument(docSnapshot, DENVER_BUILDING)

                        val isTeamMember = getIsTeamMemberValueFromDocument(docSnapshot)

                        favPOI?.let { poiId ->
                            if (buildingName.equals(HYD_BUILDING, true)) {
                                favoritesHyd = if (!offlineSync) {
                                    managePoi(favoritesHyd, poiId)
                                } else {
                                    poiId
                                }
                            } else {
                                favoritesDen = if (!offlineSync) {
                                    managePoi(favoritesDen, poiId)
                                } else {
                                    poiId
                                }
                            }
                        }

                        val profileUrl: String = userProfileUri?.let { it }
                            ?: getPersonalInfoFieldsFromDocument(docSnapshot, "image_url")

                        updateUserProfileData(name, nickName, profileUrl, favoritesHyd, favoritesDen)
                        getCollectionInstance(COLLECTION_NAME_USERS).document(userId)
                            .set(
                                generateUserInfo(
                                    email,
                                    name,
                                    userId,
                                    timeStamp,
                                    nickName,
                                    profileUrl,
                                    favoritesHyd,
                                    favoritesDen,
                                    isTeamMember
                                )
                            )
                        result.postValue(SuccessResult(false))
                    }
                    else -> {
                        result.postValue(
                            FailureResult<FirebaseErrorResult>(
                                FirebaseErrorResult.NetworkError
                            )
                        )
                    }
                }
                return@switchMap result
            }
        }
        result.postValue(
            FailureResult<FirebaseErrorResult>(
                FirebaseErrorResult.EmptyUserIdError
            )
        )
        return result
    }

    override fun managePoi(favoritesPoiString: String, favPOI: String): String {
        val favoritePoiStringBuilder: StringBuilder = StringBuilder()

        if (favoritesPoiString.isEmpty()) {
            favoritePoiStringBuilder.append(favPOI)
        } else {
            val favPoiList = favoritesPoiString.split(",").toMutableList()

            favPoiList.takeIf { it.isNotEmpty() }?.apply {
                if (contains(favPOI)) {
                    remove(favPOI)
                } else add(favPOI)
            }

            favPoiList.forEach {
                if (favoritePoiStringBuilder.isEmpty()) {
                    favoritePoiStringBuilder.append(it)
                } else {
                    favoritePoiStringBuilder.append(",").append(it)
                }
            }
        }
        return favoritePoiStringBuilder.toString()
    }

    private fun getPersonalInfoFieldsFromDocument(
        docSnapshot: DocumentSnapshot,
        fieldName: String
    ): String {
        (docSnapshot.get(PERSONAL_INFO) as HashMap<*, *>)[fieldName]?.let {
            return it as String
        }
        return ""
    }

    private fun getIsTeamMemberValueFromDocument(docSnapshot: DocumentSnapshot): Boolean {
        return docSnapshot.get(IS_TEAM_MEMBER_KEY)?.let {
            it as Boolean
        } ?: false
    }

    private fun getFavoriteInfoFromDocument(docSnapshot: DocumentSnapshot, fieldName: String): String {
        docSnapshot.get("favorites")?.let {
            (it as HashMap<*, *>)[fieldName.toLowerCase()]?.let { favPoi ->
                return favPoi as String
            }
        }
        return ""
    }

    private fun saveDocumentId(querySnapshot: QuerySnapshot) {
        if (!querySnapshot.isEmpty) {
            sharedPreferenceProvider.saveData(USER_ID, querySnapshot.documents[0].id)
        }
    }

    private fun getDocumentReference(): DocumentReference =
        getCollectionInstance(COLLECTION_NAME_USERS).document()

    private fun generateUserInfo(
        email: String,
        userName: String,
        id: String,
        timeStamp: String,
        preferredName: String,
        userProfileUri: String?,
        favoritesHyd: String,
        favoritesDenver: String,
        teamMemberValue: Boolean
    ): UserInfo {
        val personalInfo = PersonalInfo(
            name = userName,
            email = email,
            email_primarykey = email.toLowerCase(),
            preferred_name = preferredName,
            image_url = userProfileUri
        )
        val deviceInfo = DeviceInfo(
            os_version = Build.VERSION.RELEASE
        )
        val favoritesInfo = FavoritesInfo(
            hyderabad = favoritesHyd,
            denver = favoritesDenver
        )
        return UserInfo(
            user_id = id,
            app_version = BuildConfig.VERSION_NAME,
            device_info = deviceInfo,
            personal_info = personalInfo,
            timeStamp = timeStamp,
            favorites = favoritesInfo,
            is_team_member = teamMemberValue
        )
    }

    override fun syncUserProfile() {
        sharedPreferenceProvider.getData(USERNAME)?.let { savedName ->
            if (savedName.isNotEmpty()) {
                val collectionReference = getCollectionInstance(COLLECTION_NAME_USERS)
                collectionReference.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        for (documentChange in it.documentChanges) {
                            if (documentChange.type == DocumentChange.Type.MODIFIED) {
                                updateUserProfileData(documentChange.document)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUserProfileData(
        userName: String,
        preferredName: String,
        userProfileUrl: String,
        favoritesHyderabad: String = "",
        favoritesDenver: String = "",
        sendBroadcast: Boolean = false
    ) {
        sharedPreferenceProvider.saveData(USERNAME, userName)
        sharedPreferenceProvider.saveData(PREFERRED_NAME, preferredName)
        sharedPreferenceProvider.saveData(IMAGE_URL, userProfileUrl)
        sharedPreferenceProvider.saveData(USER_DATA_FAVORITES_HYDERABAD, favoritesHyderabad)
        sharedPreferenceProvider.saveData(USER_DATA_FAVORITES_DENVER, favoritesDenver)
        if(sendBroadcast) broadcastProvider.updateUserName()
    }

    private fun updateUserProfileData(documentSnapshot: DocumentSnapshot) {
        documentSnapshot.toObject(UserInfo::class.java)?.personal_info?.run {
            email.let { email ->
                if (email.equals(sharedPreferenceProvider.getStringData(USER_DATA_EMAIL), true)) {
                    name?.let { nameValue ->
                        sharedPreferenceProvider.saveData(USERNAME, nameValue)
                        preferred_name?.let {
                            sharedPreferenceProvider.saveData(PREFERRED_NAME, it)
                        }
                        image_url?.let {
                            if (sharedPreferenceProvider.getStringData(IMAGE_URL).isNotEmpty() &&
                                sharedPreferenceProvider.getStringData(IMAGE_URL) != image_url
                            ) {
                                sharedPreferenceProvider.saveStringData(LOCAL_IMAGE_URL, "")
                                sharedPreferenceProvider.saveData(
                                    IMAGE_URL, it
                                )
                            }
                        }
                        broadcastProvider.updateUserName()
                    }
                }
            }
        }
    }

    override fun getTeamInfo(teamMembersProfileCallback: AssetDataSource.LoadTeamMembersProfileCallback) {
        val teamProfileList: MutableList<TeamMemberProfileModelDto> = arrayListOf()
        getCollectionInstance(COLLECTION_NAME_USERS).whereEqualTo(IS_TEAM_MEMBER_KEY, true)
            .get().addOnCompleteListener { task ->
                task.addOnSuccessListener { result ->
                    var count = 0
                    if (!result.isEmpty) {
                        result.documents.map { document ->
                            document.data?.let { docData ->
                                val data = docData[PERSONAL_INFO] as MutableMap<*, *>
                                val photoUrlKey: String = data[IMAGE_URL].toString()
                                count++
                                teamProfileList.add(getTeamMemberInfo(document.id, photoUrlKey, data, 1))
                                if (count == result.size()) {
                                    teamMembersProfileCallback.onProfilesListLoaded(teamProfileList)
                                }
                            }
                                ?: teamMembersProfileCallback.onDataNotAvailable(stringProvider.getString(com.wwt.core.R.string.unknown_error))
                        }
                    } else {
                        val msg = if (networkInfoProvider.isConnected())
                            stringProvider.getString(com.wwt.core.R.string.no_data_available)
                        else
                            stringProvider.getString(com.wwt.core.R.string.network_unavailable)
                        teamMembersProfileCallback.onDataNotAvailable(msg)
                    }
                }.addOnFailureListener {
                    teamMembersProfileCallback.onDataNotAvailable(
                        stringProvider.getStringWithValue(
                            com.wwt.core.R.string.exception_error, it.message!!
                        )
                    )
                }
            }
    }

    override fun saveProfilePicture(data: Uri, name: String?, callback: Callback) {
        firebaseStorage.maxUploadRetryTimeMillis = 300L
        firebaseStorage.reference.child("Profiles/$name").putFile(data)
            .addOnSuccessListener { uploadTaskSnapshot ->
                uploadTaskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback.resultUri(downloadUri)
                }.addOnFailureListener {
                    callback.failure(it)
                }
            }
    }

    private fun getTeamMemberInfo(
        id: String,
        url: String,
        data: MutableMap<*, *>, isTeamMember: Int
    ): TeamMemberProfileModelDto {
        return TeamMemberProfileModelDto(
            id,
            url,
            data[USER_NAME_KEY].toString(),
            data[PREFERRED_NAME_KEY].toString(),
            isTeamMember
        )
    }

    fun syncTeamInfo() {
        val collectionReference = getCollectionInstance(COLLECTION_NAME_USERS)
        collectionReference.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            snapshot?.let {
                for (documentChange in it.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.MODIFIED) {
                        updateTeamInfo(it.documentChanges[0].document)
                    }
                }
            }
        }
    }

    private fun updateTeamInfo(modifiedDocument: QueryDocumentSnapshot) {
        var teamMemberProfileModelDto: TeamMemberProfileModelDto?
        modifiedDocument.data?.let { docData ->
            val isTeamMember = docData[IS_TEAM_MEMBER_KEY]?.let {
                it as Boolean
            } ?: false
            val teamMemberStatus = if (isTeamMember) 1 else 0
            val data = docData[PERSONAL_INFO] as MutableMap<*, *>
            val photoUrlKey: String = data[IMAGE_URL].toString()
            teamMemberProfileModelDto = getTeamMemberInfo(modifiedDocument.id, photoUrlKey, data, teamMemberStatus)
            updateSavedTeamInfoData(teamMemberProfileModelDto!!)
            broadcastProvider.updateTeamInfo()
        }
    }

    private fun updateSavedTeamInfoData(teamMemberProfileModelDto: TeamMemberProfileModelDto) {
        val savedTeamMemberInfo = sharedPreferenceProvider.getStringData(SAVED_TEAM_MEMBERS_INFO)
        var teamMembersList: MutableList<TeamMemberProfileModelDto> = ArrayList()

        if (savedTeamMemberInfo.isNotEmpty()) {
            teamMembersList = getCachedTeamInfo(savedTeamMemberInfo).toMutableList()

            val getMatchingTeamMemberInfo =
                teamMembersList.find { it.documentId == teamMemberProfileModelDto.documentId }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                teamMembersList.removeIf { it.documentId == teamMemberProfileModelDto.documentId }
            }

            if (teamMemberProfileModelDto.isTeamMember == 1) {
                if (sharedPreferenceProvider.getStringData(teamMemberProfileModelDto.documentId).isNotEmpty()) {
                    getMatchingTeamMemberInfo?.let {
                        if (it.documentId == teamMemberProfileModelDto.documentId
                            && it.url != teamMemberProfileModelDto.url) {
                            sharedPreferenceProvider.saveData(
                                teamMemberProfileModelDto.documentId,
                                ""
                            )
                        }
                    }
                }
                teamMembersList.add(teamMemberProfileModelDto)
            }
        } else {
            teamMembersList.add(teamMemberProfileModelDto)
        }
        saveTeamInfoInPreferences(teamMembersList)
    }

    private fun getCachedTeamInfo(savedTeamMemberInfo: String): List<TeamMemberProfileModelDto> =
        Gson().fromJson(
            savedTeamMemberInfo,
            object : TypeToken<List<TeamMemberProfileModelDto>>() {}.type
        )

    private fun saveTeamInfoInPreferences(teamMembersList: MutableList<TeamMemberProfileModelDto>) {
        sharedPreferenceProvider.saveStringData(
            SAVED_TEAM_MEMBERS_INFO,
            Gson().toJson(teamMembersList)
        )
    }

    override fun downloadImageFromFirebase(
        path: String,
        directory: String,
        fileName: String,
        param: GetPoiImageCallback
    ) {
        val fourMegaByte = 4 * 1024 * 1024L
        firebaseStorage.reference.child(path).getBytes(fourMegaByte)
            .addOnSuccessListener { byteArray ->
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
                bitmap?.let {
                    fileProvider.saveImageToInternal(
                        bm = it,
                        directory = directory,
                        fileName = fileName,
                        param = param
                    )
                }
                bitmap?.recycle()
                bitmap = null
            }
            .addOnFailureListener {
                param.failure(
                    stringProvider.getStringWithValue(
                        com.wwt.core.R.string.exception_error,
                        it.message!!
                    )
                )
            }
    }

    override fun downloadProfilePicFromFirebase(
        userId: String,
        url: String,
        callback: ImageDownloadCallback
    ) {
        val fourMegaByte = 4 * 1024 * 1024L
        firebaseStorage.getReferenceFromUrl(url).getBytes(fourMegaByte)
            .addOnSuccessListener { byteArray ->
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                }
                bitmap?.let {
                    saveImageToAppFolder(userId, it)
                    bitmap?.recycle()
                    bitmap = null
                    fileProvider.localImageUri(
                        FileDirectory.PictureDirectory.type,
                        "$userId$JPEG_FORMAT"
                    )
                        ?.let { storageImageUri ->
                            sharedPreferenceProvider.saveData(
                                    userId,
                                storageImageUri.toString()
                            )
                            callback.onImageSaved(userId)
                        } ?: callback.failure(stringProvider.getString(com.wwt.core.R.string.unknown_error))
                }
            }
            .addOnFailureListener {
                callback.failure(
                    stringProvider.getStringWithValue(
                        com.wwt.core.R.string.exception_error,
                        it.message!!
                    )
                )
            }
    }

    private fun saveImageToAppFolder(userId: String, bitmap: Bitmap, quality: Int = 50): String {
        return fileProvider.saveImageToInternal(
            bm = bitmap,
            directory = FileDirectory.PictureDirectory.type,
            fileName = userId.plus(JPEG_FORMAT),
            quality = quality
        )
    }

    override fun syncPoiList() {
        sharedPreferenceProvider.getData(USERNAME)?.let { savedName ->
            if (savedName.isNotEmpty()) {
                val collectionReference = getCollectionInstance(COLLECTION_NAME_USERS)
                collectionReference.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        for (documentChange in it.documentChanges) {
                            if (documentChange.type == DocumentChange.Type.MODIFIED) {
                                documentChange.document.toObject(UserInfo::class.java).run {
                                if(sharedPreferenceProvider.getData(USER_ID) == this.user_id)
                                    updatePoiList(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getFavoriteLocations(buildingName: String): LiveData<DataState> {
        val result: MutableLiveData<DataState> = MutableLiveData()
        sharedPreferenceProvider.getData(USER_ID)?.let { userId ->
            return Transformations.switchMap(
                isDocumentExists(userId)
            ) { docRes ->
                when (docRes) {
                    is SuccessResult<*> -> {
                        val docSnapshot = docRes.data as DocumentSnapshot
                        val favPoi = getFavoriteInfoFromDocument(docSnapshot, buildingName)
                        result.postValue(SuccessResult(favPoi))
                    }
                    else -> {
                        result.postValue(
                            FailureResult<FirebaseErrorResult>(
                                FirebaseErrorResult.NetworkError
                            )
                        )
                    }
                }
                return@switchMap result
            }
        }
        result.postValue(
            FailureResult<FirebaseErrorResult>(
                FirebaseErrorResult.EmptyUserIdError
            )
        )
        return result
    }

    private fun updatePoiList(data: UserInfo) {
        broadcastProvider.updatePoiList(data)
    }

    fun setTestBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    companion object {
        private const val PERSONAL_INFO = "personal_info"
        private const val EMAIL_PRIMARYKEY_PATH = "$PERSONAL_INFO.email_primarykey"
        private const val COLLECTION_NAME_USERS = "DevUsers"//BuildConfig.USERS_DB
        private const val USER_ID = "USER_ID"
        private const val USERNAME = "username"
        private const val PREFERRED_NAME = "preferredName"
        private const val USER_NAME_KEY = "name"
        private const val IMAGE_URL = "image_url"
        private const val HYD_BUILDING = "hyderabad"
        private const val DENVER_BUILDING = "denver"
        private const val PREFERRED_NAME_KEY = "preferred_name"
        private const val IS_TEAM_MEMBER_KEY = "is_team_member"
        private const val JPEG_FORMAT = ".jpg"
    }
}

sealed class FirebaseErrorResult {
    object NetworkError : FirebaseErrorResult()
    object EmptyUserIdError : FirebaseErrorResult()
}

object FireStoreHelperFactory {
    @SuppressLint("StaticFieldLeak")
    lateinit var fireStoreHelper: FireStoreHelperWrapper
}

interface Callback {
    fun resultUri(resultUri: Uri?)
    fun failure(t: Throwable)
}

interface GetPoiImageCallback {
    fun resultUri(uri: String)
    fun failure(message: String)
}