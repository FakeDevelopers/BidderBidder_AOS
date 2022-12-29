package com.fakedevelopers.bidderbidder.ui.loginType

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.bidderbidder.HiltApplication
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.WEB_CLIENT_ID
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginTypeBinding
import com.fakedevelopers.bidderbidder.ui.MainActivity
import com.fakedevelopers.bidderbidder.ui.base.BaseFragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginTypeFragment : BaseFragment<FragmentLoginTypeBinding>(
    R.layout.fragment_login_type
) {
    private lateinit var googleSignInClient: GoogleSignInClient

    private val viewModel: LoginTypeViewModel by viewModels()

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val result = it.data?.let { data -> Auth.GoogleSignInApi.getSignInResultFromIntent(data) }
            // result가 성공했을 때 이 값을 firebase에 넘겨주기
            if (result != null && result.isSuccess) {
                result.signInAccount?.let { account ->
                    viewModel.firebaseAuthWithGoogle(account)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireContext().applicationContext as HiltApplication).let {
            binding.textviewLogintypeWelcometext.text = it.setPartialTextColor(
                binding.textviewLogintypeWelcometext.text,
                R.color.bidderbidder_primary,
                0,
                4
            )
            binding.textViewLogintypeRegistration.text = it.setPartialTextColor(
                binding.textViewLogintypeRegistration.text,
                R.color.bidderbidder_primary,
                0,
                binding.textViewLogintypeRegistration.text.indexOf('?') + 1
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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signinGoogleResponse.collect {
                    if (it.isSuccessful) {
                        sendSnackBar("success")
                        navigateActivity(MainActivity::class.java)
                    } else {
                        sendSnackBar(it.errorBody().toString())
                    }
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
