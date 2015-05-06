/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.encounter;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Concept;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.builder.OrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSDrugOrderMapper;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSOrderMapper;

public class EmrOrderServiceImpl_1_11Test {

    @Mock
    private EncounterService encounterService;

    @Mock
    private OpenMRSDrugOrderMapper openMRSDrugOrderMapper;

    @Mock
    private OpenMRSOrderMapper openMRSOrderMapper;

    @Mock
    private OrderService orderService;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldSaveNewDrugOrdersInTheSequenceOfOrdering() throws ParseException {
        EmrOrderServiceImpl_1_11 emrOrderService = new EmrOrderServiceImpl_1_11(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper);
        EncounterTransaction.DrugOrder drugOrder1 = new DrugOrderBuilder().withDrugUuid("drug-uuid1").build();
        EncounterTransaction.DrugOrder drugOrder2 = new DrugOrderBuilder().withDrugUuid("drug-uuid2").build();
        DrugOrder mappedDrugOrder1 = new DrugOrder();
        DrugOrder mappedDrugOrder2 = new DrugOrder();
        Encounter encounter = new Encounter();
        when(openMRSDrugOrderMapper.map(drugOrder1, encounter)).thenReturn(mappedDrugOrder1);
        when(openMRSDrugOrderMapper.map(drugOrder2, encounter)).thenReturn(mappedDrugOrder2);

        emrOrderService.save(Arrays.asList(drugOrder1, drugOrder2), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(2));
        assertThat((DrugOrder)savedOrders.get(0), is(sameInstance(mappedDrugOrder1)));
        assertThat((DrugOrder)savedOrders.get(1), is(sameInstance(mappedDrugOrder2)));
    }

    @Test
    public void shouldSaveNewDrugOrdersInTheSequenceOfOrderingToAnEncounterWithExistingOrders() throws ParseException {
        EmrOrderServiceImpl_1_11 emrOrderService = new EmrOrderServiceImpl_1_11(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper);
        EncounterTransaction.DrugOrder drugOrder3 = new DrugOrderBuilder().withDrugUuid("drug-uuid3").build();
        EncounterTransaction.DrugOrder drugOrder4 = new DrugOrderBuilder().withDrugUuid("drug-uuid4").build();
        DrugOrder existingDrugOrder1 = new DrugOrder();
        DrugOrder existingDrugOrder2 = new DrugOrder();
        DrugOrder mappedDrugOrder3 = new DrugOrder();
        DrugOrder mappedDrugOrder4 = new DrugOrder();
        Encounter encounter = new Encounter();
        encounter.addOrder(existingDrugOrder1);
        encounter.addOrder(existingDrugOrder2);
        when(openMRSDrugOrderMapper.map(drugOrder3, encounter)).thenReturn(mappedDrugOrder3);
        when(openMRSDrugOrderMapper.map(drugOrder4, encounter)).thenReturn(mappedDrugOrder4);

        emrOrderService.save(Arrays.asList(drugOrder3, drugOrder4), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(4));
        assertThat((DrugOrder)savedOrders.get(2), is(sameInstance(mappedDrugOrder3)));
        assertThat((DrugOrder)savedOrders.get(3), is(sameInstance(mappedDrugOrder4)));
    }

    @Test
    public void shouldSaveNewOrders() throws ParseException {
        EmrOrderServiceImpl_1_11 emrOrderService = new EmrOrderServiceImpl_1_11(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper);
        EncounterTransaction.Order order1 = new OrderBuilder().withConceptUuid("concept-uuid1").build();
        EncounterTransaction.Order order2 = new OrderBuilder().withConceptUuid("concept-uuid2").build();

        Order mappedOrder1 = new Order();
        Concept concept = new Concept();
        concept.setUuid("concept-uuid1");
        mappedOrder1.setConcept(concept);

        Order mappedOrder2 = new Order();
        concept = new Concept();
        concept.setUuid("concept-uuid2");
        mappedOrder2.setConcept(concept);

        Encounter encounter = new Encounter();
        when(openMRSOrderMapper.map(order1,encounter)).thenReturn(mappedOrder1);
        when(openMRSOrderMapper.map(order2,encounter)).thenReturn(mappedOrder2);

        emrOrderService.saveOrders(Arrays.asList(order1, order2), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(2));
        assertTrue(existsInOrdersList(mappedOrder1, savedOrders));
        assertTrue(existsInOrdersList(mappedOrder2, savedOrders));
    }

    @Test
    public void shouldSaveNewOrdersToEncounterWithExistingOrders() throws ParseException {
        EmrOrderServiceImpl_1_11 emrOrderService = new EmrOrderServiceImpl_1_11(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper);
        EncounterTransaction.Order testOrder1 = new OrderBuilder().withConceptUuid("concept-uuid1").build();
        EncounterTransaction.Order testOrder2 = new OrderBuilder().withConceptUuid("concept-uuid2").build();

        Order mappedOrder1 = new Order();
        Concept concept = new Concept();
        concept.setUuid("concept-uuid1");
        mappedOrder1.setConcept(concept);

        Order mappedOrder2 = new Order();
        concept = new Concept();
        concept.setUuid("concept-uuid2");
        mappedOrder2.setConcept(concept);

        Order existingOrder1 = new Order();
        Order existingOrder2 = new Order();

        Encounter encounter = new Encounter();
        encounter.addOrder(existingOrder1);
        encounter.addOrder(existingOrder2);

        when(openMRSOrderMapper.map(testOrder1,encounter)).thenReturn(mappedOrder1);
        when(openMRSOrderMapper.map(testOrder2,encounter)).thenReturn(mappedOrder2);

        emrOrderService.saveOrders(Arrays.asList(testOrder1, testOrder2), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(4));
        assertTrue(existsInOrdersList(mappedOrder1, savedOrders));
        assertTrue(existsInOrdersList(mappedOrder2, savedOrders));
    }

    private boolean existsInOrdersList(Order testOrder, ArrayList<Order> orderArrayList) {
        for(Order order : orderArrayList) {
            if(order.getConcept()!=null && order.getConcept().getUuid().equals(order.getConcept().getUuid()) )
                return true;
        }
        return false;
    }
}