package edu.ucsb.cs156.courses.controllers;

import edu.ucsb.cs156.courses.entities.PersonalSchedule;
import edu.ucsb.cs156.courses.entities.Courses;
import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.errors.EntityNotFoundException;
import edu.ucsb.cs156.courses.models.CurrentUser;
import edu.ucsb.cs156.courses.repositories.PersonalScheduleRepository;
import edu.ucsb.cs156.courses.repositories.CoursesRepository;
import edu.ucsb.cs156.courses.services.UCSBCurriculumService;
import edu.ucsb.cs156.courses.documents.Course;
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

@Api(description = "Personal Sections")
@RequestMapping("/api/personalSections")
@RestController
@Slf4j
public class PersonalSectionsController extends ApiController {
    @Autowired
    PersonalScheduleRepository personalScheduleRepository;
    
    @Autowired
    CoursesRepository coursesRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    UCSBCurriculumService ucsbCurriculumService;

    @ApiOperation(value = "List all sections given a psId")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value = "/all", produces = "application/json")
    public ArrayList<Course> getSectionsByPsId(@ApiParam("psId") @RequestParam Long psId) throws JsonProcessingException{
        
           // .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId));
        
        ArrayList<Course> sections = new ArrayList<Course>();
        ArrayList<String> jsons = new ArrayList<String>();
        Iterable<Courses> courses = coursesRepository.findAllByPsId(psId);
        //User u = courses.get(0).getUser();
        //PersonalSchedule ps = personalScheduleRepository.findByIdAndUser(psId, u);
        //String qtr = ps.getQuarter();
            //.orElseThrow(() -> new EntityNotFoundException(Courses.class, psId));;
        for (Courses crs:courses) {
            try{
                User u = crs.getUser();
                PersonalSchedule ps = personalScheduleRepository.findByIdAndUser(psId,u)
                .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId));
                String qtr = ps.getQuarter();
                String responseBody = ucsbCurriculumService.getJSONbyQtrEnrollCd(qtr, crs.getEnrollCd());
                jsons.add(responseBody);
                Course course = objectMapper.readValue(responseBody, Course.class);
                sections.add(course);
            }
            catch (Exception e){
                System.out.println("Exception " + e);
            }
        }
        return sections;
    }


    // in sachen's pull request
    @ApiOperation(value = "List all courses (admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/all")
    public Iterable<Courses> allUsersCourses() {
        Iterable<Courses> courses = coursesRepository.findAll();
        return courses;
    }

    @ApiOperation(value = "List all courses (user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user/all")
    public Iterable<Courses> thisUsersCourses() {
        CurrentUser currentUser = getCurrentUser();
        Iterable<Courses> courses = coursesRepository.findAllByUserId(currentUser.getUser().getId());
        return courses;
    }

    @ApiOperation(value = "List all courses for a specified psId (admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/psid/all")
    public Iterable<Courses> allCoursesForPsId(
            @ApiParam("psId") @RequestParam Long psId) {
        Iterable<Courses> courses = coursesRepository.findAllByPsId(psId);
        return courses;
    }

    @ApiOperation(value = "List all courses for a specified psId (user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user/psid/all")
    public Iterable<Courses> thisUsersCoursesForPsId(
            @ApiParam("psId") @RequestParam Long psId) {
        User currentUser = getCurrentUser().getUser();
        Iterable<Courses> courses = coursesRepository.findAllByPsIdAndUser(psId, currentUser);
        return courses;
    }

    @ApiOperation(value = "Get a single course (admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public Courses getCourseById_admin(
            @ApiParam("id") @RequestParam Long id) {
        Courses courses = coursesRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException(Courses.class, id));

        return courses;
    }

    @ApiOperation(value = "Get a single course (user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/user")
    public Courses getCourseById(
            @ApiParam("id") @RequestParam Long id) {
        User currentUser = getCurrentUser().getUser();
        Courses courses = coursesRepository.findByIdAndUser(id, currentUser)
            .orElseThrow(() -> new EntityNotFoundException(Courses.class, id));

        return courses;
    }


    @ApiOperation(value = "Create a new course (user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/post")
    public Courses postCourses(
            @ApiParam("enrollCd") @RequestParam String enrollCd,
            @ApiParam("psId") @RequestParam Long psId) {
        CurrentUser currentUser = getCurrentUser();
        log.info("currentUser={}", currentUser);
        // Check if psId exists
        PersonalSchedule checkPsId = personalScheduleRepository.findByIdAndUser(psId, currentUser.getUser())
            .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId));
        // Check if enrollCd exists


        Courses courses = new Courses();
        courses.setUser(currentUser.getUser());
        courses.setEnrollCd(enrollCd);
        courses.setPsId(psId);
        Courses savedCourses = coursesRepository.save(courses);
        return savedCourses;
    }

    @ApiOperation(value = "Delete a course (admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin")
    public Object deleteCourses_Admin(
            @ApiParam("id") @RequestParam Long id) {
              Courses courses = coursesRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException(Courses.class, id));

          coursesRepository.delete(courses);

        return genericMessage("Courses with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Delete a course (user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/user")
    public Object deleteCourses(
            @ApiParam("id") @RequestParam Long id) {
        User currentUser = getCurrentUser().getUser();
        Courses courses = coursesRepository.findByIdAndUser(id, currentUser)
          .orElseThrow(() -> new EntityNotFoundException(Courses.class, id));
        coursesRepository.delete(courses);
        return genericMessage("Courses with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single Course (admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/admin")
    public Courses putCourseById_admin(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Courses incomingCourses) {
              Courses courses = coursesRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException(Courses.class, id));

          courses.setEnrollCd(incomingCourses.getEnrollCd());
          courses.setPsId(incomingCourses.getPsId());

        coursesRepository.save(courses);

        return courses;
    }

    @ApiOperation(value = "Update a single course (user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("/user")
    public Courses putCoursesById(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Courses incomingCourses) {
        User currentUser = getCurrentUser().getUser();
        Courses courses = coursesRepository.findByIdAndUser(id, currentUser)
          .orElseThrow(() -> new EntityNotFoundException(Courses.class, id));

        courses.setEnrollCd(incomingCourses.getEnrollCd());
        courses.setPsId(incomingCourses.getPsId());

        coursesRepository.save(courses);

        return courses;
    }
}