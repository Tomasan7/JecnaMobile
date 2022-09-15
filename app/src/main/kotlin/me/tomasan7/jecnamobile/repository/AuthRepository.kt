package me.tomasan7.jecnamobile.repository

import me.tomasan7.jecnaapi.web.Auth

interface AuthRepository
{
    fun getAuth(): Auth?
    fun setAuth(auth: Auth)
    fun setAuth(username: String, password: String) = setAuth(Auth(username, password))
    fun clearAuth()
    fun exists(): Boolean
}