# KAPS-CustomerAppJava
# KASP-Java-Android-App
# KASP_Customer_App_Android_Java

# Kaps Customer App

A comprehensive Android application for booking various services including cab, goods delivery, handyman services, JCB/crane services, and driver services.

## App Flow

### 1. Initial Flow
```
SplashScreenActivity -> IntroActivity -> LoginActivity -> SendOTPActivity -> 
[If New User] CustomerRegistrationActivity -> HomeActivity
[If Existing User] HomeActivity
```

### 2. Authentication Flow
- **SplashScreenActivity**: Initial loading screen (3 seconds)
  - Checks first run status
  - Routes to appropriate screen based on user status

- **IntroActivity**: App introduction
  - ViewPager with 3 info screens
  - Dots indicator for navigation
  - Continue button to LoginActivity

- **LoginActivity**: User authentication
  - Mobile number input with country code
  - Mobile number validation
  - Proceeds to OTP verification

- **SendOTPActivity**: OTP verification
  - 6-digit OTP input
  - 60-second timer for resend
  - Auto-paste OTP support
  - Routes to registration or home based on user status

- **CustomerRegistrationActivity**: New user registration
  - Full name input
  - Email input
  - Account type selection (Personal/Business)
  - Location capture

### 3. Main App Flow (HomeActivity)
- Current location display
- Service banners
- 5 dynamic services from API
- Bottom navigation

### 4. Service Modules

#### 4.1 Cab Service
```
HomeActivity -> CabBookingPickupLocationActivity -> CabBookingDropLocationActivity -> 
CabBookingReviewActivity -> CabSearchingActivity -> CabOngoingBookingDetailsActivity
```
- Pickup location selection
- Drop location selection
- Route details
- Booking review
- Driver searching
- Ongoing booking tracking

#### 4.2 Goods Service
```
HomeActivity -> GoodsPickupMapLocationActivity -> GoodsDriverMapDropLocationActivity -> 
OrderDetailsScreenActivity -> OngoingGoodsDetailActivity
```
- Pickup location selection
- Drop location selection
- Order details
- Ongoing goods tracking

#### 4.3 Handyman Service
```
HomeActivity -> HandymanWorkLocationActivity -> HandymanSearchingActivity -> 
HandymanBookingReviewScreenActivity -> HandymanOngoingBookingDetailsActivity
```
- Work location selection
- Service searching
- Booking review
- Ongoing service tracking

#### 4.4 JCB/Crane Service
```
HomeActivity -> JcbCraneWorkLocationActivity -> JcbCraneSearchingActivity -> 
JcbCraneBookingReviewActivity -> JcbCraneBookingDetailsActivity
```
- Work location selection
- Service searching
- Booking review
- Ongoing service tracking

#### 4.5 Driver Service
```
HomeActivity -> DriverPickupLocationActivity -> DriverDropLocationActivity -> 
DriverBookingDetailsActivity
```
- Pickup location selection
- Drop location selection
- Booking details

### 5. Common Features Across Services
- Real-time location tracking
- Google Maps integration
- Address search and validation
- Pincode verification
- Contact details management
- Payment processing
- Emergency contact support
- Booking cancellation
- Status updates

### 6. Project Structure
```
app/src/main/java/com/kapstranspvtltd/kaps/
├── activities/           # Main app activities
├── cab_customer_app/     # Cab service module
├── driver_customer_app/  # Driver service module
├── handyman_customer_app/# Handyman service module
├── jcb_crane_customer_app/# JCB/Crane service module
├── common_activities/    # Shared activities
├── fragments/           # UI components
├── models/             # Data models
├── network/            # API calls
├── utility/            # Helper classes
└── fcm/                # Firebase Cloud Messaging
```

### 7. Key Components

#### 7.1 Location Services
- Google Maps integration
- Current location detection
- Address search
- Route visualization
- Real-time tracking

#### 7.2 Authentication
- Mobile number verification
- OTP validation
- User registration
- Session management

#### 7.3 Booking Management
- Real-time tracking
- Status updates
- Payment processing
- Emergency support
- Booking cancellation

#### 7.4 Common Utilities
- Preference management
- Session handling
- Location helpers
- Network utilities
- Firebase integration

### 8. Dependencies
- Google Maps
- Firebase Cloud Messaging
- Volley for networking
- Retrofit for API calls
- Google Places API

### 9. Permissions Required
- Location (Fine and Coarse)
- Internet
- Phone
- Contacts
- Notifications

