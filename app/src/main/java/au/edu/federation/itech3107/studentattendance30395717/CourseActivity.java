package au.edu.federation.itech3107.studentattendance30395717;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.necer.calendar.BaseCalendar;
import com.necer.calendar.Miui10Calendar;
import com.necer.calendar.MonthCalendar;
import com.necer.calendar.WeekCalendar;
import com.necer.enumeration.CheckModel;
import com.necer.enumeration.DateChangeBehavior;
import com.necer.listener.OnCalendarChangedListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CourseActivity extends AppCompatActivity {
    private int currentWeek = 1; // Start with "Week 1"
    private int currentEndWeek = 1;
    private String UpClassDate;
    private TextInputEditText edtCourseId, edtCourseName;
    private TextView tvStartDate , tvEndDate;
    private CourseDao courseDao;
    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        // Initialize UI components
        edtCourseId = findViewById(R.id.edtCourseId);
        edtCourseName = findViewById(R.id.edtCourseName);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);

        Button endToLastPager = findViewById(R.id.end_toLastPager);

       TextView txEndPager = findViewById(R.id.tx_end_pager);
        Button endToNextPager =  findViewById(R.id.end_toNextPager);





        Button btnSave = findViewById(R.id.btnSave);

        WeekCalendar weekCalendar = findViewById(R.id.btnPickStartDate);
        weekCalendar.setCheckMode(CheckModel.SINGLE_DEFAULT_CHECKED);
        weekCalendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(BaseCalendar baseCalendar, int year, int month, org.joda.time.LocalDate localDate, DateChangeBehavior dateChangeBehavior) {
                tvStartDate.setText(localDate.toString());

            }
        });


        WeekCalendar weekUpclassDate = findViewById(R.id.upclassDate);
        weekUpclassDate.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(BaseCalendar baseCalendar, int year, int month, org.joda.time.LocalDate localDate, DateChangeBehavior dateChangeBehavior) {
                UpClassDate = localDate.toString();
            }
        });

        Button buttonlast = findViewById(R.id.toLastPager);
        Button buttonnext = findViewById(R.id.toNextPager);
        TextView txNextPager = findViewById(R.id.tx_pager);

        buttonlast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentWeek>1){
                    currentWeek --;
                    txNextPager.setText(currentWeek + "week");
                    weekCalendar.toLastPager();
                }

            }
        });

        buttonnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weekCalendar.toNextPager();
                currentWeek ++;
                txNextPager.setText( currentWeek + "week");
            }
        });


        WeekCalendar weekCalendar2 = findViewById(R.id.btnPickEndDate);
        weekCalendar2.setCheckMode(CheckModel.SINGLE_DEFAULT_CHECKED);
        weekCalendar2.setOnCalendarChangedListener((baseCalendar, year, month, localDate, dateChangeBehavior) ->
                tvEndDate.setText(localDate.toString()));



        endToLastPager.setOnClickListener(view -> {
            if (currentEndWeek>1){
                currentEndWeek --;
                txEndPager.setText(currentEndWeek + "week");
                weekCalendar2.toLastPager();
            }
        });

        endToNextPager.setOnClickListener(view -> {
            weekCalendar2.toNextPager();
            currentEndWeek ++;
            txEndPager.setText( currentEndWeek + "week");
        });




        // Initialize Room database
        AppDatabase database = AppDatabase.getDatabase(this);
        courseDao = database.courseDao();



        // Save button
        btnSave.setOnClickListener(v -> {
            String courseId = edtCourseId.getText().toString().trim();
            String courseName = edtCourseName.getText().toString().trim();
            String startDate = tvStartDate.getText().toString().trim();
            String endDate = tvEndDate.getText().toString().trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Adjust according to your date format
            LocalDate currentStartDate = LocalDate.parse(UpClassDate, formatter);

            List<String> datesList = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                datesList.add(currentStartDate.format(formatter));
                currentStartDate = currentStartDate.plusDays(7);
            }


            Course course = new Course();
            try {
                int parsedCourseId = Integer.parseInt(courseId);
                course.setCourseId(parsedCourseId);
            } catch(NumberFormatException e) {
                Toast.makeText(CourseActivity.this, "inputOverrun", Toast.LENGTH_SHORT).show();
                return;
            }
            course.setCourseName(courseName);
            course.setStartDate(startDate);
            course.setEndDate(UpClassDate);
            course.setListData(datesList);

            new Thread(() -> {
                long result = courseDao.insert(course);

                // Return to the main thread to process the results
                runOnUiThread(() -> {
                    if (result != -1) {
                        Toast.makeText(CourseActivity.this, "Course Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                        Intent intent = new Intent(CourseActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CourseActivity.this, "Error Adding Course", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();

        });
    }


}
