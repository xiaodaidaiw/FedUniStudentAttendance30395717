package au.edu.federation.itech3107.studentattendance30395717;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.necer.calendar.Miui10Calendar;
import com.necer.calendar.MonthCalendar;
import com.necer.enumeration.CheckModel;

import java.util.List;
import java.util.logging.Logger;


public class AttendanceActivity2 extends AppCompatActivity {

    private ListView attendanceListView;
    private AttendanceDao attendanceDao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance2);


        attendanceListView = findViewById(R.id.attendanceListView);

        AppDatabase database = AppDatabase.getDatabase(this);
        attendanceDao = database.attendanceDao();
        Miui10Calendar weekCalendar = findViewById(R.id.miui10Calendar);
        weekCalendar.setCheckMode(CheckModel.SINGLE_DEFAULT_CHECKED);
        weekCalendar.setOnCalendarChangedListener((baseCalendar, year, month, localDate, dateChangeBehavior) -> {

            String selectedDate = localDate.toString();
            Log.e("选中的日期：",selectedDate);
            displayAttendance(selectedDate);

        });

    }

    private void displayAttendance(String date) {
        new Thread(() -> {
            List<Attendance> attendanceList = attendanceDao.getAttendanceByDate(date);

                new Handler(Looper.getMainLooper()).post(() -> {

                    AttendanceAdapter adapter = new AttendanceAdapter(this, attendanceList);
                    attendanceListView.setAdapter(adapter);
                });

        }).start();

    }

}
