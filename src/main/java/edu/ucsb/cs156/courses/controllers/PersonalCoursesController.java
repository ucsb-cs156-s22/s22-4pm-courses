package edu.ucsb.cs156.courses.controllers;

import edu.ucsb.cs156.courses.documents.Course;
import edu.ucsb.cs156.courses.entities.Courses;
import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.errors.EntityNotFoundException;
import edu.ucsb.cs156.courses.models.CurrentUser;
import edu.ucsb.cs156.courses.repositories.CoursesRepository;
import edu.ucsb.cs156.courses.services.UCSBCurriculumService;
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

import org.json.*;
import java.util.List;
import java.util.ArrayList;

import javax.validation.Valid;
import java.util.Optional;
import java.net.*;
import java.io.InputStream;

import java.util.Scanner;

import edu.ucsb.cs156.courses.entities.PersonalSchedule;
import edu.ucsb.cs156.courses.repositories.PersonalScheduleRepository;

@Api(description = "Courses")
@RequestMapping("/api/courses")
@RestController
@Slf4j
public class PersonalCoursesController extends ApiController {

    @Autowired
    private CoursesRepository coursesRepository;
    @Autowired
    private PersonalScheduleRepository personalScheduleRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UCSBCurriculumService ucsbCurriculumService;

    @ApiOperation(value = "List all courses for any specified psId (admin)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value="/admin/psid/sections/all",produces = "application/json")
    public Iterable<Course> allSectionsForPsId_Admin(
            @ApiParam("psId") @RequestParam Long psId) {
        //User currentUser = getCurrentUser().getUser();
        Iterable<Courses> courses = coursesRepository.findAllByPsId(psId);
        //.orElseThrow(() -> new EntityNotFoundException(Courses.class, psId));
        PersonalSchedule ps = personalScheduleRepository.findById(psId)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId));
        //PersonalSchedule ps = personalScheduleRepository.findByIdAndUser(psId, getCurrentUser().getUser()) // should be 
        //    .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId)); // so right now string has multiple psId's, how to parse and understand
        //  .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId));
        String qtr = ps.getQuarter();
        //String toReturn="";
        JSONArray returnSections = new JSONArray();
        List<Course> listSec = new ArrayList();
        List<String> jsons = new ArrayList();
        for(Courses crs: courses){ // are we still getting quarter as an input?
            // test link
            //toReturn = ("https://api.ucsb.edu/academics/curriculums/v3/classes/" + qtr + "/" + crs.getEnrollCd() + "?includeClassSections=true");
            try{
                /*URL url = new URL("https://api.ucsb.edu/academics/curriculums/v3/classsection/" + qtr + "/" + crs.getEnrollCd());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestProperty("accept", "application/json");
                c.setRequestProperty("ucsb-api-version", "3.0");
                c.setRequestProperty("ucsb-api-key", "vb8mlvnaJeqYiAGXP1qa5INS4noghlAR");
                InputStream is = c.getInputStream();
                Scanner s = new Scanner(is);
                String responseBody = s.useDelimiter("\\A").next();*/
                String responseBody = ucsbCurriculumService.getJSONbyQuarterAndEnroll(qtr, crs.getEnrollCd());
                jsons.add(responseBody);
                Course course = objectMapper.readValue(responseBody, Course.class);
                listSec.add(course);
                continue;



                /*JSONObject jsonObject = new JSONObject(responseBody);
                //toReturn = jsonObject.getString("quarter");
                //JSONObject jsonObject = new JSONObject(responseBody); //the incoming data from courses API
                JSONObject returnJsonObject = new JSONObject(); //the reformatted outgoing data for each section
                //general course information
                returnJsonObject.put("quarter", jsonObject.getString("quarter"));
                returnJsonObject.put("subjArea", jsonObject.getString("subjectArea"));
                returnJsonObject.put("crsId", jsonObject.getString("courseId"));
                returnJsonObject.put("title", jsonObject.getString("title"));
                //section specific information
                JSONArray sectionArr = jsonObject.getJSONArray("classSections");
                JSONObject obj=sectionArr.getJSONObject(0);
                returnJsonObject.put("enrollCode", obj.get("enrollCode"));
                returnJsonObject.put("enrolledTotal", obj.getString("enrolledTotal"));
                returnJsonObject.put("maxEnroll", obj.getString("maxEnroll"));
                //meeting specific information
                JSONArray timeLocArr = obj.getJSONArray("timeLocations");
                JSONObject obj2=timeLocArr.getJSONObject(0);
                returnJsonObject.put("room", obj2.getString("room"));
                returnJsonObject.put("bldg", obj2.getString("building"));
                returnJsonObject.put("days", obj2.getString("days"));
                returnJsonObject.put("beginTime", obj2.getString("beginTime"));
                returnJsonObject.put("endTime", obj2.getString("endTime"));
                //instructor specific information
                JSONArray instrArr = obj.getJSONArray("instructors");
                JSONObject obj3 = instrArr.getJSONObject(0);
                returnJsonObject.put("instructor", obj3.getString("instructor"));
                //creating list following section form? loaded with all the above info - check with personal schedule detail table
                returnSections.put(returnJsonObject);
                //listSec.add(returnJsonObject);*/
            }
            catch (Exception e){
                System.out.println("Exception " + e);
            }
        }
        //JSONArray not returning so until then returning as String array
        String[] arr=new String[returnSections.length()];
        for(int i=0; i<arr.length; i++) {
            arr[i]=returnSections.optString(i);
        }
        //return arr;

