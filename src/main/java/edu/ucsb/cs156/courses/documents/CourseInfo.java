package edu.ucsb.cs156.courses.documents;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CourseInfo is an object that stores all of the information about a
 * course from the UCSB Courses API except for the section info
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseInfo {
    private String quarter;
    private String courseId;
    private String title;
    private int contactHours;
    private String description;
    private String college;
    private String objLevelCode;
    private String subjectArea;
    private int unitsFixed;
    private int unitsVariableHigh;
    private int unitsVariableLow;
    private boolean delayedSectioning;
    private boolean inProgressCourse;
    private String gradingOption;
    private String instructionType;
    private boolean onLineCourse;
    private String deptCode;
    private List<GeneralEducation> generalEducation;
    private List<Section> classSections;
}
