# -*- coding: utf-8 -*-

# Django imports
from django.http import HttpResponse
from django.http import JsonResponse
from django.shortcuts import render
from django.contrib.auth.decorators import login_required
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone

# Python Social Apps imports
from social.apps.django_app.utils import psa

# TODO
from django.contrib.auth import logout as auth_logout, login
from social.backends.oauth import BaseOAuth1, BaseOAuth2
from social.backends.google import GooglePlusAuth
from social.backends.utils import load_backends
from social.apps.django_app.utils import psa

# Project imports
from api.models import StudySession
from api.models import StudentToClass
from api.models import Class
from api.models import Attendance
from api.models import Availability
from api.models import StudentToClass
from api.models import UserProfile
from api.models import Review
from api.utils import custom_authenticate

# Python imports
from datetime import datetime
from datetime import timedelta
import json


def _get_future_study_sessions(user):
    future_sessions = []
    for stc in StudentToClass.objects.filter(user=user):
        myclass = stc.course
        all_ss = StudySession.objects.filter(parent_class=myclass)
        for ss in all_ss:
            if ss.start_time > timezone.now():
                future_sessions.append(ss)

    return future_sessions


@custom_authenticate
def get_user_availabilities(request, backend):
    availabilities = []
    for availability in Availability.objects.filter(user=request.user):
        availabilities.append(availability.get_json())
    return JsonResponse(availabilities, safe=False)


@custom_authenticate
def set_availabilities(request, backend):
    data = json.loads(request.POST.get('payload'))

    # Delete old attendances
    Availability.objects.filter(user=request.user).delete()

    availability_list = []

    for availability in data:
        new_availability = Availability(day = int(availability['day']),
                                        user = request.user,
                                        start = datetime.strptime(availability['start'], "%H:%M"),
                                        end = datetime.strptime(availability['end'], "%H:%M")
                                        )
        new_availability.save()
        availability_list.append(new_availability.get_json())

    # Now, for every class, find all StudySessions
    # For every StudySession, find future StudySessions

    # For every future StudySession, find out if our user is available
    # If the user is available, make sure there is ONE Attendance for that user-SS
    future_ss = _get_future_study_sessions(request.user)
    for ss in future_ss:
        if _user_is_available(request.user, ss):
            att_list = Attendance.objects.filter(user=request.user, study_session=ss)
            if len(att_list) >= 1:
                for i in range(len(att_list)):
                    if i is not 0:
                        att_list[i].delete()
            else:  # att_list is empty
                Attendance(user=request.user, study_session=ss, rsvp_status=0).save()
    

    return JsonResponse(availability_list, safe=False)


def delete_availability(user, availability_id):
    Availability.objects.get(id=availability_id).delete()


def add_availability(user, data):
    start = datetime.strptime(data['start'], "%H:%M").time()
    end = datetime.strptime(data['end'], "%H:%M").time()
    day = int(data['day'])

    print start, end, day

    current_avails = Availability.objects.filter(user=user)
    for avail in current_avails:
        if day == avail.day:
            if start >= avail.start and start < avail.end:
                return False
            if end > avail.start and end <= avail.end:
                return False

    Availability(day=day, start=start, end=end, user=user).save()

    future_ss = _get_future_study_sessions(user)

    for ss in future_ss:
        if _user_is_available(user, ss):
            att_list = Attendance.objects.filter(user=user, study_session=ss)
            if len(att_list) >= 1:
                for i in range(len(att_list)):
                    if i is not 0:
                        att_list[i].delete()
            else:  # att_list is empty
                Attendance(user=user, study_session=ss, rsvp_status=0).save()

    return True


@custom_authenticate
def get_optimal_times(request, backend):
    data = json.loads(request.POST.get('payload'))
    day = int(data['day'])
    arr = [0 for i in range(24)]
    class_list = Class.objects.filter(prefix=data['class_prefix'], number=int(data['class_number']))

    if len(class_list) == 0 or class_list == None:
        return JsonResponse({"error": "bad class"}, safe=False)

    student_list = []
    for stc in StudentToClass.objects.filter(course=class_list[0]):
        student_list.append(stc.user)

    for student in student_list:
        availability_list = Availability.objects.filter(user=student)
        for availability in availability_list:
            if availability.day is day:
                for hour in range(24):
                    if hour > availability.start.hour and hour < availability.end.hour:
                        arr[hour] = arr[hour] + 1
                    elif hour is availability.start.hour and availability.start.min is 0:
                        arr[hour] = arr[hour] + 1
    
    return JsonResponse(arr, safe=False)


def _user_is_available(user, ss):
    start_day = ss.start_time.weekday()
    availabilities = Availability.objects.filter(user=user, day=start_day)
    for availability in availabilities:
        if availability.start <= ss.start_time.time() and availability.end >= ss.end_time.time():
            return True

    return False


