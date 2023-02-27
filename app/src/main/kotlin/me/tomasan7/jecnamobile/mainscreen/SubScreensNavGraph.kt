package me.tomasan7.jecnamobile.mainscreen

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@NavGraph
annotation class SubScreensNavGraph(
    val start: Boolean = false
)