package dev.henkle.context

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import java.lang.ref.WeakReference

/**
* This class serves as a means of retrieving an application [Context] instance without requiring
* the user to explicitly pass an instance to the library. This approach was stolen from Firebase's
* Android SDK, but this version has been translated to Kotlin.
*
* @see <a href="https://github.com/firebase/firebase-android-sdk/blob/30b10ab0448a9729bfcca55480f92a00b3cedcac/firebase-common/src/main/java/com/google/firebase/provider/FirebaseInitProvider.java">https://github.com/firebase/firebase-android-sdk/blob/30b10ab0448a9729bfcca55480f92a00b3cedcac/firebase-common/src/main/java/com/google/firebase/provider/FirebaseInitProvider.java</a>
*/
class ContextProvider : ContentProvider() {
    override fun attachInfo(context: Context, info: ProviderInfo) {
        // super.attachInfo calls onCreate. Fail as early as possible.
        checkContentProviderAuthority(info = info)
        super.attachInfo(context, info)
    }

    /**
     * Called before [android.app.Application.onCreate]
     */
    override fun onCreate(): Boolean {
        context?.also {
            contextRef = WeakReference(it)
        } ?: throw IllegalStateException("LibraryContextProvider was unable to retrieve context!")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    companion object {
        private var contextRef: WeakReference<Context>? = null
        val context: Context? get() = contextRef?.get()

        private const val EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY =
            "dev.henkle.context.librarycontextprovider"

        private fun checkContentProviderAuthority(info: ProviderInfo) {
            if (info.authority == EMPTY_APPLICATION_ID_PROVIDER_AUTHORITY) {
                throw IllegalStateException(
                    "Incorrect provider authority in manifest. Most likely due to a missing " +
                            "applicationId variable in application's build.gradle"
                )
            }
        }
    }
}
