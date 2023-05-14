package me.tomasan7.jecnamobile.login

interface CanteenServerPasswordRepository
{
    fun get(): String?
    fun set(password: String)
    fun clear()
    fun exists(): Boolean
}