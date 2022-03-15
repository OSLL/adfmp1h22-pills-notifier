package com.example.pillnotifier.model

class Profile(name: String, nickname: String) {
    var name: String = name
        private set
    var nickname: String = nickname
        private set
}

class ProfilesList(val list_name: String, val profiles: List<Profile>)