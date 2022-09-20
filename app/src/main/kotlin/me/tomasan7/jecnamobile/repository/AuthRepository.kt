package me.tomasan7.jecnamobile.repository

import me.tomasan7.jecnaapi.web.Auth

interface AuthRepository
{
    fun get(): Auth?
    fun set(auth: Auth)
    fun set(username: String, password: String) = set(Auth(username, password))
    fun clear()
    fun exists(): Boolean
}