import json
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods

# You'll need to import your database connection function
# from your_database_module import select_query

@csrf_exempt 
@require_http_methods(["POST"])
def get_driver_rating_summary(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body)
            driver_id = data.get("driver_id")
            
            if not driver_id:
                return JsonResponse({"success": False, "message": "driver_id is required"}, status=400)

            # Query to get driver rating summary for numeric ratings (0-5)
            query = """
                SELECT 
                    COUNT(o.ratings) as total_ratings,
                    AVG(o.ratings) as average_rating,
                    COUNT(CASE WHEN o.ratings >= 4.5 AND o.ratings <= 5 THEN 1 END) as five_star,
                    COUNT(CASE WHEN o.ratings >= 3.5 AND o.ratings < 4.5 THEN 1 END) as four_star,
                    COUNT(CASE WHEN o.ratings >= 2.5 AND o.ratings < 3.5 THEN 1 END) as three_star,
                    COUNT(CASE WHEN o.ratings >= 1.5 AND o.ratings < 2.5 THEN 1 END) as two_star,
                    COUNT(CASE WHEN o.ratings >= 0.5 AND o.ratings < 1.5 THEN 1 END) as one_star
                FROM vtpartner.orders_tbl o
                WHERE o.driver_id = %s 
                AND o.ratings IS NOT NULL 
                AND o.ratings > 0
            """
            
            result = select_query(query, [driver_id])
            
            if not result or result[0][0] == 0:
                return JsonResponse({
                    "success": True,
                    "summary": {
                        "total_ratings": 0,
                        "average_rating": 0,
                        "rating_distribution": {
                            "5_star": 0,
                            "4_star": 0,
                            "3_star": 0,
                            "2_star": 0,
                            "1_star": 0
                        }
                    },
                    "message": "No ratings found for this driver"
                }, status=200)

            row = result[0]
            total_ratings = row[0]
            average_rating = round(float(row[1]), 2) if row[1] else 0

            rating_distribution = {
                "5_star": row[2] or 0,
                "4_star": row[3] or 0,
                "3_star": row[4] or 0,
                "2_star": row[5] or 0,
                "1_star": row[6] or 0
            }

            return JsonResponse({
                "success": True,
                "summary": {
                    "total_ratings": total_ratings,
                    "average_rating": average_rating,
                    "rating_distribution": rating_distribution
                },
                "driver_id": driver_id
            }, status=200)

        except Exception as err:
            print("Error fetching driver rating summary:", err)
            return JsonResponse({"success": False, "message": "Internal Server Error"}, status=500)

    return JsonResponse({"success": False, "message": "Method not allowed"}, status=405)

@csrf_exempt 
@require_http_methods(["POST"])
def get_service_availability_by_pincode(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body)
            pincode = data.get("pincode")
            auth_token = data.get("auth")
            
            if not pincode or not auth_token:
                return JsonResponse({"success": False, "message": "Missing required parameters"}, status=400)
            
            # Query to get city_id from pincode and check service availability
            query = """
                WITH pincode_city AS (
                    SELECT city_id 
                    FROM vtpartner.allowed_pincodes_tbl 
                    WHERE pincode = %s AND status = 1
                )
                SELECT 
                    pc.city_id,
                    scs.category_id,
                    scs.status as service_status
                FROM pincode_city pc
                LEFT JOIN vtpartner.service_city_status_tbl scs ON pc.city_id = scs.city_id
                WHERE pc.city_id IS NOT NULL
            """
            
            result = select_query(query, [pincode])
            
            if not result:
                # Pincode not found or not active, return all services as unavailable
                return JsonResponse({
                    "success": True,
                    "city_id": None,
                    "pincode": pincode,
                    "available_services": [],
                    "message": "Service not available in this area"
                }, status=200)
            
            city_id = result[0][0]  # First row, first column is city_id
            available_services = []
            
            for row in result:
                category_id = row[1]  # category_id
                service_status = row[2]  # status
                
                if category_id is not None and service_status == 1:
                    available_services.append(category_id)
            
            return JsonResponse({
                "success": True,
                "city_id": city_id,
                "pincode": pincode,
                "available_services": available_services,
                "message": "Service availability retrieved successfully"
            }, status=200)
                    
        except Exception as err:
            print("Error fetching service availability:", err)
            return JsonResponse({"success": False, "message": "Internal Server Error"}, status=500)
    
    return JsonResponse({"success": False, "message": "Method not allowed"}, status=405) 