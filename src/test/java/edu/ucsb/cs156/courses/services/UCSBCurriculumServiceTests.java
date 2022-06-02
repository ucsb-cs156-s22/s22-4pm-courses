package edu.ucsb.cs156.courses.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import edu.ucsb.cs156.courses.documents.ConvertedSection;
import edu.ucsb.cs156.courses.documents.CourseInfo;
import edu.ucsb.cs156.courses.documents.CoursePageFixtures;

@RestClientTest(UCSBCurriculumService.class)
public class UCSBCurriculumServiceTests {

    @Value("${app.ucsb.api.consumer_key}")
    private String apiKey;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Mock
    private RestTemplate restTemplate;

    @Autowired
    private UCSBCurriculumService ucs;

    @Test
    public void test_getJSON_success() throws Exception {
        String expectedResult = "{expectedResult}";

        String subjectArea = "CMPSC";
        String quarter = "20221";
        String level = "L";

        String expectedParams = String.format(
                "?quarter=%s&subjectCode=%s&objLevelCode=%s&pageNumber=%d&pageSize=%d&includeClassSections=%s", quarter,
                subjectArea, level, 1, 100, "true");
        String expectedURL = UCSBCurriculumService.CURRICULUM_ENDPOINT + expectedParams;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        String result = ucs.getJSON(subjectArea, quarter, level);

        assertEquals(expectedResult, result);
    }

    @Test
    public void test_getJSONbyQuarterAndEnroll_success() throws Exception {
        String expectedResult = "{expectedResult}";

        String quarter = "20223";
        String enrollCd = "77777";

        String expectedParams = String.format(
                "?quarter=%s&enrollCode=%s&pageNumber=%d&pageSize=%d&includeClassSections=true", quarter,
                enrollCd, 1, 100);
        String expectedURL = "https://api.ucsb.edu/academics/curriculums/v3/classes/search" + expectedParams;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "3.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        String result = ucs.getJSONbyQuarterAndEnroll(quarter, enrollCd);

        assertEquals(expectedResult, result);
    }

    @Test
    public void test_getJSON_success_level_A() throws Exception {
        String expectedResult = "{expectedResult}";

        String subjectArea = "CMPSC";
        String quarter = "20223";

        String level = "A";

        String expectedParams = String.format(
                "?quarter=%s&subjectCode=%s&pageNumber=%d&pageSize=%d&includeClassSections=%s",
                quarter, subjectArea, 1, 100, "true");
        String expectedURL = UCSBCurriculumService.CURRICULUM_ENDPOINT + expectedParams;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        String result = ucs.getJSON(subjectArea, quarter, level);

        assertEquals(expectedResult, result);
    }

    @Test
    public void test_getJSON_exception() throws Exception {
        String expectedResult = "{\"error\": \"401: Unauthorized\"}";

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.class);

        String subjectArea = "CMPSC";
        String quarter = "20223";
        String level = "L";

