package com.sms.moLotus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sms.moLotus.entity.Group

class GroupViewModel(application: Application) : AndroidViewModel(application) {
    private val _getGroupViewModel = MutableLiveData<List<Group>>()
    private val getGroupViewModel: LiveData<List<Group>> = _getGroupViewModel
    fun addListGroup(listUser: List<Group>) {
        _getGroupViewModel.postValue(listUser)
    }

    fun getAllGroup() = getGroupViewModel
}