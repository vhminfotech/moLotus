package com.sms.moLotus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sms.moLotus.entity.User

class UsersViewModel(application: Application) : AndroidViewModel(application) {
    private val _getUserViewModel = MutableLiveData<List<User>>()
    private val getUserViewModel: LiveData<List<User>> = _getUserViewModel
    fun addListUsers(listUser: List<User>) {
        _getUserViewModel.postValue(listUser)
    }

    fun getAllUser() = getUserViewModel
}