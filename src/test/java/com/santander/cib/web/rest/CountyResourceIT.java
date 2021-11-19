package com.santander.cib.orbe.model.web.rest;

import com.santander.cib.orbe.model.PbankMiddelwareStubMicroserviceApp;
import com.santander.cib.orbe.model.domain.County;
import com.santander.cib.orbe.model.repository.CountyRepository;
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
 * Integration tests for the {@link CountyResource} REST controller.
 */
@SpringBootTest(classes = PbankMiddelwareStubMicroserviceApp.class)
public class CountyResourceIT {

    private static final String DEFAULT_NUMERIC_CODE = "AAAAAAAAAA";
    private static final String UPDATED_NUMERIC_CODE = "BBBBBBBBBB";

    private static final String DEFAULT_ALPHA_2_CODE = "AAAAAAAAAA";
    private static final String UPDATED_ALPHA_2_CODE = "BBBBBBBBBB";

    @Autowired
    private CountyRepository countyRepository;

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

    private MockMvc restCountyMockMvc;

    private County county;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CountyResource countyResource = new CountyResource(countyRepository);
        this.restCountyMockMvc = MockMvcBuilders.standaloneSetup(countyResource)
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
    public static County createEntity(EntityManager em) {
        County county = new County()
            .numericCode(DEFAULT_NUMERIC_CODE)
            .alpha2Code(DEFAULT_ALPHA_2_CODE);
        return county;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static County createUpdatedEntity(EntityManager em) {
        County county = new County()
            .numericCode(UPDATED_NUMERIC_CODE)
            .alpha2Code(UPDATED_ALPHA_2_CODE);
        return county;
    }

    @BeforeEach
    public void initTest() {
        county = createEntity(em);
    }

    @Test
    @Transactional
    public void createCounty() throws Exception {
        int databaseSizeBeforeCreate = countyRepository.findAll().size();

        // Create the County
        restCountyMockMvc.perform(post("/api/counties")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(county)))
            .andExpect(status().isCreated());

        // Validate the County in the database
        List<County> countyList = countyRepository.findAll();
        assertThat(countyList).hasSize(databaseSizeBeforeCreate + 1);
        County testCounty = countyList.get(countyList.size() - 1);
        assertThat(testCounty.getNumericCode()).isEqualTo(DEFAULT_NUMERIC_CODE);
        assertThat(testCounty.getAlpha2Code()).isEqualTo(DEFAULT_ALPHA_2_CODE);
    }

    @Test
    @Transactional
    public void createCountyWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = countyRepository.findAll().size();

        // Create the County with an existing ID
        county.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCountyMockMvc.perform(post("/api/counties")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(county)))
            .andExpect(status().isBadRequest());

        // Validate the County in the database
        List<County> countyList = countyRepository.findAll();
        assertThat(countyList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCounties() throws Exception {
        // Initialize the database
        countyRepository.saveAndFlush(county);

        // Get all the countyList
        restCountyMockMvc.perform(get("/api/counties?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(county.getId().intValue())))
            .andExpect(jsonPath("$.[*].numericCode").value(hasItem(DEFAULT_NUMERIC_CODE)))
            .andExpect(jsonPath("$.[*].alpha2Code").value(hasItem(DEFAULT_ALPHA_2_CODE)));
    }
    
    @Test
    @Transactional
    public void getCounty() throws Exception {
        // Initialize the database
        countyRepository.saveAndFlush(county);

        // Get the county
        restCountyMockMvc.perform(get("/api/counties/{id}", county.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(county.getId().intValue()))
            .andExpect(jsonPath("$.numericCode").value(DEFAULT_NUMERIC_CODE))
            .andExpect(jsonPath("$.alpha2Code").value(DEFAULT_ALPHA_2_CODE));
    }

    @Test
    @Transactional
    public void getNonExistingCounty() throws Exception {
        // Get the county
        restCountyMockMvc.perform(get("/api/counties/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCounty() throws Exception {
        // Initialize the database
        countyRepository.saveAndFlush(county);

        int databaseSizeBeforeUpdate = countyRepository.findAll().size();

        // Update the county
        County updatedCounty = countyRepository.findById(county.getId()).get();
        // Disconnect from session so that the updates on updatedCounty are not directly saved in db
        em.detach(updatedCounty);
        updatedCounty
            .numericCode(UPDATED_NUMERIC_CODE)
            .alpha2Code(UPDATED_ALPHA_2_CODE);

        restCountyMockMvc.perform(put("/api/counties")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCounty)))
            .andExpect(status().isOk());

        // Validate the County in the database
        List<County> countyList = countyRepository.findAll();
        assertThat(countyList).hasSize(databaseSizeBeforeUpdate);
        County testCounty = countyList.get(countyList.size() - 1);
        assertThat(testCounty.getNumericCode()).isEqualTo(UPDATED_NUMERIC_CODE);
        assertThat(testCounty.getAlpha2Code()).isEqualTo(UPDATED_ALPHA_2_CODE);
    }

    @Test
    @Transactional
    public void updateNonExistingCounty() throws Exception {
        int databaseSizeBeforeUpdate = countyRepository.findAll().size();

        // Create the County

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCountyMockMvc.perform(put("/api/counties")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(county)))
            .andExpect(status().isBadRequest());

        // Validate the County in the database
        List<County> countyList = countyRepository.findAll();
        assertThat(countyList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCounty() throws Exception {
        // Initialize the database
        countyRepository.saveAndFlush(county);

        int databaseSizeBeforeDelete = countyRepository.findAll().size();

        // Delete the county
        restCountyMockMvc.perform(delete("/api/counties/{id}", county.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<County> countyList = countyRepository.findAll();
        assertThat(countyList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(County.class);
        County county1 = new County();
        county1.setId(1L);
        County county2 = new County();
        county2.setId(county1.getId());
        assertThat(county1).isEqualTo(county2);
        county2.setId(2L);
        assertThat(county1).isNotEqualTo(county2);
        county1.setId(null);
        assertThat(county1).isNotEqualTo(county2);
    }
}
