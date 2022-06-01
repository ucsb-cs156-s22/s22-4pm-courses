/*package edu.ucsb.cs156.courses.controllers;


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

@Api(description = "Courses")
@RequestMapping("/api/courses")
@RestController
@Slf4j
public class CoursesController extends ApiController {
    @Autowired
    CoursesRepository coursesRepository;
    PersonalScheduleRepository personalscheduleRepository;

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

}*/