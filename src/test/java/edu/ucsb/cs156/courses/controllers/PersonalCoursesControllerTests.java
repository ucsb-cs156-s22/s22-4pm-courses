package edu.ucsb.cs156.courses.controllers;

import edu.ucsb.cs156.courses.documents.Course;
import edu.ucsb.cs156.courses.repositories.UserRepository;
import edu.ucsb.cs156.courses.testconfig.TestConfig;
import edu.ucsb.cs156.courses.ControllerTestCase;
import edu.ucsb.cs156.courses.entities.PersonalSchedule;
import edu.ucsb.cs156.courses.entities.Courses;
import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.repositories.PersonalScheduleRepository;
import edu.ucsb.cs156.courses.repositories.CoursesRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = PersonalCoursesController.class)
@Import(TestConfig.class)
public class PersonalCoursesControllerTests extends ControllerTestCase {

    @MockBean
    PersonalScheduleRepository personalscheduleRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    CoursesRepository coursesRepository;

    // Authorization tests for /api/personalschedules/admin/psid/sections/all

    @Test
    public void api_psid_sections_admin_all__logged_out__returns_403() throws Exception {
        mockMvc.perform(get("/api/courses/admin/psid/sections/all?psId=1"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_psid_sections_admin__user_logged_in__returns_403() throws Exception {
        mockMvc.perform(get("/api/courses/admin/psid/sections/all?psId=1"))
                .andExpect(status().is(403));
    }

    // tests with mocks for database actions
    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void api_psid_sections_admin__user_logged_in__returns_a_course_that_exists() throws Exception {
        User u = currentUserService.getCurrentUser().getUser();
        PersonalSchedule personalschedule1 = PersonalSchedule.builder().name("Name 1").description("Description 1").quarter("20221").user(u).id(1L).build();
        when(personalscheduleRepository.findByIdAndUser(eq(1L), eq(u))).thenReturn(Optional.of(personalschedule1));
        Courses course1 = Courses.builder().id(1L).user(u).enrollCd("59501").psId(1L).build();
        when(coursesRepository.findByIdAndUser(eq(1L), eq(u))).thenReturn(Optional.of(course1));
        MvcResult response = mockMvc.perform(get("/api/courses/admin/psid/sections/all?psId=1"))
                .andExpect(status().isOk()).andReturn();
        verify(personalscheduleRepository, times(1)).findByIdAndUser(1L, u);
        verify(coursesRepository, times(1)).findByIdAndUser(1L, u);
        
    }

}