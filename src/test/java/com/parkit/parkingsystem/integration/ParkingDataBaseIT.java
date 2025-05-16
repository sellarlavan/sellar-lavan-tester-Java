package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import static com.parkit.parkingsystem.constants.Fare.CAR_RATE_PER_HOUR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();

    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    public static void tearDown(){

    }


    @Test
    @Order(1)
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        
        assertNotNull(ticket);
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());

        ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertFalse(parkingSpot.isAvailable());

    }


    @Test
    @Order(2)
    public void testParkingLotExit() throws SQLException, ClassNotFoundException {
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //TODO: check that the fare generated and out time are populated correctly in the database
        try(Connection con = dataBaseTestConfig.getConnection()){
            PreparedStatement ps = con.prepareStatement("UPDATE ticket SET IN_TIME = ? WHERE VEHICLE_REG_NUMBER = ?");
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000)); // 1h avant
            ps.setString(2, "ABCDEF");
            ps.executeUpdate();
        }

        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals(1 * CAR_RATE_PER_HOUR, ticket.getPrice(), 0.01);
        assertNotNull(ticket.getOutTime());


    }

    @Order(3)
    @Test
    public void testParkingLotExitRecurringUser() throws SQLException, ClassNotFoundException {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        try(Connection con = dataBaseTestConfig.getConnection()){
            PreparedStatement ps = con.prepareStatement("UPDATE ticket set IN_TIME = ? WHERE VEHICLE_REG_NUMBER = ? AND OUT_TIME IS NULL");
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis() - 45 * 60 * 1000));
            ps.setString(2, "ABCDEF");
            ps.executeUpdate();
        }
        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals(0.75 * CAR_RATE_PER_HOUR, ticket.getPrice(), 0.01);
        assertNotNull(ticket.getOutTime());

        parkingService.processIncomingVehicle();
        try(Connection con = dataBaseTestConfig.getConnection()){
            PreparedStatement ps = con.prepareStatement("UPDATE ticket set IN_TIME = ? WHERE VEHICLE_REG_NUMBER = ? AND OUT_TIME IS NULL");
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000));
            ps.setString(2, "ABCDEF");
            ps.executeUpdate();
        }

        parkingService.processExitingVehicle();

        ticket = ticketDAO.getTicket("ABCDEF");
        assertEquals(1 * CAR_RATE_PER_HOUR * 0.95, ticket.getPrice(), 0.01);
        assertNotNull(ticket.getOutTime());


    }

}
