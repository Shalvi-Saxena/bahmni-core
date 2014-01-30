package org.bahmni.module.referncedatafeedclient.worker;

import org.bahmni.module.referncedatafeedclient.ReferenceDataFeedProperties;
import org.bahmni.module.referncedatafeedclient.domain.ReferenceDataConcept;
import org.bahmni.module.referncedatafeedclient.domain.Panel;
import org.bahmni.module.referncedatafeedclient.domain.Test;
import org.bahmni.module.referncedatafeedclient.service.ReferenceDataConceptService;
import org.bahmni.webclients.HttpClient;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class PanelEventWorker implements EventWorker {
    public static final String LAB_SET = "LabSet";
    @Resource(name = "referenceDataHttpClient")
    private HttpClient httpClient;
    private final ReferenceDataFeedProperties referenceDataFeedProperties;
    private ConceptService conceptService;
    private ReferenceDataConceptService referenceDataConceptService;

    @Autowired
    public PanelEventWorker(HttpClient httpClient, ReferenceDataFeedProperties referenceDataFeedProperties, ConceptService conceptService, ReferenceDataConceptService referenceDataConceptService) {
        this.httpClient = httpClient;
        this.referenceDataFeedProperties = referenceDataFeedProperties;
        this.conceptService = conceptService;
        this.referenceDataConceptService = referenceDataConceptService;
    }

    @Override
    public void process(Event event) {
        try {
            Panel panel = httpClient.get(referenceDataFeedProperties.getReferenceDataUri() + event.getContent(), Panel.class);
            ReferenceDataConcept referenceDataConcept = new ReferenceDataConcept(panel.getId(), panel.getName(), LAB_SET, ConceptDatatype.N_A_UUID);
            referenceDataConcept.setDescription(panel.getDescription());
            referenceDataConcept.setShortName(panel.getShortName());
            referenceDataConcept.setSetMemberUuids(getTestUuids(panel));
            referenceDataConcept.setRetired(!panel.isActive());
            referenceDataConcept.setSet(true);
            Concept panelConcept = referenceDataConceptService.saveConcept(referenceDataConcept);
            referenceDataConceptService.saveSetMembership(conceptService.getConceptByUuid(panel.getSample().getId()), panelConcept);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    private Set<String> getTestUuids(Panel panel) {
        HashSet<String> testUuids = new HashSet<>();
        for (Test test : panel.getTests()) {
            testUuids.add(test.getId());
        }
        return testUuids;
    }

    @Override
    public void cleanUp(Event event) {

    }
}
