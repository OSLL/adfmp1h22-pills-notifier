package com.example.pillnotifier.ui.login

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pillnotifier.MainActivity
import com.example.pillnotifier.R

import com.example.pillnotifier.databinding.ActivityRegisterBinding
import com.example.pillnotifier.model.DataHolder

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fullName = binding.fullname
        val username = binding.username
        val password = binding.password
        val register = binding.signUp
        val loading = binding.loading
        val login = binding.backToLogin

        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory())
            .get(RegisterViewModel::class.java)

        registerViewModel.registerFormState.observe(this@RegisterActivity, Observer {
            val registerState = it ?: return@Observer

            register.isEnabled = registerState.isDataValid

            if (registerState.fullNameError != null) {
                fullName.error = getString(registerState.fullNameError)
            }
            if (registerState.usernameError != null) {
                username.error = getString(registerState.usernameError)
            }
            if (registerState.passwordError != null) {
                password.error = getString(registerState.passwordError)
            }
        })

        registerViewModel.registerResult.observe(this@RegisterActivity, Observer {
            val registerResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (registerResult.error != null) {
                showRegistrationFailed(registerResult.error)
            }
            if (registerResult.success != null) {
                updateUiWithUser(registerResult.success)
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                DataHolder.setData("userId", registerResult.success.userId)
                DataHolder.setData("fullname", registerResult.success.fullname)
                DataHolder.setData("username", registerResult.success.username)
                startActivity(intent)
                setResult(Activity.RESULT_OK)
                //Complete and destroy login activity once successful
                finish()
            }
        })

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        fullName.afterTextChanged {
            registerViewModel.loginDataChanged(
                fullName.text.toString(),
                username.text.toString(),
                password.text.toString()
            )
        }

        username.afterTextChanged {
            registerViewModel.loginDataChanged(
                fullName.text.toString(),
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                registerViewModel.loginDataChanged(
                    fullName.text.toString(),
                    username.text.toString(),
                    password.text.toString()
                )
            }

        }
        register.setOnClickListener {
            loading.visibility = View.VISIBLE
            registerViewModel.register(fullName.text.toString(), username.text.toString(), password.text.toString())
        }
    }

    private fun updateUiWithUser(model: RegisteredUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.fullname
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showRegistrationFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}
