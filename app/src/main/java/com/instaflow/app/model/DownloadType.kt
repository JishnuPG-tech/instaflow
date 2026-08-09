package com.instaflow.app.model

enum class DownloadType {
    AUDIO,
    VIDEO,
    POST,
    COMMAND;

    companion object {
        val Audio = AUDIO
        val Video = VIDEO
        val Post = POST
        val Command = COMMAND
    }
}
