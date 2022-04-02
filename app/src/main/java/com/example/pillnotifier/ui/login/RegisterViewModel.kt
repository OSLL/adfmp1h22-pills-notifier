package com.example.pillnotifier.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillnotifier.Constants
import com.example.pillnotifier.data.Result

import com.example.pillnotifier.R
import com.example.pillnotifier.data.model.LoggedInUser
import com.example.pillnotifier.fragments.MedicineFragment
import com.example.pillnotifier.model.UserInfo
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RegisterViewModel() : ViewModel() {

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    private suspend fun _register(
        fullname: String,
        username: String,
        password: String
    ): Result<LoggedInUser> {
        return suspendCoroutine { cont ->
            val user = UserInfo(fullname, username, password, null)
            val gson = Gson()
            val userJson = gson.toJson(user)
            val client = OkHttpClient.Builder().build()

            val body: RequestBody = userJson.toRequestBody("application/json".toMediaTypeOrNull())

            lateinit var request: Request
            try {
                request = Request.Builder()
                    .url(Constants.BASE_URL + "/user/register")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
            } catch (e: IllegalArgumentException) {
                cont.resume(Result.Error(e))
                return@suspendCoroutine
            }

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resume(Result.Error(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val message: String = response.body!!.string()
                    if (response.code == 200) {
                        val loggedInUser = gson.fromJson(message, LoggedInUser::class.java)
                        cont.resume(Result.Success(loggedInUser))
                    } else {
                        onFailure(call, IOException(message))
                    }
                }
            })
        }
    }

    fun register(fullName: String, username: String, password: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                _register(fullName, username, password)
            }
            if (result is Result.Success) {
                _registerResult.value =
                    RegisterResult(
                        success = RegisteredUserView(
                            username = result.data.username,
                            fullname = result.data.fullname,
                            userId = result.data.userId
                        )
                    )
            } else {
                _registerResult.value = RegisterResult(error = R.string.registration_failed)
            }
        }
    }

    fun loginDataChanged(fullname: String, username: String, password: String) {
        if (!isUserNameValid(fullname)) {
            _registerForm.value = RegisterFormState(fullNameError = R.string.invalid_fullname)
        } else if (!isUserNameValid(username)) {
            _registerForm.value = RegisterFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}