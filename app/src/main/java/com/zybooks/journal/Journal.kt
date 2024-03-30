package com.zybooks.journal

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

// Journal class implements Parcelable to allows instances of the class to be serialized and deserialized
// which is necessary for saving and restoring state.
data class Journal(val id: String, val text: String, val dateTime: Date): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        Date(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(text)
        parcel.writeLong(dateTime.time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Journal> {
        override fun createFromParcel(parcel: Parcel): Journal {
            return Journal(parcel)
        }

        override fun newArray(size: Int): Array<Journal?> {
            return arrayOfNulls(size)
        }
    }
}
