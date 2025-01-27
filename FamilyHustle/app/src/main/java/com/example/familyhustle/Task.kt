package com.example.familyhustle

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TaskData(
    var id: String? = null,
    var name: String = "",
    var description: String = "",
    var dueDate: String = "",
    var houseName: String = "",
    var weekNumber: Int = 0,
    var completed: Boolean = false,
    var points: Int = 0
) {
    // Bez-argumentni konstruktor potreban za Firebase
    constructor() : this(
        id = null,
        name = "",
        description = "",
        dueDate = "",
        houseName = "",
        weekNumber = 0,
        completed = false,
        points = 0
    )
}
