package com.fakedevelopers.bidderbidder.ui.register

import android.view.View
import com.fakedevelopers.bidderbidder.R

enum class RegistrationProgressState(
    val navigationId: Int?,
    val previousStep: RegistrationProgressState?,
    private val progressStep: Int?
) {
    EMPTY_STATE(null, null, null),
    CANCEL_REGISTRATION(null, null, null),
    ACCEPT_TERMS(R.id.acceptTermsFragment, CANCEL_REGISTRATION, null),
    ACCEPT_TERMS_CONTENTS(R.id.acceptTermsFragmentContents, ACCEPT_TERMS, null),
    INPUT_ID(R.id.userRegistrationIdFragment, ACCEPT_TERMS, 1),
    INPUT_PASSWORD(R.id.userRegistrationPasswordFragment, INPUT_ID, 2),
    PHONE_AUTH_BEFORE_SENDING(R.id.phoneAuthFragment, INPUT_PASSWORD, 3),
    PHONE_AUTH_CHECK_AUTH_CODE(null, INPUT_PASSWORD, 3),
    CONGRATULATIONS(null, null, 3);

    fun getVisibleState(): Int {
        return when (this) {
            ACCEPT_TERMS -> View.GONE
            else -> View.VISIBLE
        }
    }

    fun getProgressPercentage(): Int? {
        progressStep?.let {
            return (it * 100.0 / 3).toInt()
        }
        return null
    }

    fun checkLastStep(): Boolean {
        return this == CONGRATULATIONS
    }

    fun checkCancelStep(): Boolean {
        return this == CANCEL_REGISTRATION
    }
}
