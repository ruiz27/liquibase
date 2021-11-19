package com.santander.cib.orbe.model.web.rest;

import com.santander.cib.orbe.model.PbankMiddelwareStubMicroserviceApp;
import com.santander.cib.orbe.model.domain.ComplianceStatus;
import com.santander.cib.orbe.model.repository.ComplianceStatusRepository;
import com.santander.cib.orbe.model.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.santander.cib.orbe.model.web.rest.TestUtil.sameInstant;
import static com.santander.cib.orbe.model.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ComplianceStatusResource} REST controller.
 */
@SpringBootTest(classes = PbankMiddelwareStubMicroserviceApp.class)
public class ComplianceStatusResourceIT {

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final ZonedDateTime DEFAULT_LAST_MODIFIED_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LAST_MODIFIED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_CREATED_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private ComplianceStatusRepository complianceStatusRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restComplianceStatusMockMvc;

    private ComplianceStatus complianceStatus;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ComplianceStatusResource complianceStatusResource = new ComplianceStatusResource(complianceStatusRepository);
        this.restComplianceStatusMockMvc = MockMvcBuilders.standaloneSetup(complianceStatusResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ComplianceStatus createEntity(EntityManager em) {
        ComplianceStatus complianceStatus = new ComplianceStatus()
            .active(DEFAULT_ACTIVE)
            .lastModifiedDate(DEFAULT_LAST_MODIFIED_DATE)
            .createdDate(DEFAULT_CREATED_DATE);
        return complianceStatus;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ComplianceStatus createUpdatedEntity(EntityManager em) {
        ComplianceStatus complianceStatus = new ComplianceStatus()
            .active(UPDATED_ACTIVE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE)
            .createdDate(UPDATED_CREATED_DATE);
        return complianceStatus;
    }

    @BeforeEach
    public void initTest() {
        complianceStatus = createEntity(em);
    }

    @Test
    @Transactional
    public void createComplianceStatus() throws Exception {
        int databaseSizeBeforeCreate = complianceStatusRepository.findAll().size();

        // Create the ComplianceStatus
        restComplianceStatusMockMvc.perform(post("/api/compliance-statuses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(complianceStatus)))
            .andExpect(status().isCreated());

        // Validate the ComplianceStatus in the database
        List<ComplianceStatus> complianceStatusList = complianceStatusRepository.findAll();
        assertThat(complianceStatusList).hasSize(databaseSizeBeforeCreate + 1);
        ComplianceStatus testComplianceStatus = complianceStatusList.get(complianceStatusList.size() - 1);
        assertThat(testComplianceStatus.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testComplianceStatus.getLastModifiedDate()).isEqualTo(DEFAULT_LAST_MODIFIED_DATE);
        assertThat(testComplianceStatus.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
    }

    @Test
    @Transactional
    public void createComplianceStatusWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = complianceStatusRepository.findAll().size();

        // Create the ComplianceStatus with an existing ID
        complianceStatus.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restComplianceStatusMockMvc.perform(post("/api/compliance-statuses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(complianceStatus)))
            .andExpect(status().isBadRequest());

        // Validate the ComplianceStatus in the database
        List<ComplianceStatus> complianceStatusList = complianceStatusRepository.findAll();
        assertThat(complianceStatusList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllComplianceStatuses() throws Exception {
        // Initialize the database
        complianceStatusRepository.saveAndFlush(complianceStatus);

        // Get all the complianceStatusList
        restComplianceStatusMockMvc.perform(get("/api/compliance-statuses?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(complianceStatus.getId().intValue())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(sameInstant(DEFAULT_LAST_MODIFIED_DATE))))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(sameInstant(DEFAULT_CREATED_DATE))));
    }
    
    @Test
    @Transactional
    public void getComplianceStatus() throws Exception {
        // Initialize the database
        complianceStatusRepository.saveAndFlush(complianceStatus);

        // Get the complianceStatus
        restComplianceStatusMockMvc.perform(get("/api/compliance-statuses/{id}", complianceStatus.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(complianceStatus.getId().intValue()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.lastModifiedDate").value(sameInstant(DEFAULT_LAST_MODIFIED_DATE)))
            .andExpect(jsonPath("$.createdDate").value(sameInstant(DEFAULT_CREATED_DATE)));
    }

    @Test
    @Transactional
    public void getNonExistingComplianceStatus() throws Exception {
        // Get the complianceStatus
        restComplianceStatusMockMvc.perform(get("/api/compliance-statuses/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateComplianceStatus() throws Exception {
        // Initialize the database
        complianceStatusRepository.saveAndFlush(complianceStatus);

        int databaseSizeBeforeUpdate = complianceStatusRepository.findAll().size();

        // Update the complianceStatus
        ComplianceStatus updatedComplianceStatus = complianceStatusRepository.findById(complianceStatus.getId()).get();
        // Disconnect from session so that the updates on updatedComplianceStatus are not directly saved in db
        em.detach(updatedComplianceStatus);
        updatedComplianceStatus
            .active(UPDATED_ACTIVE)
            .lastModifiedDate(UPDATED_LAST_MODIFIED_DATE)
            .createdDate(UPDATED_CREATED_DATE);

        restComplianceStatusMockMvc.perform(put("/api/compliance-statuses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedComplianceStatus)))
            .andExpect(status().isOk());

        // Validate the ComplianceStatus in the database
        List<ComplianceStatus> complianceStatusList = complianceStatusRepository.findAll();
        assertThat(complianceStatusList).hasSize(databaseSizeBeforeUpdate);
        ComplianceStatus testComplianceStatus = complianceStatusList.get(complianceStatusList.size() - 1);
        assertThat(testComplianceStatus.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testComplianceStatus.getLastModifiedDate()).isEqualTo(UPDATED_LAST_MODIFIED_DATE);
        assertThat(testComplianceStatus.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingComplianceStatus() throws Exception {
        int databaseSizeBeforeUpdate = complianceStatusRepository.findAll().size();

        // Create the ComplianceStatus

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restComplianceStatusMockMvc.perform(put("/api/compliance-statuses")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(complianceStatus)))
            .andExpect(status().isBadRequest());

        // Validate the ComplianceStatus in the database
        List<ComplianceStatus> complianceStatusList = complianceStatusRepository.findAll();
        assertThat(complianceStatusList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteComplianceStatus() throws Exception {
        // Initialize the database
        complianceStatusRepository.saveAndFlush(complianceStatus);

        int databaseSizeBeforeDelete = complianceStatusRepository.findAll().size();

        // Delete the complianceStatus
        restComplianceStatusMockMvc.perform(delete("/api/compliance-statuses/{id}", complianceStatus.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ComplianceStatus> complianceStatusList = complianceStatusRepository.findAll();
        assertThat(complianceStatusList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ComplianceStatus.class);
        ComplianceStatus complianceStatus1 = new ComplianceStatus();
        complianceStatus1.setId(1L);
        ComplianceStatus complianceStatus2 = new ComplianceStatus();
        complianceStatus2.setId(complianceStatus1.getId());
        assertThat(complianceStatus1).isEqualTo(complianceStatus2);
        complianceStatus2.setId(2L);
        assertThat(complianceStatus1).isNotEqualTo(complianceStatus2);
        complianceStatus1.setId(null);
        assertThat(complianceStatus1).isNotEqualTo(complianceStatus2);
    }
}
