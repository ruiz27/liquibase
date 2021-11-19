package com.santander.cib.orbe.model.web.rest;

import com.santander.cib.orbe.model.PbankMiddelwareStubMicroserviceApp;
import com.santander.cib.orbe.model.domain.ComplianceStatusTranslate;
import com.santander.cib.orbe.model.repository.ComplianceStatusTranslateRepository;
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
import java.util.List;

import static com.santander.cib.orbe.model.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ComplianceStatusTranslateResource} REST controller.
 */
@SpringBootTest(classes = PbankMiddelwareStubMicroserviceApp.class)
public class ComplianceStatusTranslateResourceIT {

    @Autowired
    private ComplianceStatusTranslateRepository complianceStatusTranslateRepository;

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

    private MockMvc restComplianceStatusTranslateMockMvc;

    private ComplianceStatusTranslate complianceStatusTranslate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ComplianceStatusTranslateResource complianceStatusTranslateResource = new ComplianceStatusTranslateResource(complianceStatusTranslateRepository);
        this.restComplianceStatusTranslateMockMvc = MockMvcBuilders.standaloneSetup(complianceStatusTranslateResource)
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
    public static ComplianceStatusTranslate createEntity(EntityManager em) {
        ComplianceStatusTranslate complianceStatusTranslate = new ComplianceStatusTranslate();
        return complianceStatusTranslate;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ComplianceStatusTranslate createUpdatedEntity(EntityManager em) {
        ComplianceStatusTranslate complianceStatusTranslate = new ComplianceStatusTranslate();
        return complianceStatusTranslate;
    }

    @BeforeEach
    public void initTest() {
        complianceStatusTranslate = createEntity(em);
    }

    @Test
    @Transactional
    public void createComplianceStatusTranslate() throws Exception {
        int databaseSizeBeforeCreate = complianceStatusTranslateRepository.findAll().size();

        // Create the ComplianceStatusTranslate
        restComplianceStatusTranslateMockMvc.perform(post("/api/compliance-status-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(complianceStatusTranslate)))
            .andExpect(status().isCreated());

        // Validate the ComplianceStatusTranslate in the database
        List<ComplianceStatusTranslate> complianceStatusTranslateList = complianceStatusTranslateRepository.findAll();
        assertThat(complianceStatusTranslateList).hasSize(databaseSizeBeforeCreate + 1);
        ComplianceStatusTranslate testComplianceStatusTranslate = complianceStatusTranslateList.get(complianceStatusTranslateList.size() - 1);
    }

    @Test
    @Transactional
    public void createComplianceStatusTranslateWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = complianceStatusTranslateRepository.findAll().size();

        // Create the ComplianceStatusTranslate with an existing ID
        complianceStatusTranslate.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restComplianceStatusTranslateMockMvc.perform(post("/api/compliance-status-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(complianceStatusTranslate)))
            .andExpect(status().isBadRequest());

        // Validate the ComplianceStatusTranslate in the database
        List<ComplianceStatusTranslate> complianceStatusTranslateList = complianceStatusTranslateRepository.findAll();
        assertThat(complianceStatusTranslateList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllComplianceStatusTranslates() throws Exception {
        // Initialize the database
        complianceStatusTranslateRepository.saveAndFlush(complianceStatusTranslate);

        // Get all the complianceStatusTranslateList
        restComplianceStatusTranslateMockMvc.perform(get("/api/compliance-status-translates?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(complianceStatusTranslate.getId().intValue())));
    }
    
    @Test
    @Transactional
    public void getComplianceStatusTranslate() throws Exception {
        // Initialize the database
        complianceStatusTranslateRepository.saveAndFlush(complianceStatusTranslate);

        // Get the complianceStatusTranslate
        restComplianceStatusTranslateMockMvc.perform(get("/api/compliance-status-translates/{id}", complianceStatusTranslate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(complianceStatusTranslate.getId().intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingComplianceStatusTranslate() throws Exception {
        // Get the complianceStatusTranslate
        restComplianceStatusTranslateMockMvc.perform(get("/api/compliance-status-translates/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateComplianceStatusTranslate() throws Exception {
        // Initialize the database
        complianceStatusTranslateRepository.saveAndFlush(complianceStatusTranslate);

        int databaseSizeBeforeUpdate = complianceStatusTranslateRepository.findAll().size();

        // Update the complianceStatusTranslate
        ComplianceStatusTranslate updatedComplianceStatusTranslate = complianceStatusTranslateRepository.findById(complianceStatusTranslate.getId()).get();
        // Disconnect from session so that the updates on updatedComplianceStatusTranslate are not directly saved in db
        em.detach(updatedComplianceStatusTranslate);

        restComplianceStatusTranslateMockMvc.perform(put("/api/compliance-status-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedComplianceStatusTranslate)))
            .andExpect(status().isOk());

        // Validate the ComplianceStatusTranslate in the database
        List<ComplianceStatusTranslate> complianceStatusTranslateList = complianceStatusTranslateRepository.findAll();
        assertThat(complianceStatusTranslateList).hasSize(databaseSizeBeforeUpdate);
        ComplianceStatusTranslate testComplianceStatusTranslate = complianceStatusTranslateList.get(complianceStatusTranslateList.size() - 1);
    }

    @Test
    @Transactional
    public void updateNonExistingComplianceStatusTranslate() throws Exception {
        int databaseSizeBeforeUpdate = complianceStatusTranslateRepository.findAll().size();

        // Create the ComplianceStatusTranslate

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restComplianceStatusTranslateMockMvc.perform(put("/api/compliance-status-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(complianceStatusTranslate)))
            .andExpect(status().isBadRequest());

        // Validate the ComplianceStatusTranslate in the database
        List<ComplianceStatusTranslate> complianceStatusTranslateList = complianceStatusTranslateRepository.findAll();
        assertThat(complianceStatusTranslateList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteComplianceStatusTranslate() throws Exception {
        // Initialize the database
        complianceStatusTranslateRepository.saveAndFlush(complianceStatusTranslate);

        int databaseSizeBeforeDelete = complianceStatusTranslateRepository.findAll().size();

        // Delete the complianceStatusTranslate
        restComplianceStatusTranslateMockMvc.perform(delete("/api/compliance-status-translates/{id}", complianceStatusTranslate.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<ComplianceStatusTranslate> complianceStatusTranslateList = complianceStatusTranslateRepository.findAll();
        assertThat(complianceStatusTranslateList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ComplianceStatusTranslate.class);
        ComplianceStatusTranslate complianceStatusTranslate1 = new ComplianceStatusTranslate();
        complianceStatusTranslate1.setId(1L);
        ComplianceStatusTranslate complianceStatusTranslate2 = new ComplianceStatusTranslate();
        complianceStatusTranslate2.setId(complianceStatusTranslate1.getId());
        assertThat(complianceStatusTranslate1).isEqualTo(complianceStatusTranslate2);
        complianceStatusTranslate2.setId(2L);
        assertThat(complianceStatusTranslate1).isNotEqualTo(complianceStatusTranslate2);
        complianceStatusTranslate1.setId(null);
        assertThat(complianceStatusTranslate1).isNotEqualTo(complianceStatusTranslate2);
    }
}
