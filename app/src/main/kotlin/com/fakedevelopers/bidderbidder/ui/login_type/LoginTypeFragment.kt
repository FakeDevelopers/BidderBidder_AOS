package com.fakedevelopers.bidderbidder.ui.login_type

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.bidderbidder.HiltApplication
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.WEB_CLIENT_ID
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginTypeBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginTypeFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var _binding: FragmentLoginTypeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login_type,
            container,
            false
        )
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        firebaseAuth = FirebaseAuth.getInstance()
        return binding.root
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
        binding.buttonLogintypeGoogle.layoutLoginType.setOnClickListener {
            googleLogin()
        }
        binding.buttonLogintypeCommon.layoutLoginType.setOnClickListener {
            findNavController().navigate(R.id.action_loginTypeFragment_to_loginFragment)
        }
        binding.textViewLogintypeRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_loginTypeFragment_to_phoneAuthFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun googleLogin() {
        val signInIntent = googleSignInClient.signInIntent
        requestActivity.launch(signInIntent)
    }

    val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data)
            // result가 성공했을 때 이 값을 firebase에 넘겨주기
            if (result!!.isSuccess) {
                val account = result.signInAccount
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Login, 아이디와 패스워드가 맞았을 때
                Toast.makeText(requireActivity(), "success", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_loginTypeFragment_to_productListFragment)
            } else {
                // Show the error message, 아이디와 패스워드가 틀렸을 때
                Toast.makeText(requireActivity(), task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
