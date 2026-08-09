package com.instaflow.app.model

enum class DownloadType {
    Audio,
    Video,
    Post,
    Command;

    companion object {
        val AUDIO = Audio
        val VIDEO = Video
        val POST = Post
        val COMMAND = Command
    }
}
