package edu.ucsb.cs156.courses.controllers;

import edu.ucsb.cs156.courses.config.SecurityConfig;
import edu.ucsb.cs156.courses.services.UCSBCurriculumService;
import edu.ucsb.cs156.courses.entities.Courses;
import edu.ucsb.cs156.courses.repositories.UserRepository;
import edu.ucsb.cs156.courses.repositories.CoursesRepository;
import edu.ucsb.cs156.courses.documents.ConvertedSection;
import edu.ucsb.cs156.courses.documents.CoursePage;
import edu.ucsb.cs156.courses.documents.CourseInfo;
import edu.ucsb.cs156.courses.documents.CoursePageFixtures;
import edu.ucsb.cs156.courses.ControllerTestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@WebMvcTest(value = SectionController.class)
@Import(SecurityConfig.class)
public class SectionControllerTests extends ControllerTestCase {
    private final Logger logger = LoggerFactory.getLogger(SectionControllerTests.class);
    private ObjectMapper mapper = new ObjectMapper();

    @MockBean
    CoursesRepository coursesRepository;

     @MockBean
    UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UCSBCurriculumService ucsbCurriculumService;

    @WithMockUser(roles = { "USER" })
    @Test
    public void api_sections_admin__user_logged_in__returns_200() throws Exception {
        mockMvc.perform(get("/api/sections/getsections?psId=7"))
                .andExpect(status().is(200));
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void test_section_search() throws Exception {

        CoursePage cp = CoursePage.fromJSON(CoursePageFixtures.COURSE_PAGE_JSON_MATH3B);
        List<CourseInfo> convertedSections = cp.convertedSectionsInfo();
        List<CourseInfo> empty = new ArrayList<CourseInfo>();
        assertNotEquals(empty, convertedSections);
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedResult = objectMapper.writeValueAsString(convertedSections);

        String urlTemplate = "/api/sections/getsections?psId=%s";
        String url = String.format(urlTemplate, "7");
        when(ucsbCurriculumService.getConvertedSectionsByQuarterAndEnroll(any(String.class), any(String.class)))
	    .thenReturn(convertedSections);


        Courses p7 = Courses.builder().psId(7).enrollCd("77777").quarter("20223").id(0L).build();
        ArrayList<Courses> expectedCourses = new ArrayList<>();
        expectedCourses.addAll(Arrays.asList(p7));
        when(coursesRepository.findAllByPsId(any(Long.class))).thenReturn(expectedCourses);


        MvcResult response = mockMvc.perform(get(url).contentType("application/json")).andExpect(status().isOk())
                .andReturn();
        String responseString = response.getResponse().getContentAsString();

        assertEquals(expectedResult, responseString);
    }
}
