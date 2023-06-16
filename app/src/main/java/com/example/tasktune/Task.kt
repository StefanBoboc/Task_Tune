package com.example.tasktune
import android.os.Parcel
import android.os.Parcelable

data class Task(
    var title: String? = null,
    var date: String? = null,
    var startHour: String? = null,
    var endHour: String? = null,
    var tag: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(date)
        parcel.writeString(startHour)
        parcel.writeString(endHour)
        parcel.writeString(tag)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}

fun sortTasksByStartHour(taskList: MutableList<Task>) {
    taskList.sortBy { it.startHour }
}
