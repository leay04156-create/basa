package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.HotelRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HotelViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HotelRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = HotelRepository(database.hotelDao())
    }

    val rooms: StateFlow<List<RoomEntity>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reservations: StateFlow<List<ReservationEntity>> = repository.allReservations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val guests: StateFlow<List<GuestEntity>> = repository.allGuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun addReservation(
        guestName: String,
        guestPhone: String,
        roomNumber: String,
        checkInDate: String,
        checkOutDate: String,
        totalPrice: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val res = ReservationEntity(
                guestName = guestName,
                guestPhone = guestPhone,
                roomNumber = roomNumber,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                totalPrice = totalPrice,
                status = "مؤكد",
                notes = notes
            )
            repository.insertReservation(res)
            // Update room status
            repository.updateRoomStatus(roomNumber, "محجوزة")
            
            // Auto create invoice
            val invoice = InvoiceEntity(
                guestName = guestName,
                roomNumber = roomNumber,
                date = checkInDate,
                amount = totalPrice,
                status = "معلقة",
                method = "بطاقة ائتمان"
            )
            repository.insertInvoice(invoice)

            // Auto create or update Guest if not exists
            val defaultGuest = GuestEntity(
                phone = guestPhone,
                name = guestName,
                email = if (guestPhone.length > 5) "${guestPhone.takeLast(5)}@opal.luxury" else "guest@opal.luxury",
                country = "المملكة العربية السعودية",
                previousStays = 1,
                totalSpent = totalPrice,
                status = "كلاسيك"
            )
            repository.insertGuest(defaultGuest)
        }
    }

    fun updateReservationStatus(reservation: ReservationEntity, status: String) {
        viewModelScope.launch {
            val updated = reservation.copy(status = status)
            repository.insertReservation(updated)
            if (status == "مكتمل") {
                repository.updateRoomStatus(reservation.roomNumber, "تنظيف")
            } else if (status == "ملغي") {
                repository.updateRoomStatus(reservation.roomNumber, "شاغرة")
            }
        }
    }

    fun addRoom(roomNumber: String, type: String, pricePerNight: Double, floor: Int, bedCount: Int, features: String) {
        viewModelScope.launch {
            val room = RoomEntity(
                roomNumber = roomNumber,
                type = type,
                status = "شاغرة",
                pricePerNight = pricePerNight,
                floor = floor,
                bedCount = bedCount,
                features = features
            )
            repository.insertRoom(room)
        }
    }

    fun updateRoomStatus(roomNumber: String, status: String) {
        viewModelScope.launch {
            repository.updateRoomStatus(roomNumber, status)
        }
    }

    fun addGuest(name: String, phone: String, email: String, country: String, status: String) {
        viewModelScope.launch {
            val guest = GuestEntity(
                phone = phone,
                name = name,
                email = email,
                country = country,
                previousStays = 1,
                totalSpent = 0.0,
                status = status
            )
            repository.insertGuest(guest)
        }
    }

    fun addInvoice(guestName: String, roomNumber: String, amount: Double, status: String, method: String) {
        viewModelScope.launch {
            val invoice = InvoiceEntity(
                guestName = guestName,
                roomNumber = roomNumber,
                date = "2026-07-19",
                amount = amount,
                status = status,
                method = method
            )
            repository.insertInvoice(invoice)
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    fun updateEmployeeStatus(employee: EmployeeEntity, status: String) {
        viewModelScope.launch {
            val updated = employee.copy(status = status)
            repository.updateEmployee(updated)
        }
    }

    fun addEmployee(name: String, role: String, shift: String, phone: String) {
        viewModelScope.launch {
            val employee = EmployeeEntity(
                name = name,
                role = role,
                shift = shift,
                phone = phone,
                status = "في الخدمة"
            )
            repository.insertEmployee(employee)
        }
    }
}