        return listSec;

        //return toReturn;

        //return returnSections;    
    }

    @ApiOperation(value = "List all courses for a specified psId if it belongs to the user")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(value="/psid/sections/all",produces = "application/json")
    public Iterable<Course> allSectionsForPsId(
            @ApiParam("psId") @RequestParam Long psId) {
        User currentUser = getCurrentUser().getUser();
        Iterable<Courses> courses = coursesRepository.findAllByPsIdAndUser(psId, currentUser);
        PersonalSchedule ps = personalScheduleRepository.findByIdAndUser(psId, currentUser)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId));
        String qtr = ps.getQuarter();
        //String toReturn="";
        JSONArray returnSections = new JSONArray();
        List<Course> listSec = new ArrayList();
        List<String> jsons = new ArrayList();
        for(Courses crs: courses){ // are we still getting quarter as an input?
            // test link
            //toReturn = ("https://api.ucsb.edu/academics/curriculums/v3/classes/" + qtr + "/" + crs.getEnrollCd() + "?includeClassSections=true");
            try{
                /*URL url = new URL("https://api.ucsb.edu/academics/curriculums/v3/classsection/" + qtr + "/" + crs.getEnrollCd());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestProperty("accept", "application/json");
                c.setRequestProperty("ucsb-api-version", "3.0");
                c.setRequestProperty("ucsb-api-key", "vb8mlvnaJeqYiAGXP1qa5INS4noghlAR");
                InputStream is = c.getInputStream();
                Scanner s = new Scanner(is);
                String responseBody = s.useDelimiter("\\A").next();*/
                String responseBody = ucsbCurriculumService.getJSONbyQuarterAndEnroll(qtr, crs.getEnrollCd());
                jsons.add(responseBody);
                Course course = objectMapper.readValue(responseBody, Course.class);
                listSec.add(course);
                continue;



                /*JSONObject jsonObject = new JSONObject(responseBody);
                //toReturn = jsonObject.getString("quarter");
                //JSONObject jsonObject = new JSONObject(responseBody); //the incoming data from courses API
                JSONObject returnJsonObject = new JSONObject(); //the reformatted outgoing data for each section
                //general course information
                returnJsonObject.put("quarter", jsonObject.getString("quarter"));
                returnJsonObject.put("subjArea", jsonObject.getString("subjectArea"));
                returnJsonObject.put("crsId", jsonObject.getString("courseId"));
                returnJsonObject.put("title", jsonObject.getString("title"));
                //section specific information
                JSONArray sectionArr = jsonObject.getJSONArray("classSections");
                JSONObject obj=sectionArr.getJSONObject(0);
                returnJsonObject.put("enrollCode", obj.get("enrollCode"));
                returnJsonObject.put("enrolledTotal", obj.getString("enrolledTotal"));
                returnJsonObject.put("maxEnroll", obj.getString("maxEnroll"));
                //meeting specific information
                JSONArray timeLocArr = obj.getJSONArray("timeLocations");
                JSONObject obj2=timeLocArr.getJSONObject(0);
                returnJsonObject.put("room", obj2.getString("room"));
                returnJsonObject.put("bldg", obj2.getString("building"));
                returnJsonObject.put("days", obj2.getString("days"));
                returnJsonObject.put("beginTime", obj2.getString("beginTime"));
                returnJsonObject.put("endTime", obj2.getString("endTime"));
                //instructor specific information
                JSONArray instrArr = obj.getJSONArray("instructors");
                JSONObject obj3 = instrArr.getJSONObject(0);
                returnJsonObject.put("instructor", obj3.getString("instructor"));
                //creating list following section form? loaded with all the above info - check with personal schedule detail table
                returnSections.put(returnJsonObject);
                //listSec.add(returnJsonObject);*/
            }
            catch (Exception e){
                System.out.println("Exception " + e);
            }
        }
        //JSONArray not returning so until then returning as String array
        String[] arr=new String[returnSections.length()];
        for(int i=0; i<arr.length; i++) {
            arr[i]=returnSections.optString(i);
        }
        //return arr;

        return listSec;   
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