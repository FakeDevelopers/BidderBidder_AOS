package com.fakedevelopers.bidderbidder.ui.util

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections

fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}

fun NavController.safeNavigate(@IdRes resId: Int, args: Bundle? = null) {
    currentDestination?.getAction(resId)?.destinationId.run { navigate(resId, args) }
}