        String expectedParams = String.format(
                "?quarter=%s&subjectCode=%s&objLevelCode=%s&pageNumber=%d&pageSize=%d&includeClassSections=%s", quarter,
                subjectArea, level, 1, 100, "true");
        String expectedURL = UCSBCurriculumService.CURRICULUM_ENDPOINT + expectedParams;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withUnauthorizedRequest());

        String result = ucs.getJSON(subjectArea, quarter, level);

        assertEquals(expectedResult, result);
    }

        @Test
        public void test_getJSONbyQuarterAndEnroll_exception() throws Exception {
        String expectedResult = "{\"error\": \"401: Unauthorized\"}";

        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.class);

        String quarter = "20223";
        String enrollCd = "77777";

        String expectedParams = String.format(
                "?quarter=%s&enrollCode=%s&pageNumber=%d&pageSize=%d&includeClassSections=true", quarter,
                enrollCd, 1, 100);
        String expectedURL = "https://api.ucsb.edu/academics/curriculums/v3/classes/search" + expectedParams;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "3.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withUnauthorizedRequest());

        String result = ucs.getJSONbyQuarterAndEnroll(quarter, enrollCd);

        assertEquals(expectedResult, result);
    }

    @Test
    public void test_getSubjectsJSON_success() throws Exception {
        String expectedResult = "[ {deptCode: \"ANTH\"} ]";
        String expectedURL = UCSBCurriculumService.SUBJECTS_ENDPOINT;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        String result = ucs.getSubjectsJSON();
        assertEquals(expectedResult, result);
    }

    @Test
    public void test_getSubjectsJSON_exception() throws Exception {
        String expectedResult = "{\"error\": \"401: Unauthorized\"}";
        String expectedURL = UCSBCurriculumService.SUBJECTS_ENDPOINT;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andRespond(withUnauthorizedRequest());

        String result = ucs.getSubjectsJSON();
        assertEquals(expectedResult, result);
    }

    @Test
    public void test_getConvertedSections() throws Exception {
        String expectedResult = CoursePageFixtures.COURSE_PAGE_JSON_MATH3B;

        String subjectArea = "MATH";
        String quarter = "20222";
        String level = "L";

        String expectedParams = String.format(
                "?quarter=%s&subjectCode=%s&objLevelCode=%s&pageNumber=%d&pageSize=%d&includeClassSections=%s", quarter,
                subjectArea, level, 1, 100, "true");
        String expectedURL = UCSBCurriculumService.CURRICULUM_ENDPOINT + expectedParams;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        ObjectMapper objectMapper = new ObjectMapper();
        List<ConvertedSection> convertedSections = ucs.getConvertedSections(subjectArea, quarter, level);
        List<ConvertedSection> expected = objectMapper.readValue(CoursePageFixtures.CONVERTED_SECTIONS_JSON_MATH5B,
                new TypeReference<List<ConvertedSection>>() {
                });

        assertEquals(expected, convertedSections);
    }


     @Test
     public void test_getConvertedSectionsByQuarterAndEnroll() throws Exception {
        String expectedResult = CoursePageFixtures.CS777original;

        String quarter = "20222";
        String enrollCd = "77777";

         String expectedParams = String.format(
                "?quarter=%s&enrollCode=%s&pageNumber=%d&pageSize=%d&includeClassSections=true", quarter,
                enrollCd, 1, 100);
        String expectedURL = "https://api.ucsb.edu/academics/curriculums/v3/classes/search" + expectedParams;
        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "3.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        ObjectMapper objectMapper = new ObjectMapper();

        List<CourseInfo> convertedSections = ucs.getConvertedSectionsByQuarterAndEnroll(quarter, enrollCd);
        List<CourseInfo> expected = objectMapper.readValue(CoursePageFixtures.CS777,
                new TypeReference<List<CourseInfo>>() {
                });

        assertEquals(expected, convertedSections);
    }


    @Test
    public void test_getSectionJSON_success() throws Exception {
        String expectedResult = "[{\"quarter\": \"20223\", \"classSections\": [{\"enrollCode\": \"77777\"}]}]";


        String quarter = "20223";
        String enrollCode = "77777";

        String params = String.format(
            "/%s/%s", quarter, enrollCode);
        String expectedURL = UCSBCurriculumService.ENDPOINT + params;
  
        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess(expectedResult, MediaType.APPLICATION_JSON));

        String result = ucs.getSectionsJSON(quarter, enrollCode);
        
        assertEquals(expectedResult, result);
    }

                

    @Test
    public void test_getSectionJSON_exception() throws Exception {
        String expectedResult = "{\"error\": \"401: Unauthorized\"}";

        String quarter = "20223";
        String enrollCode = "77777";

        String params = String.format(
            "/%s/%s", quarter, enrollCode);
        String expectedURL = UCSBCurriculumService.ENDPOINT + params;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withUnauthorizedRequest());

        String result = ucs.getSectionsJSON(quarter, enrollCode);
        assertEquals(expectedResult, result);
    }

    @Test
    public void test_getSectionJSON_DNE() throws Exception {

        String expectedResult = "{\"error\": \"404 (Not Found): Enroll code does not exist!\"}";
        String quarter = "20223";
        String enrollCode = "00000";

        String params = String.format(
            "/%s/%s", quarter, enrollCode);
        String expectedURL = UCSBCurriculumService.ENDPOINT + params;

        this.mockRestServiceServer.expect(requestTo(expectedURL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON.toString()))
                .andExpect(header("ucsb-api-version", "1.0"))
                .andExpect(header("ucsb-api-key", apiKey))
                .andRespond(withSuccess("null", MediaType.APPLICATION_JSON));

        String result = ucs.getSectionsJSON(quarter, enrollCode);
        assertEquals(expectedResult, result);
    }

}
