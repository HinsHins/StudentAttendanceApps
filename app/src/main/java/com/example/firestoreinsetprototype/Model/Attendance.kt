package com.example.firestoreinsetprototype.Model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class Attendance (
    override var id:String="",
    var studentId:String="",
    var status:Boolean=false,
    var time: Timestamp? = null,
    var timetableId: String = "",
    var lesson_type:String=""
): Model

