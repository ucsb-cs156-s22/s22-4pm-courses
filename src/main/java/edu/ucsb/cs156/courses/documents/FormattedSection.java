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
public class FormattedSection {
    private CourseInfo courseInfo;
    private SectionInfo sectionInfo;
}