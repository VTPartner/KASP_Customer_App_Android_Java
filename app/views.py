@api_view(['POST'])
def update_booking_status_jcb_crane_driver(request):
    try:
        booking_id = request.data.get('booking_id')
        booking_status = request.data.get('booking_status')
        total_payment = request.data.get('total_payment')
        penalty_amount = float(request.data.get('penalty_amount', 0))
        
        # Initialize penalty_text with empty string
        penalty_text = ""
        
        if booking_status == "Make Payment":
            if penalty_amount > 0:
                penalty_text = f"Penalty Amount - Rs.{penalty_amount}/-"
            body = f"Please do the payment against Booking ID {booking_id} for Driver Service. Total Amount=Rs.{total_payment}/-\n{penalty_text}"
            
        # ... rest of the code ...
    except Exception as e:
        return Response({"error": str(e)}, status=status.HTTP_400_BAD_REQUEST) 