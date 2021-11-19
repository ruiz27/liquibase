package com.santander.cib.orbe.model.web.rest;

import com.santander.cib.orbe.model.PbankMiddelwareStubMicroserviceApp;
import com.santander.cib.orbe.model.domain.CountryTranslate;
import com.santander.cib.orbe.model.repository.CountryTranslateRepository;
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
 * Integration tests for the {@link CountryTranslateResource} REST controller.
 */
@SpringBootTest(classes = PbankMiddelwareStubMicroserviceApp.class)
public class CountryTranslateResourceIT {

    private static final String DEFAULT_IP_ADDRESS = "AAAAAAAAAA";
    private static final String UPDATED_IP_ADDRESS = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATION_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATION_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATE_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATE_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private CountryTranslateRepository countryTranslateRepository;

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

    private MockMvc restCountryTranslateMockMvc;

    private CountryTranslate countryTranslate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CountryTranslateResource countryTranslateResource = new CountryTranslateResource(countryTranslateRepository);
        this.restCountryTranslateMockMvc = MockMvcBuilders.standaloneSetup(countryTranslateResource)
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
    public static CountryTranslate createEntity(EntityManager em) {
        CountryTranslate countryTranslate = new CountryTranslate()
            .ipAddress(DEFAULT_IP_ADDRESS)
            .creationDate(DEFAULT_CREATION_DATE)
            .updateDate(DEFAULT_UPDATE_DATE);
        return countryTranslate;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static CountryTranslate createUpdatedEntity(EntityManager em) {
        CountryTranslate countryTranslate = new CountryTranslate()
            .ipAddress(UPDATED_IP_ADDRESS)
            .creationDate(UPDATED_CREATION_DATE)
            .updateDate(UPDATED_UPDATE_DATE);
        return countryTranslate;
    }

    @BeforeEach
    public void initTest() {
        countryTranslate = createEntity(em);
    }

    @Test
    @Transactional
    public void createCountryTranslate() throws Exception {
        int databaseSizeBeforeCreate = countryTranslateRepository.findAll().size();

        // Create the CountryTranslate
        restCountryTranslateMockMvc.perform(post("/api/country-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(countryTranslate)))
            .andExpect(status().isCreated());

        // Validate the CountryTranslate in the database
        List<CountryTranslate> countryTranslateList = countryTranslateRepository.findAll();
        assertThat(countryTranslateList).hasSize(databaseSizeBeforeCreate + 1);
        CountryTranslate testCountryTranslate = countryTranslateList.get(countryTranslateList.size() - 1);
        assertThat(testCountryTranslate.getIpAddress()).isEqualTo(DEFAULT_IP_ADDRESS);
        assertThat(testCountryTranslate.getCreationDate()).isEqualTo(DEFAULT_CREATION_DATE);
        assertThat(testCountryTranslate.getUpdateDate()).isEqualTo(DEFAULT_UPDATE_DATE);
    }

    @Test
    @Transactional
    public void createCountryTranslateWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = countryTranslateRepository.findAll().size();

        // Create the CountryTranslate with an existing ID
        countryTranslate.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCountryTranslateMockMvc.perform(post("/api/country-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(countryTranslate)))
            .andExpect(status().isBadRequest());

        // Validate the CountryTranslate in the database
        List<CountryTranslate> countryTranslateList = countryTranslateRepository.findAll();
        assertThat(countryTranslateList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCountryTranslates() throws Exception {
        // Initialize the database
        countryTranslateRepository.saveAndFlush(countryTranslate);

        // Get all the countryTranslateList
        restCountryTranslateMockMvc.perform(get("/api/country-translates?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(countryTranslate.getId().intValue())))
            .andExpect(jsonPath("$.[*].ipAddress").value(hasItem(DEFAULT_IP_ADDRESS)))
            .andExpect(jsonPath("$.[*].creationDate").value(hasItem(sameInstant(DEFAULT_CREATION_DATE))))
            .andExpect(jsonPath("$.[*].updateDate").value(hasItem(sameInstant(DEFAULT_UPDATE_DATE))));
    }
    
    @Test
    @Transactional
    public void getCountryTranslate() throws Exception {
        // Initialize the database
        countryTranslateRepository.saveAndFlush(countryTranslate);

        // Get the countryTranslate
        restCountryTranslateMockMvc.perform(get("/api/country-translates/{id}", countryTranslate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(countryTranslate.getId().intValue()))
            .andExpect(jsonPath("$.ipAddress").value(DEFAULT_IP_ADDRESS))
            .andExpect(jsonPath("$.creationDate").value(sameInstant(DEFAULT_CREATION_DATE)))
            .andExpect(jsonPath("$.updateDate").value(sameInstant(DEFAULT_UPDATE_DATE)));
    }

    @Test
    @Transactional
    public void getNonExistingCountryTranslate() throws Exception {
        // Get the countryTranslate
        restCountryTranslateMockMvc.perform(get("/api/country-translates/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCountryTranslate() throws Exception {
        // Initialize the database
        countryTranslateRepository.saveAndFlush(countryTranslate);

        int databaseSizeBeforeUpdate = countryTranslateRepository.findAll().size();

        // Update the countryTranslate
        CountryTranslate updatedCountryTranslate = countryTranslateRepository.findById(countryTranslate.getId()).get();
        // Disconnect from session so that the updates on updatedCountryTranslate are not directly saved in db
        em.detach(updatedCountryTranslate);
        updatedCountryTranslate
            .ipAddress(UPDATED_IP_ADDRESS)
            .creationDate(UPDATED_CREATION_DATE)
            .updateDate(UPDATED_UPDATE_DATE);

        restCountryTranslateMockMvc.perform(put("/api/country-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedCountryTranslate)))
            .andExpect(status().isOk());

        // Validate the CountryTranslate in the database
        List<CountryTranslate> countryTranslateList = countryTranslateRepository.findAll();
        assertThat(countryTranslateList).hasSize(databaseSizeBeforeUpdate);
        CountryTranslate testCountryTranslate = countryTranslateList.get(countryTranslateList.size() - 1);
        assertThat(testCountryTranslate.getIpAddress()).isEqualTo(UPDATED_IP_ADDRESS);
        assertThat(testCountryTranslate.getCreationDate()).isEqualTo(UPDATED_CREATION_DATE);
        assertThat(testCountryTranslate.getUpdateDate()).isEqualTo(UPDATED_UPDATE_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingCountryTranslate() throws Exception {
        int databaseSizeBeforeUpdate = countryTranslateRepository.findAll().size();

        // Create the CountryTranslate

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCountryTranslateMockMvc.perform(put("/api/country-translates")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(countryTranslate)))
            .andExpect(status().isBadRequest());

        // Validate the CountryTranslate in the database
        List<CountryTranslate> countryTranslateList = countryTranslateRepository.findAll();
        assertThat(countryTranslateList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCountryTranslate() throws Exception {
        // Initialize the database
        countryTranslateRepository.saveAndFlush(countryTranslate);

        int databaseSizeBeforeDelete = countryTranslateRepository.findAll().size();

        // Delete the countryTranslate
        restCountryTranslateMockMvc.perform(delete("/api/country-translates/{id}", countryTranslate.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<CountryTranslate> countryTranslateList = countryTranslateRepository.findAll();
        assertThat(countryTranslateList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CountryTranslate.class);
        CountryTranslate countryTranslate1 = new CountryTranslate();
        countryTranslate1.setId(1L);
        CountryTranslate countryTranslate2 = new CountryTranslate();
        countryTranslate2.setId(countryTranslate1.getId());
        assertThat(countryTranslate1).isEqualTo(countryTranslate2);
        countryTranslate2.setId(2L);
        assertThat(countryTranslate1).isNotEqualTo(countryTranslate2);
        countryTranslate1.setId(null);
        assertThat(countryTranslate1).isNotEqualTo(countryTranslate2);
    }
}
