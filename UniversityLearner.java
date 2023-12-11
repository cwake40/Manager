package registrar;

public class UniversityLearner {

    private String givenName;
    private String familyName;
    private int enrolledCourseCount;
    private int maxCourseLimit;

    public UniversityLearner(String givenName, String familyName, int maxAllowedCourses) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.maxCourseLimit = maxAllowedCourses;
        this.enrolledCourseCount = 0;
    }

    public void enrollInCourse() {
        enrolledCourseCount++;
    }

    public void withdrawFromCourse() {
        if (enrolledCourseCount > 0) {
            enrolledCourseCount--;
        }
    }

    public boolean isSameLearner(UniversityLearner otherLearner) {
        return this.givenName.equals(otherLearner.givenName) && this.familyName.equals(otherLearner.familyName);
    }

    public boolean canEnrollInAnotherCourse() {
        return enrolledCourseCount < maxCourseLimit;
    }

    public boolean hasMatchingLastName(String lastName) {
        return this.familyName.equals(lastName);
    }
}
