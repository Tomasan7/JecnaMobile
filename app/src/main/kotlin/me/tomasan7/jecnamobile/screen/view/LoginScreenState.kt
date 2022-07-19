package me.tomasan7.jecnamobile.screen.view

data class LoginScreenState(val isLoading: Boolean = false,
                            val username: String = "",
                            val password: String = "",
                            val usernameBlankError: Boolean = false,
                            val passwordBlankError: Boolean = false,
                            val rememberAuth: Boolean = false)