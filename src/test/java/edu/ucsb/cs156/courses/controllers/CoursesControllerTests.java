package edu.ucsb.cs156.courses.controllers;

import edu.ucsb.cs156.courses.repositories.UserRepository;
import edu.ucsb.cs156.courses.testconfig.TestConfig;
import edu.ucsb.cs156.courses.ControllerTestCase;
import edu.ucsb.cs156.courses.entities.Courses;
import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.repositories.CoursesRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

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

@WebMvcTest(controllers = CoursesController.class)
@Import(TestConfig.class)
public class CoursesControllerTests extends ControllerTestCase {

    @MockBean
    CoursesRepository coursesRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/courses/admin/all

    @Test
    public void api_courses_admin_all__logged_out__returns_403() throws Exception {
        mockMvc.perform(get("/api/courses/admin/all"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses_admin_all__user_logged_in__returns_403() throws Exception {
        mockMvc.perform(get("/api/courses/admin/all"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses_admin__user_logged_in__returns_403() throws Exception {
        mockMvc.perform(get("/api/courses/admin?id=7"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "ADMIN" })
    @Test
    public void api_courses_admin_all__admin_logged_in__returns_200() throws Exception {
        mockMvc.perform(get("/api/courses/admin/all"))
                .andExpect(status().isOk());
    }

    // Authorization tests for /api/courses/all

    @Test
    public void api_courses_all__logged_out__returns_403() throws Exception {
        mockMvc.perform(get("/api/courses/all"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses_all__user_logged_in__returns_200() throws Exception {
        mockMvc.perform(get("/api/courses/user/all"))
                .andExpect(status().isOk());
    }

    // Authorization tests for /api/courses/post

    @Test
    public void api_courses_post__logged_out__returns_403() throws Exception {
        mockMvc.perform(post("/api/courses/post"))
                .andExpect(status().is(403));
    }

    // Tests with mocks for database actions

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__returns_a_courses_that_exists() throws Exception {

        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        Courses course1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(u).id(7L).build();
        when(coursesRepository.findByIdAndUser(eq(7L), eq(u))).thenReturn(Optional.of(course1));

        // act
        MvcResult response = mockMvc.perform(get("/api/courses/user?id=7"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(coursesRepository, times(1)).findByIdAndUser(7L, u);
        String expectedJson = mapper.writeValueAsString(course1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__search_for_courses_that_does_not_exist() throws Exception {

        // arrange

        User u = currentUserService.getCurrentUser().getUser();

        when(coursesRepository.findByIdAndUser(eq(7L), eq(u))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/courses/user?id=7"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(coursesRepository, times(1)).findByIdAndUser(7L, u);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Courses with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__search_for_courses_that_belongs_to_another_user() throws Exception {

        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        User otherUser = User.builder().id(999L).build();
        Courses otherUsersCourses = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(otherUser).id(13L)
                .build();

        when(coursesRepository.findByIdAndUser(eq(13L), eq(otherUser))).thenReturn(Optional.of(otherUsersCourses));

        // act
        MvcResult response = mockMvc.perform(get("/api/courses/user?id=13"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(coursesRepository, times(1)).findByIdAndUser(13L, u);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Courses with id 13 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void api_courses__admin_logged_in__search_for_course_that_belongs_to_another_user() throws Exception {

        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        User otherUser = User.builder().id(999L).build();
        Courses otherUsersCourses = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(otherUser).id(27L)
                .build();

        when(coursesRepository.findById(eq(27L))).thenReturn(Optional.of(otherUsersCourses));

        // act
        MvcResult response = mockMvc.perform(get("/api/courses/admin?id=27"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(coursesRepository, times(1)).findById(27L);
        String expectedJson = mapper.writeValueAsString(otherUsersCourses);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void api_courses__admin_logged_in__search_for_course_that_does_not_exist() throws Exception {

        // arrange

        when(coursesRepository.findById(eq(29L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/courses/admin?id=29"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(coursesRepository, times(1)).findById(29L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Courses with id 29 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void api_courses_admin_all__admin_logged_in__returns_all_courses() throws Exception {

        // arrange

        User u1 = User.builder().id(1L).build();
        User u2 = User.builder().id(2L).build();
        User u = currentUserService.getCurrentUser().getUser();

        Courses p1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(u1).id(1L).build();
        Courses p2 = Courses.builder().enrollCd("08276").psId(13L).quarter("20222").user(u2).id(2L).build();
        Courses p3 = Courses.builder().enrollCd("08078").psId(13L).quarter("20223").user(u).id(3L).build();

        ArrayList<Courses> expectedCourses = new ArrayList<>();
        expectedCourses.addAll(Arrays.asList(p1, p2, p3));

        when(coursesRepository.findAll()).thenReturn(expectedCourses);

        // act
        MvcResult response = mockMvc.perform(get("/api/courses/admin/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(coursesRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedCourses);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses_all__user_logged_in__returns_only_courses_for_user() throws Exception {

        // arrange

        User thisUser = currentUserService.getCurrentUser().getUser();

        Courses p1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(thisUser).id(1L).build();
        Courses p2 = Courses.builder().enrollCd("08276").psId(13L).quarter("20222").user(thisUser).id(2L).build();

        ArrayList<Courses> expectedCourses = new ArrayList<>();
        expectedCourses.addAll(Arrays.asList(p1, p2));
        when(coursesRepository.findAllByUserId(thisUser.getId())).thenReturn(expectedCourses);

        // act
        MvcResult response = mockMvc.perform(get("/api/courses/user/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(coursesRepository, times(1)).findAllByUserId(eq(thisUser.getId()));
        String expectedJson = mapper.writeValueAsString(expectedCourses);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses_post__user_logged_in() throws Exception {
        // arrange

        User thisUser = currentUserService.getCurrentUser().getUser();

        Courses expectedCourses = Courses.builder().enrollCd("08250").psId(13L).quarter("20222").user(thisUser).id(0L).build();

        when(coursesRepository.save(eq(expectedCourses))).thenReturn(expectedCourses);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/courses/post?enrollCd=08250&psId=13&quarter=20222")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(coursesRepository, times(1)).save(expectedCourses);
        String expectedJson = mapper.writeValueAsString(expectedCourses);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__delete_course() throws Exception {
        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        Courses ps1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(u).id(15L).build();
        when(coursesRepository.findByIdAndUser(eq(15L), eq(u))).thenReturn(Optional.of(ps1));

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/courses/user?id=15")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findByIdAndUser(15L, u);
        verify(coursesRepository, times(1)).delete(ps1);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Courses with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__delete_course_that_does_not_exist() throws Exception {
        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        User otherUser = User.builder().id(98L).build();
        Courses ps1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(u).id(15L).build();
        when(coursesRepository.findByIdAndUser(eq(15L), eq(otherUser))).thenReturn(Optional.of(ps1));

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/courses/user?id=15")
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findByIdAndUser(15L, u);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Courses with id 15 not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__cannot_delete_delete_belonging_to_another_user() throws Exception {
        // arrange
        User u = currentUserService.getCurrentUser().getUser();
        User otherUser = User.builder().id(98L).build();
        Courses ps1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(otherUser).id(31L).build();
        when(coursesRepository.findById(eq(31L))).thenReturn(Optional.of(ps1));

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/courses/user?id=31")
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findByIdAndUser(31L, u);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Courses with id 31 not found", json.get("message"));
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void api_courses__admin_logged_in__delete_course() throws Exception {
        // arrange

        User otherUser = User.builder().id(98L).build();
        Courses ps1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(otherUser).id(16L).build();
        when(coursesRepository.findById(eq(16L))).thenReturn(Optional.of(ps1));

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/courses/admin?id=16")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findById(16L);
        verify(coursesRepository, times(1)).delete(ps1);
        Map<String, Object> output = responseToJson(response);
        assertEquals("Courses with id 16 deleted", output.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void api_courses__admin_logged_in__cannot_delete_course_that_does_not_exist() throws Exception {
        // arrange

        when(coursesRepository.findById(eq(17L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/courses/admin?id=17")
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findById(17L);
        Map<String, Object> output = responseToJson(response);
        assertEquals("Courses with id 17 not found", output.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__put_course() throws Exception {
        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        User otherUser = User.builder().id(999).build();
        Courses ps1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(u).id(67L).build();
        // We deliberately set the user information to another user
        // This should get ignored and overwritten with current user when todo is saved

        Courses updatedCourses = Courses.builder().enrollCd("08276").psId(14L).quarter("20222").user(otherUser).id(67L).build();
        Courses correctCourses = Courses.builder().enrollCd("08276").psId(14L).quarter("20222").user(u).id(67L).build();

        String requestBody = mapper.writeValueAsString(updatedCourses);
        String expectedReturn = mapper.writeValueAsString(correctCourses);

        when(coursesRepository.findByIdAndUser(eq(67L), eq(u))).thenReturn(Optional.of(ps1));

        // act
        MvcResult response = mockMvc.perform(
                put("/api/courses/user?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findByIdAndUser(67L, u);
        verify(coursesRepository, times(1)).save(correctCourses); // should be saved with correct user
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedReturn, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__cannot_put_course_that_does_not_exist() throws Exception {
        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        Courses updatedCourses = Courses.builder().enrollCd("08276").psId(14L).quarter("20222").id(67L).build();

        String requestBody = mapper.writeValueAsString(updatedCourses);

        when(coursesRepository.findByIdAndUser(eq(67L), eq(u))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                put("/api/courses/user?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findByIdAndUser(67L, u);
        Map<String, Object> output = responseToJson(response);
        assertEquals("Courses with id 67 not found", output.get("message"));
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses__user_logged_in__cannot_put_course_for_another_user() throws Exception {
        // arrange

        User u = currentUserService.getCurrentUser().getUser();
        User otherUser = User.builder().id(98L).build();
        Courses ps1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(otherUser).id(31L).build();
        Courses updatedCourses = Courses.builder().enrollCd("08276").psId(14L).quarter("20222").id(31L).build();

        when(coursesRepository.findByIdAndUser(eq(31L), eq(otherUser))).thenReturn(Optional.of(ps1));

        String requestBody = mapper.writeValueAsString(updatedCourses);

        // act
        MvcResult response = mockMvc.perform(
                put("/api/courses/user?id=31")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findByIdAndUser(31L, u);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Courses with id 31 not found", json.get("message"));
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void api_schedules__admin_logged_in__put_schedule() throws Exception {
        // arrange

        User otherUser = User.builder().id(255L).build();
        Courses ps1 = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(otherUser).id(77L).build();
        User yetAnotherUser = User.builder().id(512L).build();
        // We deliberately put the wrong user on the updated course
        // We expect the controller to ignore this and keep the user the same
        Courses updatedCourses = Courses.builder().enrollCd("08276").psId(14L).quarter("20222").user(yetAnotherUser).id(77L)
                .build();
        Courses correctCourses = Courses.builder().enrollCd("08276").psId(14L).quarter("20222").user(otherUser).id(77L)
                .build();

        String requestBody = mapper.writeValueAsString(updatedCourses);
        String expectedJson = mapper.writeValueAsString(correctCourses);

        when(coursesRepository.findById(eq(77L))).thenReturn(Optional.of(ps1));

        // act
        MvcResult response = mockMvc.perform(
                put("/api/courses/admin?id=77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findById(77L);
        verify(coursesRepository, times(1)).save(correctCourses);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void api_courses__admin_logged_in__cannot_put_course_that_does_not_exist() throws Exception {
        // arrange

        User otherUser = User.builder().id(345L).build();
        Courses updatedCourses = Courses.builder().enrollCd("08250").psId(13L).quarter("20221").user(otherUser).id(77L)
                .build();

        String requestBody = mapper.writeValueAsString(updatedCourses);

        when(coursesRepository.findById(eq(77L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                put("/api/courses/admin?id=77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(coursesRepository, times(1)).findById(77L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Courses with id 77 not found", json.get("message"));
    }

}