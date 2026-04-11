package com.example.smartpodcast.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smartpodcast.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class UiState { WELCOME, LOGIN, REGISTER }

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private val viewModel: AuthViewModel by viewModels()
    private var currentUiState = UiState.WELCOME

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val llWelcome = view.findViewById<LinearLayout>(R.id.llWelcome)
        val llForm = view.findViewById<LinearLayout>(R.id.llForm)

        val btnWelcomeLogin = view.findViewById<Button>(R.id.btnWelcomeLogin)
        val btnWelcomeRegister = view.findViewById<Button>(R.id.btnWelcomeRegister)

        val tvFormTitle = view.findViewById<TextView>(R.id.tvFormTitle)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val tvBackToWelcome = view.findViewById<TextView>(R.id.tvBackToWelcome)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        fun updateUiState(state: UiState) {
            currentUiState = state
            when (state) {
                UiState.WELCOME -> {
                    llWelcome.visibility = View.VISIBLE
                    llForm.visibility = View.GONE
                    etEmail.text.clear()
                    etPassword.text.clear()
                }
                UiState.LOGIN -> {
                    llWelcome.visibility = View.GONE
                    llForm.visibility = View.VISIBLE
                    tvFormTitle.text = "ĐĂNG NHẬP TÀI KHOẢN"
                    // Giao diện đã có background màu primary cho form Đăng nhập
                    btnSubmit.setBackgroundResource(R.drawable.bg_button_primary)
                    btnSubmit.text = "ĐĂNG NHẬP"
                }
                UiState.REGISTER -> {
                    llWelcome.visibility = View.GONE
                    llForm.visibility = View.VISIBLE
                    tvFormTitle.text = "TẠO TÀI KHOẢN MỚI"
                    // Thay đổi màu nút cho khác một chút trong form Đăng ký (nếu cần thiết)
                    btnSubmit.setBackgroundResource(R.drawable.bg_button_secondary)
                    btnSubmit.text = "ĐĂNG KÝ"
                }
            }
        }

        btnWelcomeLogin.setOnClickListener { updateUiState(UiState.LOGIN) }
        btnWelcomeRegister.setOnClickListener { updateUiState(UiState.REGISTER) }
        tvBackToWelcome.setOnClickListener { updateUiState(UiState.WELCOME) }

        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            if (currentUiState == UiState.LOGIN) {
                viewModel.login(email, pass)
            } else {
                viewModel.register(email, pass)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collectLatest { state ->
                when (state) {
                    is AuthState.Idle -> {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                    }
                    is AuthState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        btnSubmit.isEnabled = false
                    }
                    is AuthState.Success -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Thành công: Xin chào ${state.user.email}", Toast.LENGTH_SHORT).show()

                        // Quay lại trang Home khi đăng nhập thành công
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, com.example.smartpodcast.ui.home.HomeFragment())
                            .commit()
                    }
                    is AuthState.RegisterSuccess -> {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        Toast.makeText(context, "Đăng ký thành công, vui lòng đăng nhập!", Toast.LENGTH_LONG).show()

                        // Chuyển luôn sang trạng thái LOGIN để sẵn sàng nhập liệu
                        updateUiState(UiState.LOGIN)
                    }
                    is AuthState.Error -> {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Bắt đầu với màn hình Welcome
        updateUiState(UiState.WELCOME)
    }
}
