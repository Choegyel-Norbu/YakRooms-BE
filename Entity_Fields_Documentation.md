# YakRooms Entity Fields Documentation

A comprehensive reference guide for all database entities in the YakRooms Hotel Booking and Management System.

## Table of Contents

1. [User Entity](#user-entity)
2. [Booking Entity](#booking-entity)
3. [Hotel Entity](#hotel-entity)
4. [Staff Entity](#staff-entity)
5. [Room Entity](#room-entity)
6. [Entity Relationships](#entity-relationships)
7. [Common Patterns](#common-patterns)

---

## User Entity

**Table Name:** `users`

**Description:** Represents system users including guests, hotel staff, and administrators.

### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| **id** | `Long` | Primary Key, Auto-increment | Unique identifier for the user |
| **hotel** | `Hotel` | Foreign Key (nullable) | Associated hotel for staff users |
| **name** | `String` | Max length: 100 | User's full name |
| **email** | `String` | Unique, Not null, Max length: 255 | User's email address (used for authentication) |
| **password** | `String` | - | Encrypted password |
| **phone** | `String` | Max length: 20 | Contact phone number |
| **profilePicUrl** | `String` | Max length: 500 | URL to user's profile picture |
| **roles** | `Set<Role>` | Enum collection | User roles (GUEST, STAFF, ADMIN, etc.) |
| **staff** | `Staff` | One-to-One relationship | Associated staff record if user is hotel staff |
| **isActive** | `boolean` | Not null, Default: true | Account activation status |
| **lastLogin** | `LocalDateTime` | - | Timestamp of last successful login |
| **createdAt** | `LocalDateTime` | Not null, Auto-set | Account creation timestamp |
| **updatedAt** | `LocalDateTime` | Not null, Auto-update | Last modification timestamp |

### Indexes
- `idx_user_email` - Fast email lookup
- `idx_user_hotel_id` - Query users by hotel
- `idx_user_active` - Filter active/inactive users
- `idx_user_email_active` - Composite index for login queries

---

## Booking Entity

**Table Name:** `booking`

**Description:** Represents hotel room reservations made by users.

### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| **id** | `Long` | Primary Key, Auto-increment | Unique booking identifier |
| **user** | `User` | Not null, Foreign Key | User who made the booking |
| **hotel** | `Hotel` | Not null, Foreign Key | Hotel where booking is made |
| **room** | `Room` | Not null, Foreign Key | Specific room booked |
| **phone** | `String` | Max length: 20, Pattern validation | Contact number for booking |
| **checkInDate** | `LocalDate` | Not null | Scheduled check-in date |
| **checkOutDate** | `LocalDate` | Not null, Must be future date | Scheduled check-out date |
| **guests** | `int` | Min: 1, Max: 20 | Number of guests |
| **checkInPasscode** | `String` | Max length: 10 | Digital check-in code |
| **passcode** | `String` | Unique, Not null, Exactly 6 chars | Unique booking reference code |
| **status** | `BookingStatus` | Not null, Default: PENDING | Booking status (PENDING, CONFIRMED, CANCELLED, COMPLETED) |
| **paymentStatus** | `PaymentStatus` | Not null, Default: PENDING | Payment status (PENDING, PAID, REFUNDED) |
| **totalPrice** | `BigDecimal` | Not null, Min: 0.00 | Total booking amount |
| **createdAt** | `LocalDateTime` | Not null, Auto-set | Booking creation timestamp |
| **updatedAt** | `LocalDateTime` | Not null, Auto-update | Last modification timestamp |

### Indexes
- `idx_booking_user_id` - Query bookings by user
- `idx_booking_hotel_id` - Query bookings by hotel
- `idx_booking_room_id` - Query bookings by room
- `idx_booking_passcode` - Fast passcode lookup
- `idx_booking_status` - Filter by booking status
- `idx_booking_dates` - Date range queries
- `idx_booking_created_at` - Sort by creation date

---

## Hotel Entity

**Table Name:** `hotels`

**Description:** Represents hotels registered in the system.

### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| **id** | `Long` | Primary Key, Auto-increment | Unique hotel identifier |
| **name** | `String` | Not null, Max length: 255 | Hotel name |
| **email** | `String` | Unique, Not null, Max length: 255 | Official hotel email |
| **phone** | `String` | Not null, Max length: 20 | Hotel contact number |
| **address** | `String` | Not null, Max length: 500 | Full street address |
| **district** | `String` | Not null, Max length: 100 | District/area location |
| **logoUrl** | `String` | Max length: 500 | URL to hotel logo |
| **description** | `String` | TEXT column | Hotel description and details |
| **isVerified** | `boolean` | Not null, Default: false | Verification status |
| **websiteUrl** | `String` | Max length: 500 | Hotel website URL |
| **licenseUrl** | `String` | Max length: 500 | Business license document URL |
| **idProofUrl** | `String` | Max length: 500 | Identity proof document URL |
| **latitude** | `String` | Max length: 50 | Geographic latitude coordinate |
| **longitude** | `String` | Max length: 50 | Geographic longitude coordinate |
| **hotelType** | `HotelType` | Enum | Type of hotel (STANDARD, LUXURY, BUDGET, etc.) |
| **users** | `Set<User>` | One-to-Many | Associated users (staff) |
| **restaurant** | `Restaurant` | One-to-One | Associated restaurant if available |
| **staffList** | `Set<Staff>` | One-to-Many | Hotel staff members |
| **rooms** | `Set<Room>` | One-to-Many | Hotel rooms |
| **amenities** | `Set<String>` | Element collection | List of hotel amenities |
| **photoUrls** | `Set<String>` | Element collection | Gallery photo URLs |
| **createdAt** | `LocalDateTime` | Not null, Auto-set | Registration timestamp |
| **updatedAt** | `LocalDateTime` | Not null, Auto-update | Last modification timestamp |

### Indexes
- `idx_hotel_email` - Fast email lookup
- `idx_hotel_district` - Query by location
- `idx_hotel_verified` - Filter verified hotels
- `idx_hotel_type` - Filter by hotel type
- `idx_hotel_district_type_verified` - Composite search index

---

## Staff Entity

**Table Name:** `staff`

**Description:** Represents hotel staff members with their employment details.

### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| **id** | `Long` | Primary Key, Auto-increment | Unique staff identifier |
| **email** | `String` | Unique, Not null, Max length: 100 | Staff email address |
| **phoneNumber** | `String` | Not null, Max length: 20 | Staff phone number |
| **position** | `String` | Max length: 50 | Job position/title |
| **dateJoined** | `LocalDate` | Default: current date | Employment start date |
| **hotel** | `Hotel` | Not null, Foreign Key | Employer hotel |
| **user** | `User` | One-to-One, Foreign Key | Associated user account |

### Indexes
- `idx_staff_email` - Unique email constraint
- `idx_staff_hotel_id` - Query staff by hotel
- `idx_staff_user_id` - Link to user account
- `idx_staff_hotel_position` - Query by hotel and position
- `idx_staff_date_joined` - Sort by join date
- `idx_staff_hotel_date_joined` - Hotel-specific date queries

---

## Room Entity

**Table Name:** `room`

**Description:** Represents individual hotel rooms available for booking.

### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| **id** | `Long` | Primary Key, Auto-increment | Unique room identifier |
| **hotel** | `Hotel` | Not null, Foreign Key | Hotel that owns the room |
| **roomNumber** | `String` | Not null, Max length: 50 | Room number (unique per hotel) |
| **roomType** | `RoomType` | Not null, Enum | Type of room (SINGLE, DOUBLE, SUITE, etc.) |
| **isAvailable** | `boolean` | Not null, Default: true | Current availability status |
| **price** | `Double` | Not null | Price per night |
| **maxGuests** | `int` | Not null | Maximum occupancy |
| **description** | `String` | TEXT column | Room description and features |
| **createdAt** | `LocalDateTime` | Not null, Auto-set | Room creation timestamp |
| **updatedAt** | `LocalDateTime` | Not null, Auto-update | Last modification timestamp |
| **amenities** | `List<String>` | Element collection | Room-specific amenities |
| **imageUrl** | `List<String>` | Element collection | Room photo URLs |
| **items** | `List<RoomItem>` | One-to-Many | Items/inventory in room |
| **notification** | `Notification` | One-to-One | Associated notifications |

### Indexes
- `idx_room_hotel_id` - Query rooms by hotel
- `idx_room_number_hotel` - Unique room number per hotel
- `idx_room_type` - Filter by room type
- `idx_room_available` - Filter available rooms
- `idx_room_price` - Price range queries
- `idx_room_hotel_available` - Available rooms per hotel
- `idx_room_created_at` - Sort by creation date

---

## Entity Relationships

### Primary Relationships

```
User (1) ←→ (1) Staff
User (N) → (1) Hotel
User (1) → (N) Booking

Hotel (1) ←→ (1) Restaurant
Hotel (1) → (N) Staff
Hotel (1) → (N) Room
Hotel (1) → (N) Booking

Room (1) → (N) Booking
Room (N) → (1) Hotel

Booking (N) → (1) User
Booking (N) → (1) Hotel
Booking (N) → (1) Room
```

### Key Points
- **User-Staff**: One-to-one bidirectional relationship
- **Booking**: Central entity connecting Users, Hotels, and Rooms
- **Hotel**: Parent entity for Staff, Rooms, and optionally Restaurant
- **Cascade Operations**: Carefully configured to maintain data integrity
- **Lazy Loading**: Used throughout to optimize performance

---

## Common Patterns

### Audit Fields
All entities include:
- `createdAt` - Automatically set on creation
- `updatedAt` - Automatically updated on modification

### Performance Optimizations
- **Batch Size**: Collections use `@BatchSize(size = 20)` to prevent N+1 queries
- **Lazy Loading**: `FetchType.LAZY` used for relationships
- **Indexing**: Strategic indexes for common query patterns

### Data Integrity
- **Cascade Types**: Configured per relationship needs
- **Orphan Removal**: Enabled where appropriate
- **Validation**: JSR-303 annotations for data validation

### Naming Conventions
- **Tables**: Snake_case (e.g., `user_roles`)
- **Columns**: Snake_case (e.g., `check_in_date`)
- **Java Fields**: camelCase (e.g., `checkInDate`)

---

## Notes

1. **Security**: Passwords should be encrypted using BCrypt or similar
2. **Transactions**: Use `@Transactional` for operations spanning multiple entities
3. **DTOs**: Consider using DTOs to avoid exposing entity internals
4. **Validation**: Leverage Bean Validation for input validation
5. **Caching**: Consider second-level cache for frequently accessed data