package edu.ucsb.cs156.courses.controllers;
import edu.ucsb.cs156.courses.entities.PersonalCourses;
import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.errors.EntityNotFoundException;
import edu.ucsb.cs156.courses.models.CurrentUser;
import edu.ucsb.cs156.courses.repositories.PersonalCoursesRepository;
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
import javax.validation.Valid;
import java.util.Optional;
import java.util.ArrayList;

@Api(description = "PersonalCourses")
//CHECK
@RequestMapping("/api/personalcourses")
//
@RestController
@Slf4j
public class PersonalCoursesController extends ApiController {

    
    @Autowired
    PersonalCoursesRepository personalcoursesRepository;
    
    @ApiOperation(value = "List all personal courses")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<PersonalCourses> allPersonalCourses() {
        Iterable<PersonalCourses> personalcourses = personalcoursesRepository.findAll();
        return personalcourses;
    }
    
    @ApiOperation(value = "Add a new personal course")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/add")
    public PersonalCourses addCourse(
            @ApiParam("enrollCd") @RequestParam String enrollCd,
            @ApiParam("psID") @RequestParam long psId,
            @ApiParam("Quarter (in yyyyq)") @RequestParam String quarter) {
        CurrentUser currentUser = getCurrentUser();
        log.info("currentUser={}", currentUser);

        PersonalCourses personalcourse = new PersonalCourses();
        personalcourse.setEnrollCd(enrollCd);
        personalcourse.setQuarter(quarter);
        personalcourse.setPsId(psId);
        PersonalCourses savedPersonalCourse = personalcoursesRepository.save(personalcourse);
        return savedPersonalCourse;
    }

    @ApiOperation(value = "List all enroll cd's of the sections in a personal schedule")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all/enrollCd")
    public Iterable<String> allEnrollCds(
          @ApiParam("psID") @RequestParam long psId
    ){
      Iterable<PersonalCourses> personalcourses = personalcoursesRepository.findAllByPsId(psId);
      ArrayList<String> enrollCds = new ArrayList<String>();

      for (PersonalCourses p: personalcourses){
        enrollCds.add(p.getEnrollCd());
      }
      return enrollCds;
    }
    
}
