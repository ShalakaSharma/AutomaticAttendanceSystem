package com.example.shalaka.automaticattendancesystem;

import java.sql.Time;

/**
 * Created by Shalaka on 4/7/2017.
 */

public class Course {

    private String courseName;
    private String courseId;
    private Time courseStartTime;
    private Time courseEndTime;
    private String courseDay;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Time getCourseStartTime() {
        return courseStartTime;
    }

    public void setCourseStartTime(Time courseStartTime) {
        this.courseStartTime = courseStartTime;
    }

    public Time getCourseEndTime() {
        return courseEndTime;
    }

    public void setCourseEndTime(Time courseEndTime) {
        this.courseEndTime = courseEndTime;
    }

    public String getCourseDay() {
        return courseDay;
    }

    public void setCourseDay(String courseDay) {
        this.courseDay = courseDay;
    }
}
