package me.tomasan7.jecnamobile.login

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FileCanteenServerPasswordRepository @Inject constructor(
    @ApplicationContext
    private val appContext: Context
) : CanteenServerPasswordRepository
{
    private val file = appContext.filesDir.resolve(FILE_NAME)

    override fun get() = if (exists()) file.readText() else null

    override fun set(password: String) = file.writeText(password)

    override fun clear()
    {
        file.delete()
    }

    override fun exists() = file.exists()

    companion object
    {
        private const val FILE_NAME = "canteen_server_password"
    }
}