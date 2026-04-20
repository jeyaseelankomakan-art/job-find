package com.jobmatch.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.api.controller.JobController;
import com.jobmatch.api.model.dto.JobRequest;
import com.jobmatch.api.model.entity.Job;
import com.jobmatch.api.service.CompanyService;
import com.jobmatch.api.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobController.class)
@AutoConfigureMockMvc(addFilters = false)
class JobControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @MockBean
    private CompanyService companyService;

    @Test
    void getAllJobsReturnsPagedResponse() throws Exception {
        Job job = Job.builder().id(101L).title("Backend Engineer").build();
        when(jobService.getAllPublishedJobs(0, 20)).thenReturn(new PageImpl<>(List.of(job)));

        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jobs retrieved"))
                .andExpect(jsonPath("$.data.content[0].id").value(101));

        verify(jobService).getAllPublishedJobs(0, 20);
    }

    @Test
    @WithMockUser(username = "7", roles = "COMPANY_ADMIN")
    void createJobReturnsCreatedWhenUserIsCompanyAdmin() throws Exception {
        JobRequest request = JobRequest.builder()
                .title("Senior Java Engineer")
                .description("Role description")
                .jobType("FULL_TIME")
                .location("Remote")
                .remote(true)
                .salaryMin(100000L)
                .salaryMax(150000L)
                .experienceLevel("SENIOR")
                .build();

        Job created = Job.builder().id(202L).companyId(11L).title("Senior Java Engineer").build();

        when(companyService.isUserAdminOfCompany(7L, 11L)).thenReturn(true);
        when(jobService.createJob(eq(11L), any(JobRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/jobs/company/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(202));

        verify(companyService).isUserAdminOfCompany(7L, 11L);
        verify(jobService).createJob(eq(11L), any(JobRequest.class));
    }

    @Test
    @WithMockUser(username = "9", roles = "COMPANY_ADMIN")
    void createJobReturnsForbiddenWhenUserIsNotCompanyAdmin() throws Exception {
        JobRequest request = JobRequest.builder()
                .title("Java Engineer")
                .description("Role description")
                .jobType("FULL_TIME")
                .location("NY")
                .remote(false)
                .salaryMin(90000L)
                .salaryMax(120000L)
                .experienceLevel("MID")
                .build();

        when(companyService.isUserAdminOfCompany(9L, 11L)).thenReturn(false);

        mockMvc.perform(post("/api/v1/jobs/company/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(companyService).isUserAdminOfCompany(9L, 11L);
    }
}
