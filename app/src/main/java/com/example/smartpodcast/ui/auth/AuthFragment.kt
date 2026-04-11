package com.example.smartpodcast.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smartpodcast.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            viewModel.login(etEmail.text.toString(), etPassword.text.toString())
        }

        btnRegister.setOnClickListener {
            viewModel.register(etEmail.text.toString(), etPassword.text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Idle -> {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        btnRegister.isEnabled = true
                    }
                    is AuthState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        btnLogin.isEnabled = false
                        btnRegister.isEnabled = false
                    }
                    is AuthState.Success -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Thành công: Xin chào ${state.user.email}", Toast.LENGTH_SHORT).show()

                        // Quay lại trang Home khi đăng nhập thành công
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, com.example.smartpodcast.ui.home.HomeFragment())
                            .commit()
                    }
                    is AuthState.Error -> {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        btnRegister.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
