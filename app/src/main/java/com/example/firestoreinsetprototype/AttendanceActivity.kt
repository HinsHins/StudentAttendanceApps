package com.example.firestoreinsetprototype

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestoreinsetprototype.Adaptor.AttendanceRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Adaptor.StudentRecyclerViewAdaptor
import com.example.firestoreinsetprototype.Constant.FirestoreCollectionPath
import com.example.firestoreinsetprototype.Document.AttendanceDocMapper
import com.example.firestoreinsetprototype.Document.TimetableDocMapper
import com.example.firestoreinsetprototype.Model.*
import com.example.firestoreinsetprototype.Util.realTimeUpdate
import com.example.firestoreinsetprototype.Util.retrieveData
import com.example.firestoreinsetprototype.Util.retrieveDataWithMatch
import com.example.firestoreinsetprototype.ViewModel.AttendanceViewModel
import com.example.firestoreinsetprototype.ViewModel.TimetableViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_attendance.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_student.*
import java.util.*
import kotlin.collections.ArrayList

class AttendanceActivity : AppCompatActivity() {
    private val studentsPath = FirestoreCollectionPath.STUDENTS_PATH
    private val attendancesPath = FirestoreCollectionPath.ATTENDANCES_PATH
//    private val attendances = ArrayList<Attendance>()
    private val auth = FirebaseAuth.getInstance()
    private val fb = FirebaseFirestore.getInstance()
    var currentTimetableId : String = ""
    lateinit var attendanceAdapter : AttendanceRecyclerViewAdaptor
    private val students = ArrayList<Student>()
    private val attendanceDocMapper = AttendanceDocMapper(ArrayList<Attendance>(),ArrayList<AttendanceViewModel>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)
        currentTimetableId = intent.getStringExtra("timetableId").toString()
        Log.d("currentTimetableId", "currentTimetableId: ${currentTimetableId}")

        val linearLayoutManager = LinearLayoutManager(this)
        attendance_recyclerView.layoutManager = linearLayoutManager
        attendance_recyclerView.adapter = AttendanceRecyclerViewAdaptor(attendanceDocMapper.attendanceViewModels)
        attendanceAdapter = (attendance_recyclerView.adapter as AttendanceRecyclerViewAdaptor)
        attendanceAdapter.onItemClickListener = object : AttendanceRecyclerViewAdaptor.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
            }

            override fun onLongClick(v: View, position: Int) : Boolean {
                Log.d("OnItemLongClickListener", "long pressed at $position")
                return true
            }
        }
        attendanceAdapter.onCheckboxClickListener = object :  AttendanceRecyclerViewAdaptor.OnCheckBoxClickListener {
            override fun onClick(v: View, position: Int) {
                Log.d("onClick", "$position")
                if (v !is CheckBox) {  return }
                val checked: Boolean = v.isChecked
                updateAttendance(checked,attendanceDocMapper.attendances[position])
            }

        }

        retrieveStudents()
    }

    private fun retrieveAttendance() {
        val attendanceCollection = fb.collection(attendancesPath)
        attendanceCollection.retrieveDataWithMatch("timetableId",currentTimetableId,attendanceDocMapper.attendances as ArrayList<Model>,
            Attendance::class.java) {
            Log.d("attendances", "retrieveAttendance: ${attendanceDocMapper.attendances}")
            attendanceDocMapper.updateViewModel(students)
            attendanceAdapter.notifyDataSetChanged()
        }
    }

    private fun retrieveStudents() {
        val studentCollection = fb.collection(studentsPath)
        studentCollection.retrieveData(students as ArrayList<Model>, Student::class.java){
            retrieveAttendance()
        }
    }

    fun updateAttendance(isPresent : Boolean,attendance: Attendance){
        val attendanceRef = fb.collection(attendancesPath).document(attendance.id)
        var currentTime : Timestamp? = if(isPresent) Timestamp(Date()) else null
//        if(isPresent){
           attendanceRef.update("time",currentTime).addOnSuccessListener {
               val index = attendanceDocMapper.attendances.indexOf(attendance)
               attendanceDocMapper.attendances[index].time = currentTime
               attendanceDocMapper.updateViewModel(students)
               attendanceAdapter.notifyDataSetChanged()
           }
//        }
    }



}
