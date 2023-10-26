package au.edu.federation.itech3107.studentattendance30395717;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class AttendanceActivity extends AppCompatActivity {

    private Spinner dateSpinner;
    private RecyclerView studentsRecyclerView;
    private StudentAttendanceAdapter adapter;
    private AppDatabase appDatabase;
    private int courseId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        courseId = getIntent().getIntExtra("COURSE_ID", -1);
        Log.e("获取课程courseId", courseId +"");

        appDatabase = AppDatabase.getDatabase(this);

        dateSpinner = findViewById(R.id.dateSpinner);
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        Button saveAttendanceButton = findViewById(R.id.saveAttendanceButton);
        new Thread(new Runnable() {
            @Override
            public void run() {


                List<StudentCourse> students = getStudentsFromDatabase(courseId);

                StudentCourseDao studentCourseDao = appDatabase.studentCourseDao();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new StudentAttendanceAdapter(AttendanceActivity.this, students,studentCourseDao);
                        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(AttendanceActivity.this));
                        studentsRecyclerView.setAdapter(adapter);
                    }
                });
            }
        }).start();

        saveAttendanceButton.setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveAttendance();
                }
            }).start();


        });




        new Thread(new Runnable() {
            @Override
            public void run() {
                Course courseByBean = appDatabase.courseDao().getCourseById(courseId);
                List<String> listData = courseByBean.getListData();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(AttendanceActivity.this, android.R.layout.simple_spinner_item, listData);
                        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        dateSpinner.setAdapter(dateAdapter);
                    }
                });
            }
        }).start();
    }

    private List<StudentCourse> getStudentsFromDatabase(int courseId) {

        return appDatabase.studentCourseDao().getStudentByCourseId(courseId);
    }

    private void saveAttendance() {
        String selectedDate = dateSpinner.getSelectedItem().toString();
        SparseBooleanArray attendance = adapter.getAttendanceState();

        for (int i = 0; i < attendance.size(); i++) {
            int studentId = attendance.keyAt(i);
            boolean isPresent = attendance.valueAt(i);

            // Before attempting to save attendance information, check if the studentId exists in the StudentCourse table
            if (appDatabase.attendanceDao().studentCourseExists(studentId) > 0) {

                Attendance newAttendance = new Attendance();
                newAttendance.setAttendanceDate(selectedDate);
                newAttendance.setStudentId(studentId);
                newAttendance.setPresent(isPresent);
                newAttendance.setCourse_id(courseId);
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        appDatabase.attendanceDao().insert(newAttendance);
                    }
                }).start();
                runOnUiThread(() -> Toast.makeText(this, "Attendance Saved", Toast.LENGTH_SHORT).show());

            } else {

                runOnUiThread(() -> Toast.makeText(this, "Invalid student ID: " + studentId, Toast.LENGTH_SHORT).show());
            }
        }
    }

}
