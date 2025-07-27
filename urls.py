from django.urls import path
from . import views

urlpatterns = [
    path('get_driver_rating_summary/', views.get_driver_rating_summary, name='get_driver_rating_summary'),
    path('get_service_availability_by_pincode/', views.get_service_availability_by_pincode, name='get_service_availability_by_pincode'),
] 