### 10. Configuration
- Google Maps API Key
- Firebase Configuration
- API Base URL
- Other service configurations

### 11. APIs and Services

#### 11.1 Base URLs
- Development: `http://00.00.00.00:8000/api/******/`
- Production: `https://www.******.in/api/********/`


#### 11.2 Authentication APIs
- `send_otp`: Send OTP to mobile number
  - Method: POST
  - Endpoint: `/send_otp`
  - Parameters: `mobile_no`
- `login`: User login
  - Method: POST
  - Endpoint: `/login`
- `customer_registration`: New user registration
  - Method: POST
  - Endpoint: `/customer_registration`
- `get_customer_details`: Get user profile
  - Method: POST
  - Endpoint: `/get_customer_details`
- `update_customer_details`: Update user profile
  - Method: POST
  - Endpoint: `/update_customer_details`

#### 11.3 Service-Specific APIs

##### Cab Service
- `allowed_pin_code`: Check if pincode is serviceable
  - Method: POST
  - Parameters: `pincode`
- `customers_all_cab_bookings`: Get all cab bookings
  - Method: POST
  - Parameters: `customer_id`
- `customers_all_cab_orders`: Get all cab orders
  - Method: POST
  - Parameters: `customer_id`
- `cab_booking_details_live_track`: Live tracking of cab
  - Method: POST
  - Parameters: `booking_id`, `customer_id`
- `cab_driver_current_location`: Get cab driver's location
  - Method: POST
  - Parameters: `driver_id`
- `cancel_cab_booking`: Cancel cab booking
  - Method: POST
  - Parameters: `booking_id`, `customer_id`, `driver_id`, `pickup_address`, `cancel_reason`
- `save_cab_order_ratings`: Rate cab service
  - Method: POST
  - Parameters: `order_id`, `ratings`, `ratings_description`
- `generate_new_cab_drivers_booking_id_get_nearby_drivers_with_fcm_token`: Book new cab
  - Method: POST
  - Parameters: Various booking details

##### Goods Service
- `allowed_pin_code`: Check if pincode is serviceable
  - Method: POST
  - Parameters: `pincode`
- `customers_all_bookings`: Get all goods bookings
  - Method: POST
  - Parameters: `customer_id`
- `customers_all_orders`: Get all goods orders
  - Method: POST
  - Parameters: `customer_id`
- `booking_details_live_track`: Live tracking of goods delivery
  - Method: POST
  - Parameters: `booking_id`, `customer_id`
- `goods_driver_current_location`: Get goods driver's location
  - Method: POST
  - Parameters: `driver_id`
- `cancel_booking`: Cancel goods booking
  - Method: POST
  - Parameters: `booking_id`, `customer_id`, `driver_id`, `pickup_address`, `cancel_reason`
- `save_order_ratings`: Rate goods service
  - Method: POST
  - Parameters: `order_id`, `ratings`, `ratings_description`
- `generate_new_goods_drivers_booking_id_get_nearby_drivers_with_fcm_token`: Book new goods delivery
  - Method: POST
  - Parameters: Various booking details
- `all_vehicles_with_price_details`: Get available vehicles
  - Method: POST
  - Parameters: Various vehicle details
- `get_peak_hour_prices`: Get peak hour pricing
  - Method: POST
  - Parameters: Various pricing details

##### JCB/Crane Service
- `allowed_pin_code`: Check if pincode is serviceable
  - Method: POST
  - Parameters: `pincode`
- `customers_all_jcb_crane_bookings`: Get all JCB/crane bookings
  - Method: POST
  - Parameters: `customer_id`
- `customers_all_jcb_crane_orders`: Get all JCB/crane orders
  - Method: POST
  - Parameters: `customer_id`
- `jcb_crane_driver_booking_details_live_track`: Live tracking of JCB/crane
  - Method: POST
  - Parameters: `booking_id`, `customer_id`
- `jcb_crane_driver_current_location`: Get JCB/crane driver's location
  - Method: POST
  - Parameters: `driver_id`
- `cancel_jcb_crane_driver_booking`: Cancel JCB/crane booking
  - Method: POST
  - Parameters: `booking_id`, `customer_id`, `driver_id`, `pickup_address`, `cancel_reason`
- `save_jcb_crane_order_ratings`: Rate JCB/crane service
  - Method: POST
  - Parameters: `order_id`, `ratings`, `ratings_description`
