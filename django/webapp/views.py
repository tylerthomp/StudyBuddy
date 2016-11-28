# Django
from django.shortcuts import render
from django.http import JsonResponse
from django.http import HttpResponse
from django.contrib.auth.decorators import login_required

# API
from api.models import Attendance
from api.models import Availability
from api.models import Class
from api.models import Review
from api.models import StudentToClass
from api.models import StudySession
from api.models import UserProfile

from api.attendance import _get_past_attendances
from api.attendance import _get_future_attendances
from api.availability import add_availability
from api.availability import delete_availability
from api.review import perform_create_review
from api.student_to_class import get_or_add_class
from api.student_to_class import register_for_class
from api.study_session import create_session
from api.study_session import _search

# Python
import re
import time


@login_required
def children(request):
    my_ss_list = StudySession.objects.filter(author=request.user).order_by('start_time')
    return render(request, 'webapp/children.html', {'my_ss_list': my_ss_list})


@login_required
def ss_review(request, ss_id):
    ss = StudySession.objects.get(id=ss_id)
    #if request.user is not ss.author:
    #    return render(request, 'webapp/reviews.html', {'message': "That isn't your review"})
    message = ""
    review_list = Review.objects.filter(study_session=ss)
    if len(review_list) is 0:
        message = "There are no reviews for this study session."
    return render(request, 'webapp/reviews.html', 
        {'review_list': review_list, 'message': message})


@login_required
def search(request):
    if request.method == "POST":
        if request.POST.has_key('rsvp'):
            try:
                ss_id = request.POST['rsvp']
                ss = StudySession.objects.get(id=ss_id)
                current = Attendance.objects.filter(user=request.user, study_session=ss)
                if len(current) > 0:
                    current[0].rsvp_status = 1
                    current[0].save()
                else:
                    Attendance(study_session=ss, user=request.user, rsvp_status=1).save()
                return render(request, 'webapp/search.html',
                             {'message': "You've successfully RSVP'd to the Study Session"})

            except:
                return render(request, 'webapp/search.html',
                             {'message': "Failed to RSVP to the Study Session"})
                
        else:
            try:
                class_prefix_us = request.POST['class_prefix']
                class_number_us = request.POST['class_number']

                class_prefix = re.search(r'[A-Za-z]{4}', class_prefix_us).group(0)
                class_number = re.search(r'[0-9]{3}', class_number_us).group(0)

                safe_data = {"class_number": class_number,
                             "class_prefix": class_prefix }
                ss_list = _search(safe_data)
                print ss_list
                return render(request, 'webapp/search.html',
                             {'search_results': ss_list,
                              'message': ""})
            except:
                return render(request, 'webapp/search.html',
                             {'message': "Failed to find that class"})
                
    return render(request, 'webapp/search.html')


@login_required
def index(request):
    if request.method == "POST":
        if request.POST.has_key('accept'):
            rsvp_status = 1
            att_id = request.POST['accept']
        elif request.POST.has_key('decline'):
            rsvp_status = 2
            att_id = request.POST['decline']

        att = Attendance.objects.get(id=att_id)
        att.rsvp_status = rsvp_status
        att.save()

    return render(request, 'webapp/view_study_session.html', 
        {'attendances': _get_future_attendances(request.user),
         'time': time.strftime("%H:%M"),
         'date': time.strftime("%d/%m/%Y")})


@login_required
def manage_availabilities(request):
    if request.method == "POST":
        if request.POST.has_key("add"):
            try:
                start_time = re.search(r'([0-2]{0,1}[0-9]{1}:[0-5][0-9])',
                                       request.POST['start_time'])
                if start_time:
                    start_time = start_time.group(0)
                else:
                    start_time = re.search(r'([0-2]{0,1}[0-9]{1})',
                                           request.POST['start_time']).group(0)
                    start_time = "{0}:00".format(start_time)

                end_time = re.search(r'([0-2]{0,1}[0-9]{1}:[0-5][0-9])',
                                     request.POST['end_time'])
                if end_time:
                    end_time = end_time.group(0)
                else:
                    end_time = re.search(r'([0-2]{0,1}[0-9]{1})',
                                         request.POST['end_time']).group(0)
                    end_time = "{0}:00".format(end_time)

                daymap =    {   "monday":   0,
                                "tuesday":  1,
                                "wednesday":2,
                                "thursday": 3,
                                "friday":   4,
                                "saturday": 5,
                                "sunday":   6,
                                "mon":      0,
                                "tue":      1,
                                "wed":      2,
                                "thurs":    3,
                                "fri":      4,
                                "sat":      5,
                                "sun":      6,
                            }

                day = daymap[request.POST['day'].lower()]

            except:
                return render(request, 'webapp/manage_availabilities.html', 
                    {"availabilities": availabilities,
                     "error_message": "Data entered is invalid. Remember time is in 24-hr format and in UTC time." })

            data = {"start": start_time,
                    "end": end_time,
                    "day": int(day)}

            success = add_availability(request.user, data)

            availabilities = Availability.objects.filter(user=request.user).order_by('day')
            if not success:
                return render(request, 'webapp/manage_availabilities.html', 
                    {"availabilities": availabilities,
                     "error_message": "Your added availability overlaps with an existing availability"
                    })

            return render(request, 'webapp/manage_availabilities.html', 
                {"availabilities": availabilities})

        if request.POST.has_key("delete"):
            availability_id = int(request.POST['availability_id'])
            delete_availability(request.user, availability_id)

            availabilities = Availability.objects.filter(user=request.user).order_by('day')
            return render(request, 'webapp/manage_availabilities.html', 
                {"availabilities": availabilities})

    availabilities = Availability.objects.filter(user=request.user).order_by('day')
    return render(request, 'webapp/manage_availabilities.html', 
        {"availabilities": availabilities})


