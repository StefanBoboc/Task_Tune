package com.example.tasktune

data class DayTasks(
    val date: String = "",
    val events: List<Task> = listOf()
)
