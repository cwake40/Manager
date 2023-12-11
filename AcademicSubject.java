package registrar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class AcademicSubject {

    private String subjectDepartment;
    private int subjectCode;
    private int totalSeats;
    private int maxCoursesPerStudent;
    private HashMap<String, Student> enrolledStudents;

    public AcademicSubject(String dept, int code, int seatCount, int maxAllowed) {
        subjectDepartment = dept;
        subjectCode = code;
        totalSeats = seatCount;
        maxCoursesPerStudent = maxAllowed;
        enrolledStudents = new HashMap<>();
    }

    public void registerStudent(String studentID, Student student) {
        enrolledStudents.put(studentID, student);
    }

    public boolean isStudentEnrolled(String firstName, String lastName) {
        return enrolledStudents.containsKey(firstName + lastName);
    }

    public void clearSubjectEnrollments() {
        for (Student student : enrolledStudents.values()) {
            student.dropCourse();
        }
        enrolledStudents.clear();
    }

    public boolean isSubjectFull() {
        return enrolledStudents.size() >= totalSeats;
    }

    public int countEnrolledStudents() {
        return enrolledStudents.size();
    }

    public int countStudentsByLastName(String lastName) {
        int count = 0;
        for (Student student : enrolledStudents.values()) {
            if (student.lastName(lastName)) {
                count++;
            }
        }
        return count;
    }

    public void unregisterStudent(String studentID) {
        enrolledStudents.remove(studentID);
    }
}