@login_required
def manage_classes(request):
    if request.method == "POST":
        if request.POST.has_key("remove"):
            stc = StudentToClass.objects.get(id=str(request.POST['remove']))
            stc.delete()

            stcs = StudentToClass.objects.filter(user=request.user)
            return render(request, 'webapp/manage_classes.html', 
                {"classes": stcs, 
                 "message": "Successfully removed class."})
                
        elif request.POST.has_key("add"):
            class_prefix_us = request.POST['class_prefix']
            class_number_us = request.POST['class_number']

            class_prefix = re.search(r'[A-Za-z]{4}', class_prefix_us).group(0)
            class_number = re.search(r'[0-9]{3}', class_number_us).group(0)

            desired_class = get_or_add_class(class_prefix.upper(), class_number)

            is_ta = False
            if request.POST.has_key('is_ta'):
                is_ta = (request.POST['is_ta'] == 'on')

            new_stc = register_for_class(request.user, desired_class, is_ta)

            stcs = StudentToClass.objects.filter(user=request.user)
            return render(request, 'webapp/manage_classes.html', 
                {"classes": stcs, 
                 "message": "Successfully added you to a class."})

    stcs = StudentToClass.objects.filter(user=request.user)
    return render(request, 'webapp/manage_classes.html', 
        {"classes": stcs})


@login_required
def review_sessions(request):
    if request.method == "POST":
        description_us = request.POST['text']
        rating_us = request.POST['rating']

        att_id = request.POST['attendance_id']
        description = re.search(r'.{,512}', description_us).group(0)
        rating = re.search(r'[0-5]', rating_us).group(0)

        safe_data = {   "attendance_id": int(att_id),
                        "text": description,
                        "rating": rating,
                    }

        perform_create_review(request.user, safe_data)

    user_attendances = Attendance.objects.filter(user=request.user)
    past_attendances = _get_past_attendances(user=request.user)

    return render(request, 'webapp/review_study_session.html',
        {"sessions": user_attendances,
         "past_attendances": past_attendances})


@login_required
def ss_create(request):
    if request.method == "POST":
        class_number_us = request.POST['class_number']  # 420
        class_prefix_us = request.POST['class_prefix']  # COMS
        start_time_us = request.POST['start_time']      # 9:30 or 09:30 or 23:42 not 25:61
        start_day_us = request.POST['start_day']        # 9/31/15
        end_time_us = request.POST['end_time']
        end_day_us = request.POST['end_day']
        description_us = request.POST['description']    # Text up to 255
        location_us = request.POST['location']          # Text up to ???
        latitude_us = request.POST['latitude']
        longitude_us = request.POST['longitude']

        class_prefix = re.search(r'[A-Z]{4}', class_prefix_us).group(0)
        class_number = re.search(r'[0-9]{3}', class_number_us).group(0)
        description  = re.search(r'.{,255}', description_us).group(0)
        location     = re.search(r'.{,255}', location_us).group(0)
        start_day    = re.search(r'(\d{2}/\d{2}/\d{4})', start_day_us).group(0)
        end_day      = re.search(r'(\d{2}/\d{2}/\d{4})', end_day_us).group(0)
        start_time   = re.search(r'^(2[0-3]|[0-1]?[0-1]?[0-9]):([0-5]?[0-9])', start_time_us).group(0)
        end_time     = re.search(r'^(2[0-3]|[0-1]?[0-1]?[0-9]):([0-5]?[0-9])', end_time_us).group(0)

        if latitude_us:
            latitude = float(latitude_us)
        else:
            latitude = None

        if longitude_us:
            longitude = float(longitude_us)
        else:
            longitude = None

        start_tuple = start_day.split("/")
        end_tuple = end_day.split("/")
        start_time_tuple = start_time.split(":")
        end_time_tuple = end_time.split(":")

        start_time = "{year}-{month}-{day}T{hour}:{minute}:00".format(
            year=start_tuple[2], month=start_tuple[0], day=start_tuple[1], 
            hour=start_time_tuple[0], minute=start_time_tuple[1])

        end_time = "{year}-{month}-{day}T{hour}:{minute}:00".format(
            year=end_tuple[2], month=end_tuple[0], day=end_tuple[1], 
            hour=end_time_tuple[0], minute=end_time_tuple[1])

        safe_data = {   "class": { "class_prefix": class_prefix, "class_number": class_number },
                        "description": description,
                        "location": location,
                        "start_time": start_time,
                        "end_time": end_time,
                        "latitude": latitude,
                        "longitude": longitude
                    }

        result = create_session(request, safe_data)

        return render(request, 'webapp/create_study_session.html', 
            {'result': result,
             'message': "Study session created successfully."})
    return render(request, 'webapp/create_study_session.html')


