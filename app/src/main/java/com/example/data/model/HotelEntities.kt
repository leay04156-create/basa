package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hotel_rooms")
data class RoomEntity(
    @PrimaryKey val roomNumber: String,
    val type: String,          // جناح ملكي, جناح ديلوكس, غرفة فاخرة, جناح تنفيذي
    val status: String,        // شاغرة, محجوزة, تنظيف, صيانة
    val pricePerNight: Double,
    val floor: Int,
    val bedCount: Int,
    val features: String       // ميزات الغرفة مثل "إطلالة بحرية, جاكوزي, إنترنت سريع"
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guestName: String,
    val guestPhone: String,
    val roomNumber: String,
    val checkInDate: String,    // YYYY-MM-DD
    val checkOutDate: String,   // YYYY-MM-DD
    val totalPrice: Double,
    val status: String,         // مؤكد, مكتمل, ملغي
    val notes: String = ""
)

@Entity(tableName = "guests")
data class GuestEntity(
    @PrimaryKey val phone: String,
    val name: String,
    val email: String,
    val country: String,
    val previousStays: Int,
    val totalSpent: Double,
    val status: String         // نخبة VIP, ذهبي, فضي, كلاسيك
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val guestName: String,
    val roomNumber: String,
    val date: String,
    val amount: Double,
    val status: String,         // مدفوعة, معلقة, مستردة
    val method: String          // بطاقة ائتمان, نقداً, تحويل بنكي
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,           // مدير مناوبة, استقبال, خدمة غرف, صيانة, أمن
    val shift: String,          // صباحية, مسائية, ليلية
    val phone: String,
    val status: String          // في الخدمة, خارج الخدمة
)
