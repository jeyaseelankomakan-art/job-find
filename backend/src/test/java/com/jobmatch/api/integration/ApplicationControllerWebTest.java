package com.jobmatch.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobmatch.api.controller.ApplicationController;
import com.jobmatch.api.model.dto.ApplicationRequest;
import com.jobmatch.api.model.entity.Application;
import com.jobmatch.api.model.entity.Job;
import com.jobmatch.api.service.ApplicationService;
import com.jobmatch.api.service.CompanyService;
import com.jobmatch.api.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private JobService jobService;

    @MockBean
    private CompanyService companyService;

    @Test
    @WithMockUser(username = "7", roles = "JOB_SEEKER")
    void submitApplicationReturnsCreated() throws Exception {
        ApplicationRequest request = ApplicationRequest.builder().jobId(33L).coverLetter("Ready to contribute").build();
        Application created = Application.builder().id(900L).jobId(33L).userId(7L).status(Application.ApplicationStatus.PENDING).build();

        when(applicationService.submitApplication(eq(7L), any(ApplicationRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(900));

        verify(applicationService).submitApplication(eq(7L), any(ApplicationRequest.class));
    }

    @Test
    @WithMockUser(username = "2", roles = "JOB_SEEKER")
    void withdrawApplicationReturnsForbiddenWhenNotOwner() throws Exception {
        Application existing = Application.builder().id(44L).userId(99L).jobId(10L).status(Application.ApplicationStatus.PENDING).build();
        when(applicationService.getApplicationById(44L)).thenReturn(existing);

        mockMvc.perform(post("/api/v1/applications/44/withdraw"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(applicationService).getApplicationById(44L);
    }

    @Test
    @WithMockUser(username = "5", roles = "COMPANY_ADMIN")
    void acceptApplicationReturnsForbiddenWhenNotCompanyAdminForJob() throws Exception {
        Application app = Application.builder().id(77L).userId(8L).jobId(12L).status(Application.ApplicationStatus.PENDING).build();
        Job job = Job.builder().id(12L).companyId(300L).build();

        when(applicationService.getApplicationById(77L)).thenReturn(app);
        when(jobService.getJobById(12L)).thenReturn(job);
        when(companyService.isUserAdminOfCompany(5L, 300L)).thenReturn(false);

        mockMvc.perform(post("/api/v1/applications/77/accept"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(applicationService).getApplicationById(77L);
        verify(jobService).getJobById(12L);
        verify(companyService).isUserAdminOfCompany(5L, 300L);
    }
}
