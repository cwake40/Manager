package registrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class AcademicManager {

    private int maxCoursesAllowed;
    private HashMap<String, HashMap<Integer, AcademicSubject>> subjectCatalog;
    private HashMap<String, UniversityLearner> studentDirectory;

    public AcademicManager(int maxCourses) {
        maxCoursesAllowed = maxCourses < 1 ? 1 : maxCourses;
        subjectCatalog = new HashMap<>();
        studentDirectory = new HashMap<>();
    }

    public AcademicManager addSubject(String department, int courseNumber, int seatCount) {
        if (department == null || department.isEmpty() || courseNumber < 1 || seatCount < 1) {
            throw new IllegalArgumentException("Invalid subject parameters");
        }

        if (!subjectCatalog.containsKey(department)) {
            subjectCatalog.put(department, new HashMap<>());
        }

        HashMap<Integer, AcademicSubject> departmentSubjects = subjectCatalog.get(department);
        if (!departmentSubjects.containsKey(courseNumber)) {
            departmentSubjects.put(courseNumber, new AcademicSubject(department, courseNumber, seatCount, maxCoursesAllowed));
        }

        return this;
    }

    public boolean removeSubject(String department, int courseNumber) {
        if (department == null || department.isEmpty() || courseNumber < 1) {
            throw new IllegalArgumentException("Invalid subject parameters");
        }

        if (subjectCatalog.containsKey(department) && subjectCatalog.get(department).containsKey(courseNumber)) {
            subjectCatalog.get(department).remove(courseNumber).clearSubjectEnrollments();
            return true;
        }

        return false;
    }

    public int countSubjects() {
        return subjectCatalog.values().stream().mapToInt(HashMap::size).sum();
    }

    public boolean enrollStudent(String department, int courseNumber, String firstName, String lastName) {
        if (invalidParameters(department, courseNumber, firstName, lastName)) {
            throw new IllegalArgumentException("Invalid enrollment parameters");
        }

        AcademicSubject subject = getSubject(department, courseNumber);
        if (subject == null || subject.isSubjectFull() || subject.isStudentEnrolled(firstName, lastName)) {
            return false;
        }

        String studentKey = firstName + lastName;
        UniversityLearner learner = studentDirectory.computeIfAbsent(studentKey, k -> new UniversityLearner(firstName, lastName, maxCoursesAllowed));
        if (!learner.canEnrollInAnotherCourse()) {
            return false;
        }

        subject.registerStudent(studentKey, learner);
        learner.enrollInCourse();
        return true;
    }

    public int countStudentsInSubject(String department, int courseNumber) {
        AcademicSubject subject = getSubject(department, courseNumber);
        return subject != null ? subject.countEnrolledStudents() : 0;
    }

    public int countStudentsWithLastName(String department, int courseNumber, String lastName) {
        AcademicSubject subject = getSubject(department, courseNumber);
        return subject != null ? subject.countStudentsByLastName(lastName) : 0;
    }

    public boolean isStudentEnrolled(String department, int courseNumber, String firstName, String lastName) {
        AcademicSubject subject = getSubject(department, courseNumber);
        return subject != null && subject.isStudentEnrolled(firstName, lastName);
    }

    public int countCoursesStudentEnrolled(String firstName, String lastName) {
        return (int) subjectCatalog.values().stream()
            .flatMap(map -> map.values().stream())
            .filter(subject -> subject.isStudentEnrolled(firstName, lastName))
            .count();
    }

    public boolean withdrawStudent(String department, int courseNumber, String firstName, String lastName) {
        if (invalidParameters(department, courseNumber, firstName, lastName)) {
            throw new IllegalArgumentException("Invalid withdrawal parameters");
        }

        AcademicSubject subject = getSubject(department, courseNumber);
        if (subject == null || !subject.isStudentEnrolled(firstName, lastName)) {
            return false;
        }

        subject.unregisterStudent(firstName + lastName);
        studentDirectory.get(firstName + lastName).withdrawFromCourse();
        return true;
    }

    public boolean cancelStudentEnrollment(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty()) {
            throw new IllegalArgumentException("Invalid student name");
        }

        boolean removed = false;
        for (HashMap<Integer, AcademicSubject> department : subjectCatalog.values()) {
            for (AcademicSubject subject : department.values()) {
                if (subject.unregisterStudent(firstName + lastName)) {
                    studentDirectory.get(firstName + lastName).withdrawFromCourse();
                    removed = true;
                }
            }
        }

        return removed;
    }

    private boolean invalidParameters(String department, int courseNumber, String firstName, String lastName) {
        return department == null || department.isEmpty() || courseNumber < 1 ||
               firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty();
    }

    private AcademicSubject getSubject(String department, int courseNumber) {
        return subjectCatalog.containsKey(department) ? subjectCatalog.get(department).get(courseNumber) : null;
    }

    public void processRegistrations(Collection<String> fileNames) {
        HashSet<Thread> threads = new HashSet<>();

        for (String fileName : fileNames) {
            File file = new File(fileName);
            RegistrationProcessor processor = new RegistrationProcessor(file);
            Thread thread = new Thread(processor);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class RegistrationProcessor implements Runnable {
        private File file;

        public RegistrationProcessor(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split("\\s+");
                    if (parts[0].equalsIgnoreCase("addsubject")) {
                        String department = parts[1];
                        int courseNumber = Integer.parseInt(parts[2]);
                        int seatCount = Integer.parseInt(parts[3]);
                        synchronized (AcademicManager.this) {
                            addSubject(department, courseNumber, seatCount);
                        }
                    } else if (parts[0].equalsIgnoreCase("enrollstudent")) {
                        String department = parts[1];
                        int courseNumber = Integer.parseInt(parts[2]);
                        String firstName = parts[3];
                        String lastName = parts[4];
                        synchronized (AcademicManager.this) {
                            enrollStudent(department, courseNumber, firstName, lastName);
                        }
                    }
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
