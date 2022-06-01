package edu.ucsb.cs156.courses.controllers;
import edu.ucsb.cs156.courses.repositories.UserRepository;
import edu.ucsb.cs156.courses.testconfig.TestConfig;
import edu.ucsb.cs156.courses.ControllerTestCase;
import edu.ucsb.cs156.courses.entities.PersonalCourses;
import edu.ucsb.cs156.courses.entities.User;
import edu.ucsb.cs156.courses.repositories.PersonalCoursesRepository;
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

@WebMvcTest(controllers = PersonalCoursesController.class)
@Import(TestConfig.class)
public class PersonalCoursesControllerTests extends ControllerTestCase {

    @MockBean
    PersonalCoursesRepository personalcoursesRepository;

    @MockBean
    UserRepository userRepository;
    @Test
    public void api_schedules_all__logged_out__returns_403() throws Exception {
        mockMvc.perform(get("/api/personalcourses/all"))
                .andExpect(status().is(403));
    }
    
    @WithMockUser(roles = { "USER" })
    @Test
    public void api_schedules_all__user_logged_in__returns_200() throws Exception {
        mockMvc.perform(get("/api/personalcourses/all"))
                .andExpect(status().isOk());
    }

    
    @Test
    public void api_schedules_post__logged_out__returns_403() throws Exception {
        mockMvc.perform(post("/api/personalcourses/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_schedules_admin_all__admin_logged_in__returns_all_schedules() throws Exception {



        PersonalCourses p1 = PersonalCourses.builder().psId(1).enrollCd("111111").quarter("20223").id(0L).build();
        PersonalCourses p2 = PersonalCourses.builder().psId(2).enrollCd("233233").quarter("20231").id(1L).build();
        PersonalCourses p3 = PersonalCourses.builder().psId(3).enrollCd("666666").quarter("20232").id(2L).build();
       

        ArrayList<PersonalCourses> expectedCourses = new ArrayList<>();
        expectedCourses.addAll(Arrays.asList(p1, p2, p3));

        when(personalcoursesRepository.findAll()).thenReturn(expectedCourses);

        
        MvcResult response = mockMvc.perform(get("/api/personalcourses/all"))
                .andExpect(status().isOk()).andReturn();

        

        verify(personalcoursesRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedCourses);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
    
    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses_get_enrollcd__user_logged_in() throws Exception {
        
        PersonalCourses p1 = PersonalCourses.builder().psId(1).enrollCd("111111").quarter("20223").id(0L).build();
        ArrayList<PersonalCourses> expectedCourses = new ArrayList<>();
        expectedCourses.add(p1);

        ArrayList<String> expectedEnrollCds = new ArrayList<String>();
        expectedEnrollCds.add("111111");

        when(personalcoursesRepository.findAllByPsId((long) 1)).thenReturn(expectedCourses);

        MvcResult response = mockMvc.perform(
                get("/api/personalcourses/all/enrollCd?psId=1")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        
        verify(personalcoursesRepository, times(1)).findAllByPsId((long) 1);
        String expectedJson = mapper.writeValueAsString(expectedEnrollCds);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_courses_add__user_logged_in() throws Exception {
        
        PersonalCourses expectedCourse = PersonalCourses.builder().psId(1).enrollCd("111111").quarter("20223").id(0L).build();

        when(personalcoursesRepository.save(eq(expectedCourse))).thenReturn(expectedCourse);

        
        MvcResult response = mockMvc.perform(
                post("/api/personalcourses/add?psId=1&enrollCd=111111&quarter=20223")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        
        verify(personalcoursesRepository, times(1)).save(expectedCourse);
        String expectedJson = mapper.writeValueAsString(expectedCourse);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
}
