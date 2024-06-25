package com.team2.chitchat.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.team2.chitchat.R
import com.team2.chitchat.data.repository.remote.request.users.LoginUserRequest
import com.team2.chitchat.databinding.FragmentLoginBinding
import com.team2.chitchat.ui.base.BaseFragment
import com.team2.chitchat.ui.dialogfragment.MessageDialogFragment
import com.team2.chitchat.ui.extensions.TAG
import com.team2.chitchat.ui.extensions.setErrorBorder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


/**
 * A simple [Fragment] subclass.
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()
    override fun inflateBinding() {
        binding = FragmentLoginBinding.inflate(layoutInflater)
    }

    override fun createViewAfterInflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        initListener()
    }

    override fun configureToolbarAndConfigScreenSections() {
        fragmentLayoutWithToolbar()
        hideToolbar()
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginStateFlow.collect {isOk->
                if (isOk) {
                    findNavController().navigate(R.id.action_loginFragment_to_chatListFragment)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.errorFlow.collect{errorModel->
                showErrorMessage(
                    message = errorModel.message,
                    listener = object: MessageDialogFragment.MessageDialogListener{
                        override fun positiveButtonOnclick(view: View) {
                            Log.d(this.TAG, "positiveButtonOnclick: ")
                        }
                    }
                )
            }
        }
        lifecycleScope.launch {
            viewModel.loadingFlow.collect{loading->
                showLoading(loading)
            }
        }

    }

    override fun viewCreatedAfterSetupObserverViewModel(view: View, savedInstanceState: Bundle?) {

    }
    private fun initListener() {

        binding?.apply {

            textButtonRegisterLoginF.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            }
            textVRegisterLoginF.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            }
            buttonLogin.setOnClickListener {
                val userInput = editTUserLoginFragment.text.toString()
                val passwordInput = editTPasswordLoginFragment.text.toString()
                if (userInput.isNotBlank() && passwordInput.isNotBlank()) {
                    viewModel.getAuthenticationUser(LoginUserRequest(
                        login = userInput, password = passwordInput
                    ))
                } else {
                    emptyEditText(
                        listOf(
                            editTUserLoginFragment to textVUserErrorLoginFragment,
                            editTPasswordLoginFragment to textVPasswordErrorLoginFragment
                        )
                    )
                }
            }
        }

    }
    private fun emptyEditText(pairOfEditTextToTextView: List<Pair<EditText,TextView>>) {

        pairOfEditTextToTextView.forEach { (editText, textView) ->
            if (editText?.text.toString().isBlank()) {
                textView?.text = getString(R.string.required_field)
                editText?.setErrorBorder(true, requireContext(), textView)
            }
        }
    }


}