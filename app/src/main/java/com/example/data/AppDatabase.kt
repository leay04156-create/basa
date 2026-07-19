package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.HotelDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RoomEntity::class,
        ReservationEntity::class,
        GuestEntity::class,
        InvoiceEntity::class,
        EmployeeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hotelDao(): HotelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luxury_hotel_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.hotelDao())
                }
            }
        }

        suspend fun populateDatabase(dao: HotelDao) {
            // Initial Rooms (some booked, some vacant, cleaning, maintenance)
            val rooms = listOf(
                RoomEntity("101", "غرفة فاخرة", "شاغرة", 850.0, 1, 1, "إطلالة بحرية, إنترنت سريع, آلة قهوة إسبريسو"),
                RoomEntity("102", "غرفة فاخرة", "محجوزة", 850.0, 1, 1, "إطلالة بحرية, إنترنت سريع, آلة قهوة إسبريسو"),
                RoomEntity("103", "غرفة فاخرة", "تنظيف", 850.0, 1, 2, "إطلالة على الحديقة, سرير كينج, ميني بار"),
                RoomEntity("104", "غرفة فاخرة", "صيانة", 850.0, 1, 1, "إطلالة على الحديقة, ميني بار"),
                
                RoomEntity("201", "جناح ديلوكس", "شاغرة", 1600.0, 2, 2, "شرفة واسعة, جاكوزي خاص, إطلالة بحرية, صالة معيشة منفصلة"),
                RoomEntity("202", "جناح ديلوكس", "محجوزة", 1600.0, 2, 2, "شرفة واسعة, جاكوزي خاص, إطلالة بحرية, صالة معيشة منفصلة"),
                RoomEntity("203", "جناح ديلوكس", "شاغرة", 1500.0, 2, 2, "إطلالة على المدينة, صالة معيشة, نظام صوتي ذكي"),
                
                RoomEntity("301", "جناح تنفيذي", "محجوزة", 2400.0, 3, 2, "إطلالة بانورامية, مكتب عمل فاخر, دخول حصري للردهة التنفيذية, جاكوزي"),
                RoomEntity("302", "جناح تنفيذي", "شاغرة", 2400.0, 3, 2, "إطلالة بانورامية, مكتب عمل فاخر, دخول حصري للردهة التنفيذية, جاكوزي"),
                
                RoomEntity("401", "جناح ملكي", "محجوزة", 5200.0, 4, 3, "مصعد خاص, خدمة نادل شخصي 24 ساعة, بيانو كبير, جاكوزي خارجي, سينما منزلية"),
                RoomEntity("402", "جناح ملكي", "شاغرة", 5200.0, 4, 3, "مصعد خاص, خدمة نادل شخصي 24 ساعة, بيانو كبير, جاكوزي خارجي, صالة رياضية مصغرة")
            )
            dao.insertRooms(rooms)

            // Initial Guests
            val guests = listOf(
                GuestEntity("+966501234567", "سمو الأمير عبد العزيز بن سلمان", "abdulaziz@royals.sa", "المملكة العربية السعودية", 14, 84200.0, "نخبة VIP"),
                GuestEntity("+971509876543", "سارة بنت أحمد الهاشمي", "sara.alhashemi@dubai.ae", "دولة الإمارات العربية المتحدة", 6, 21500.0, "ذهبي"),
                GuestEntity("+96560456123", "فيصل محمد الكندري", "f.alkandari@kwt.com", "دولة الكويت", 8, 32000.0, "ذهبي"),
                GuestEntity("+96899112233", "مريم بنت سعيد العلوية", "maryam.alawi@oman.om", "سلطنة عمان", 3, 9800.0, "فضي"),
                GuestEntity("+12025550143", "د. ألكسندر تشامبرز", "a.chambers@harvard.edu", "الولايات المتحدة الأمريكية", 4, 18400.0, "فضي"),
                GuestEntity("+966541230987", "خالد يوسف الحربي", "khalid@harb.me", "المملكة العربية السعودية", 1, 1500.0, "كلاسيك")
            )
            dao.insertGuests(guests)

            // Initial Reservations
            val reservations = listOf(
                ReservationEntity(0, "سمو الأمير عبد العزيز بن سلمان", "+966501234567", "401", "2026-07-18", "2026-07-25", 36400.0, "مؤكد", "يرجى توفير زهور الأوركيد البيضاء الطازجة وترتيب النادل الشخصي"),
                ReservationEntity(0, "سارة بنت أحمد الهاشمي", "+971509876543", "202", "2026-07-19", "2026-07-22", 4800.0, "مؤكد", "تفضيل وسائد ريش إضافية"),
                ReservationEntity(0, "د. ألكسندر تشامبرز", "+12025550143", "301", "2026-07-15", "2026-07-20", 12000.0, "مؤكد", "النزيل يحتاج هدوء تام لإنجاز أبحاث"),
                ReservationEntity(0, "فيصل محمد الكندري", "+96560456123", "102", "2026-07-10", "2026-07-15", 4250.0, "مكتمل", "تمت ترقية الغرفة مجاناً"),
                ReservationEntity(0, "خالد يوسف الحربي", "+966541230987", "103", "2026-07-22", "2026-07-24", 1700.0, "مؤكد", "")
            )
            for (res in reservations) {
                dao.insertReservation(res)
            }

            // Initial Invoices
            val invoices = listOf(
                InvoiceEntity(0, "سمو الأمير عبد العزيز بن سلمان", "401", "2026-07-18", 36400.0, "مدفوعة", "تحويل بنكي"),
                InvoiceEntity(0, "سارة بنت أحمد الهاشمي", "202", "2026-07-19", 4800.0, "معلقة", "بطاقة ائتمان"),
                InvoiceEntity(0, "د. ألكسندر تشامبرز", "301", "2026-07-15", 12000.0, "مدفوعة", "بطاقة ائتمان"),
                InvoiceEntity(0, "فيصل محمد الكندري", "102", "2026-07-15", 4250.0, "مدفوعة", "نقداً")
            )
            dao.insertInvoices(invoices)

            // Initial Employees
            val employees = listOf(
                EmployeeEntity(0, "أحمد مسعود الزهراني", "مدير مناوبة", "صباحية", "+966551112222", "في الخدمة"),
                EmployeeEntity(0, "ليان بنت فهد العتيبي", "استقبال", "صباحية", "+966552223333", "في الخدمة"),
                EmployeeEntity(0, "عبد الله محمد عسيري", "خدمة غرف", "مسائية", "+966553334444", "في الخدمة"),
                EmployeeEntity(0, "فاطمة محمد الشريف", "استقبال", "مسائية", "+966554445555", "خارج الخدمة"),
                EmployeeEntity(0, "سعيد عمر الحربي", "صيانة", "ليلية", "+966555556666", "في الخدمة"),
                EmployeeEntity(0, "رائد خالد النفيعي", "أمن", "ليلية", "+966556667777", "في الخدمة")
            )
            dao.insertEmployees(employees)
        }
    }
}
