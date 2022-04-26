package com.fakedevelopers.bidderbidder.ui.login_type

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentLoginTypeBinding

class LoginTypeFragment : Fragment() {

    private val binding: FragmentLoginTypeBinding by viewBinding(createMethod = CreateMethod.INFLATE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        // 로그인 버튼을 누르면 로그인 프래그먼트로 넘어갑니다.
        binding.buttonLogintypeCommonlogin.setOnClickListener {
            navController.navigate(R.id.action_loginTypeFragment_to_loginFragment)
        }
    }
}
