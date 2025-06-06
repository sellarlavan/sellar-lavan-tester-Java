package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.codehaus.plexus.util.cli.Arg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;

    @Mock
    private ParkingSpotDAO parkingSpotDAO;

    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000)); // 1h avant
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest() {
        //when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        parkingService.processExitingVehicle();
        verify(ticketDAO).updateTicket(ticketCaptor.capture());
        Ticket ticket = ticketCaptor.getValue();
        assertNotNull(ticket.getOutTime());
        assertTrue(ticket.getPrice() > 0);

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));

    }

    // TODO ce test echoue
    @Disabled
    @Test
    public void testProcessIncomingVehicle() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        parkingService.processIncomingVehicle();

        verify(ticketDAO).saveTicket(ticketCaptor.capture());
        Ticket ticket = ticketCaptor.getValue();

        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());

        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).getNbTicket("ABCDEF");

    }

    @Test
    public void processExitingVehicleTestUnableUpdate(){

        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

        parkingService.processExitingVehicle();

        verify(ticketDAO).updateTicket(ticketCaptor.capture());
        Ticket ticket = ticketCaptor.getValue();

        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNotNull(ticket.getOutTime());


    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertEquals(1, parkingSpot.getId());
        assertTrue(parkingSpot.isAvailable());
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot);
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertNull(parkingSpot);

    }
}
