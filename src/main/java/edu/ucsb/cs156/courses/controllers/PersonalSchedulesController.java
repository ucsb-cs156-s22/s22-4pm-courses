package edu.ucsb.cs156.courses.controllers;


import edu.ucsb.cs156.courses.entities.Courses;
import edu.ucsb.cs156.courses.entities.PersonalSchedule;

import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.errors.EntityNotFoundException;
import edu.ucsb.cs156.courses.models.CurrentUser;

import edu.ucsb.cs156.courses.repositories.CoursesRepository;
import edu.ucsb.cs156.courses.repositories.PersonalScheduleRepository;

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

import javax.validation.Valid;
import java.util.Optional;
import java.net.*;
import java.io.InputStream;

import java.util.Scanner;

@Api(description = "PersonalSchedules")
@RequestMapping("/api/personalschedules")
@RestController
@Slf4j
public class PersonalSchedulesController extends ApiController {

    @Autowired
    PersonalScheduleRepository personalscheduleRepository;

    @ApiOperation(value = "List all personal schedules")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin/all")
    public Iterable<PersonalSchedule> allUsersSchedules() {
        Iterable<PersonalSchedule> personalschedules = personalscheduleRepository.findAll();
        return personalschedules;
    }

    @ApiOperation(value = "List this user's personal schedules")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<PersonalSchedule> thisUsersSchedules() {
        CurrentUser currentUser = getCurrentUser();
        Iterable<PersonalSchedule> personalschedules = personalscheduleRepository.findAllByUserId(currentUser.getUser().getId());
        return personalschedules;
    }

    @ApiOperation(value = "Get a single personal schedule (if it belongs to current user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public PersonalSchedule getScheduleById(
            @ApiParam("id") @RequestParam Long id) {
        User currentUser = getCurrentUser().getUser();
        PersonalSchedule personalschedule = personalscheduleRepository.findByIdAndUser(id, currentUser)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, id));

        return personalschedule;
    }

    @ApiOperation(value = "Get a single personal schedule (no matter who it belongs to, admin only)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/admin")
    public PersonalSchedule getScheduleById_admin(
            @ApiParam("id") @RequestParam Long id) {
              PersonalSchedule personalschedule = personalscheduleRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, id));

        return personalschedule;
    }

    @ApiOperation(value = "Create a new personal schedule")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/post")
    public PersonalSchedule postSchedule(
            @ApiParam("name") @RequestParam String name,
            @ApiParam("description") @RequestParam String description,
            @ApiParam("quarter") @RequestParam String quarter) {
        CurrentUser currentUser = getCurrentUser();
        log.info("currentUser={}", currentUser);

        PersonalSchedule personalschedule = new PersonalSchedule();
        personalschedule.setUser(currentUser.getUser());
        personalschedule.setName(name);
        personalschedule.setDescription(description);
        personalschedule.setQuarter(quarter);
        PersonalSchedule savedPersonalSchedule = personalscheduleRepository.save(personalschedule);
        return savedPersonalSchedule;
    }

    @ApiOperation(value = "Delete a personal schedule owned by this user")
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("")
    public Object deleteSchedule(
            @ApiParam("id") @RequestParam Long id) {
        User currentUser = getCurrentUser().getUser();
        PersonalSchedule personalschedule = personalscheduleRepository.findByIdAndUser(id, currentUser)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, id));

          personalscheduleRepository.delete(personalschedule);

        return genericMessage("PersonalSchedule with id %s deleted".formatted(id));

    }

    @ApiOperation(value = "Delete another user's personal schedule")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/admin")
    public Object deleteSchedule_Admin(
            @ApiParam("id") @RequestParam Long id) {
              PersonalSchedule personalschedule = personalscheduleRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, id));

          personalscheduleRepository.delete(personalschedule);

        return genericMessage("PersonalSchedule with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single personal schedule (if it belongs to current user)")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping("")
    public PersonalSchedule putScheduleById(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid PersonalSchedule incomingSchedule) {
        User currentUser = getCurrentUser().getUser();
        PersonalSchedule personalschedule = personalscheduleRepository.findByIdAndUser(id, currentUser)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, id));

        personalschedule.setName(incomingSchedule.getName());
        personalschedule.setDescription(incomingSchedule.getDescription());
        personalschedule.setQuarter(incomingSchedule.getQuarter());

        personalscheduleRepository.save(personalschedule);

        return personalschedule;
    }

    @ApiOperation(value = "Update a single Schedule (regardless of ownership, admin only, can't change ownership)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/admin")
    public PersonalSchedule putScheduleById_admin(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid PersonalSchedule incomingSchedule) {
              PersonalSchedule personalschedule = personalscheduleRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, id));

        personalschedule.setName(incomingSchedule.getName());
        personalschedule.setDescription(incomingSchedule.getDescription());
        personalschedule.setQuarter(incomingSchedule.getQuarter());

        personalscheduleRepository.save(personalschedule);

        return personalschedule;
    }
    
    CoursesRepository coursesRepository;

    @ApiOperation(value = "Get all sections in a personal schedule") // does this need an admin setting?
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/getSectionsByPsId")
    public JSONArray getSectionsBypsId(@ApiParam("psId") @RequestParam long psId){
        Iterable<Courses> courses = coursesRepository.findAllBypsId(psId);
        User currentUser = getCurrentUser().getUser();
        PersonalSchedule ps = personalscheduleRepository.findByIdAndUser(psId, currentUser) // so right now string has multiple psId's, how to parse and understand
          .orElseThrow(() -> new EntityNotFoundException(PersonalSchedule.class, psId));
        String qtr = ps.getQuarter();
        JSONArray returnSections = new JSONArray();
        for(Courses crs: courses){ // are we still getting quarter as an input?
            // test link
            System.out.println("https://api.ucsb.edu/academics/curriculums/v3/classes/" + qtr + "/" + crs.getEnrollCd());
            try{
                URL url = new URL("https://api.ucsb.edu/academics/curriculums/v3/classes/" + qtr + "/" + crs.getEnrollCd());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestProperty("accept", "application/json");
                c.setRequestProperty("ucsb-api-version", "3.0");
                c.setRequestProperty("ucsb-api-key", "vb8mlvnaJeqYiAGXP1qa5INS4noghlAR");
                InputStream is = c.getInputStream();
                Scanner s = new Scanner(is);
                String responseBody = s.useDelimiter("\\A").next();
                JSONObject jsonObject = new JSONObject(responseBody); //the incoming data from courses API
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
                JSONObject obj3=timeLocArr.getJSONObject(0);
                returnJsonObject.put("instructor", obj3.getString("instructor"));
                //creating list following section form? loaded with all the above info - check with personal schedule detail table
                returnSections.put(returnJsonObject);
            }
            catch (Exception e){
                System.out.println("Exception " + e);
            }
        }
        return returnSections;
    }
}