- `generate_new_jcb_crane_booking_id_get_nearby_agents_with_fcm_token`: Book new JCB/crane
  - Method: POST
  - Parameters: Various booking details

##### Driver Service
- `allowed_pin_code`: Check if pincode is serviceable
  - Method: POST
  - Parameters: `pincode`
- `customers_all_other_driver_bookings`: Get all driver bookings
  - Method: POST
  - Parameters: `customer_id`
- `customers_all_other_driver_orders`: Get all driver orders
  - Method: POST
  - Parameters: `customer_id`
- `other_driver_booking_details_live_track`: Live tracking of driver
  - Method: POST
  - Parameters: `booking_id`, `customer_id`
- `other_driver_current_location`: Get driver's current location
  - Method: POST
  - Parameters: `driver_id`
- `cancel_other_driver_booking`: Cancel driver booking
  - Method: POST
  - Parameters: `booking_id`, `customer_id`, `driver_id`, `pickup_address`, `cancel_reason`
- `save_other_driver_order_ratings`: Rate driver service
  - Method: POST
  - Parameters: `order_id`, `ratings`, `ratings_description`
- `generate_new_other_driver_booking_id_get_nearby_agents_with_fcm_token`: Book new driver
  - Method: POST
  - Parameters: Various booking details

##### Handyman Service
- `allowed_pin_code`: Check if pincode is serviceable
  - Method: POST
  - Parameters: `pincode`
- `customers_all_handyman_bookings`: Get all handyman bookings
  - Method: POST
  - Parameters: `customer_id`
- `customers_all_handyman_orders`: Get all handyman orders
  - Method: POST
  - Parameters: `customer_id`
- `handyman_agent_booking_details_live_track`: Live tracking of handyman
  - Method: POST
  - Parameters: `booking_id`, `customer_id`
- `handyman_agent_current_location`: Get handyman's current location
  - Method: POST
  - Parameters: `driver_id`
- `cancel_handyman_agent_booking`: Cancel handyman booking
  - Method: POST
  - Parameters: `booking_id`, `customer_id`, `driver_id`, `pickup_address`, `cancel_reason`
- `save_handyman_order_ratings`: Rate handyman service
  - Method: POST
  - Parameters: `order_id`, `ratings`, `ratings_description`
- `generate_new_handyman_booking_id_get_nearby_agents_with_fcm_token`: Book new handyman
  - Method: POST
  - Parameters: Various booking details

#### 11.4 Common APIs
- `get_all_banners`: Get service banners
  - Method: POST
- `all_services`: Get all available services
  - Method: POST
- `get_all_sub_categories`: Get service subcategories
  - Method: POST
- `get_all_sub_services`: Get service subservices
  - Method: POST
- `get_all_guide_lines`: Get service guidelines
  - Method: POST
- `get_scheduled_bookings`: Get scheduled bookings
  - Method: POST
- `all_coupons`: Get available coupons
  - Method: POST
- `update_wallet_balance`: Update wallet balance
  - Method: POST
- `customer_wallet_details`: Get wallet details
  - Method: POST
- `update_firebase_customer_token`: Update FCM token
  - Method: POST
- `get_agent_app_firebase_access_token`: Get agent FCM token
  - Method: POST
- `get_customer_app_firebase_access_token`: Get customer FCM token
  - Method: POST

#### 11.5 Third-Party APIs
- **Google Maps API**
  - API Key: `AIza*****************************`
  - Features:
    - Location services
    - Geocoding
    - Route visualization
    - Places API

- **Razorpay Payment Gateway**
  - Test Key: `rzp_test_61op4YoSkMB***`
  - Features:
    - Payment processing
    - Transaction management

#### 11.6 API Implementation
The app uses two main networking libraries:
1. **Retrofit**
   - Base configuration in `APIClient.java`
   - Interface definitions in `UserService.java`
   - JSON parsing with Gson

2. **Volley**
   - Singleton implementation in `VolleySingleton.java`
   - API helper methods in `APIHelper.java`
   - Default timeout: 30 seconds
   - Automatic retry policy

#### 11.7 API Security
- Content-Type: application/json
- Custom headers support
- Error handling and retry mechanisms
- Development/Production environment switching
- Server token authentication
- Customer token authentication

## Getting Started

1. Clone the repository
2. Add required API keys in `local.properties`
3. Sync project with Gradle files
4. Build and run the application

## Contributing
Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License
This project is licensed under the MIT License - see the LICENSE.md file for details
