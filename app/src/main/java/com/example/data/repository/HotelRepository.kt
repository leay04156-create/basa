package com.example.data.repository

import com.example.data.dao.HotelDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class HotelRepository(private val hotelDao: HotelDao) {
    // Rooms
    val allRooms: Flow<List<RoomEntity>> = hotelDao.getAllRooms()
    
    suspend fun insertRoom(room: RoomEntity) = hotelDao.insertRoom(room)
    suspend fun updateRoom(room: RoomEntity) = hotelDao.updateRoom(room)
    suspend fun updateRoomStatus(roomNumber: String, status: String) = hotelDao.updateRoomStatus(roomNumber, status)

    // Reservations
    val allReservations: Flow<List<ReservationEntity>> = hotelDao.getAllReservations()
    
    suspend fun insertReservation(reservation: ReservationEntity) = hotelDao.insertReservation(reservation)
    suspend fun updateReservation(reservation: ReservationEntity) = hotelDao.updateReservation(reservation)
    suspend fun deleteReservation(reservation: ReservationEntity) = hotelDao.deleteReservation(reservation)

    // Guests
    val allGuests: Flow<List<GuestEntity>> = hotelDao.getAllGuests()
    
    suspend fun insertGuest(guest: GuestEntity) = hotelDao.insertGuest(guest)
    suspend fun updateGuest(guest: GuestEntity) = hotelDao.updateGuest(guest)
    suspend fun deleteGuest(guest: GuestEntity) = hotelDao.deleteGuest(guest)

    // Invoices
    val allInvoices: Flow<List<InvoiceEntity>> = hotelDao.getAllInvoices()
    
    suspend fun insertInvoice(invoice: InvoiceEntity) = hotelDao.insertInvoice(invoice)
    suspend fun deleteInvoice(invoice: InvoiceEntity) = hotelDao.deleteInvoice(invoice)

    // Employees
    val allEmployees: Flow<List<EmployeeEntity>> = hotelDao.getAllEmployees()
    
    suspend fun insertEmployee(employee: EmployeeEntity) = hotelDao.insertEmployee(employee)
    suspend fun updateEmployee(employee: EmployeeEntity) = hotelDao.updateEmployee(employee)
    suspend fun deleteEmployee(employee: EmployeeEntity) = hotelDao.deleteEmployee(employee)
}
