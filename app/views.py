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

@csrf_exempt
def get_app_content(request):
    if request.method == "POST":
        try:
            # Query to get all app content
            query = """
                SELECT 
                    content_id,
                    screen_name,
                    title,
                    description,
                    image_url,
                    sort_order,
                    status,
                    time_at
                FROM vtpartner.app_content_tbl
                WHERE status = 1
                ORDER BY sort_order ASC
            """
            result = select_query(query)

            if not result:
                return JsonResponse({"message": "No app content found"}, status=404)

            # Create content dictionary organized by screen_name
            content_by_screen = {}
            
            for row in result:
                content_id = row[0]
                screen_name = row[1]
                title = row[2]
                description = row[3]
                image_url = row[4]
                sort_order = row[5]
                status = row[6]
                time_at = row[7]

                if screen_name not in content_by_screen:
                    content_by_screen[screen_name] = []
                
                content_by_screen[screen_name].append({
                    'content_id': content_id,
                    'title': title,
                    'description': description,
                    'image_url': image_url,
                    'sort_order': sort_order,
                    'status': status,
                    'time_at': time_at
                })

            return JsonResponse({
                "message": "Success",
                "content": content_by_screen
            }, status=200)

        except Exception as err:
            print("Error in get_app_content:", err)
            return JsonResponse({"message": "Internal Server Error"}, status=500)

    return JsonResponse({"message": "Method not allowed"}, status=405) 