package edu.ucsb.cs156.courses.documents;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionInfo {
    private String enrollCode;
    private String enrolledTotal;
    private String maxEnroll;
    // in timeloc
    private List<FormattedTimeLocation> timeLocations;
    // in instr
    private List<FormattedInstructor> instructors;
}