package edu.ucsb.cs156.courses.controllers;

import edu.ucsb.cs156.courses.documents.CoursePage;
import edu.ucsb.cs156.courses.documents.CoursePageFixtures;
import edu.ucsb.cs156.courses.entities.PersonalSchedule;
import edu.ucsb.cs156.courses.entities.Courses;
import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.errors.EntityNotFoundException;
import edu.ucsb.cs156.courses.models.CurrentUser;
import edu.ucsb.cs156.courses.repositories.CoursesRepository;
import edu.ucsb.cs156.courses.services.UCSBCurriculumService;
import edu.ucsb.cs156.courses.documents.ConvertedSection;
import edu.ucsb.cs156.courses.documents.CourseInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import java.util.Optional;

@Api(description = "Sections")
@RequestMapping("/api/sections")
@RestController
@Slf4j
public class SectionController extends ApiController {

    @Autowired
    CoursesRepository coursesRepository;

    @Autowired
    UCSBCurriculumService ucsbCurriculumService;


    @ApiOperation(value = "List sections in a personal schedule")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value = "/getsections", produces = "application/json")
    public Iterable<CourseInfo> getSectionsByPersonalScheduleID (
          @ApiParam("personal schedule id") @RequestParam Long psId) throws JsonProcessingException {

        ArrayList<CourseInfo> convertedSections = new ArrayList<CourseInfo>();
        Iterable<Courses> courses = coursesRepository.findAllByPsId(psId);
        for (Courses c: courses) {
            List<CourseInfo> convertedSection = ucsbCurriculumService.getConvertedSectionsByQuarterAndEnroll(c.getQuarter(), c.getEnrollCd());
            convertedSections.addAll(convertedSection);
        }

        return convertedSections;
    }
}
