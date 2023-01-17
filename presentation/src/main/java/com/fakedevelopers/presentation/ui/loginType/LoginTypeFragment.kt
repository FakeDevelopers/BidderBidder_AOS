package com.fakedevelopers.presentation.ui.loginType

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.domain.secret.Constants.Companion.WEB_CLIENT_ID
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentLoginTypeBinding
import com.fakedevelopers.presentation.ui.MainActivity
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.ApiErrorHandler
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginTypeFragment : BaseFragment<FragmentLoginTypeBinding>(
    R.layout.fragment_login_type
) {
    private lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel: LoginTypeViewModel by viewModels()

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val result = it.data?.let { data -> Auth.GoogleSignInApi.getSignInResultFromIntent(data) }
            if (result != null && result.isSuccess) {
                result.signInAccount?.idToken?.let { idToken ->
                    viewModel.loginWithGoogle(idToken)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textviewLogintypeWelcometext.text =
            SpannableStringBuilder(binding.textviewLogintypeWelcometext.text).apply {
                setSpan(
                    ForegroundColorSpan(requireContext().getColor(R.color.bidderbidder_primary)),
                    0,
                    4,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        binding.textViewLogintypeRegistration.text =
            SpannableStringBuilder(binding.textViewLogintypeRegistration.text).apply {
                setSpan(
                    ForegroundColorSpan(requireContext().getColor(R.color.bidderbidder_primary)),
                    0,
                    binding.textViewLogintypeRegistration.text.indexOf('?') + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        initListener()
        initCollector()
    }

    private fun initListener() {
        binding.buttonLogintypeGoogle.layoutLoginType.setOnClickListener {
            googleLogin()
        }
        binding.buttonLogintypeCommon.layoutLoginType.setOnClickListener {
            findNavController().navigate(R.id.action_loginTypeFragment_to_loginFragment)
        }
        binding.textViewLogintypeRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_loginTypeFragment_to_userRegistrationFragment)
        }
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.loginWithGoogleEvent.collect { result ->
                if (result.isSuccess) {
                    navigateActivity(MainActivity::class.java)
                } else {
                    ApiErrorHandler.printMessage(result.exceptionOrNull()?.message)
                }
            }
        }
    }

    private fun googleLogin() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
        requestActivity.launch(googleSignInClient.signInIntent)
    }
}
