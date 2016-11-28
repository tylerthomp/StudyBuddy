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


def _get_future_attendances(user):
    future_attendances = []
    for attendance in Attendance.objects.filter(user=user):
        if attendance.study_session.start_time > timezone.now():
            future_attendances.append(attendance)
    return future_attendances


def _get_past_attendances(user, slice_size=5):
    past_attendances = Attendance.objects.filter(user=user)
    past_attendances = past_attendances.filter(start_time__lt=timezone.now()).order_by('start_time')

    slice_size = min(slice_size, len(past_attendances))
    past_attendances = past_attendances[len(past_attendances)-slice_size:]

    return past_attendances


@custom_authenticate
def get_future_attendances(request, backend):
    future_attendances = []
    for attendance in _get_future_attendances(request.user):
        if attendance.rsvp_status != 1:
            future_attendances.append(attendance.get_json())
    return JsonResponse(future_attendances, safe=False)


@custom_authenticate
def get_accepted_attendances(request, backend):
    accepted_attendances = []
    for attendance in _get_future_attendances(request.user):
        if attendance.rsvp_status == 1:
            accepted_attendances.append(attendance.get_json())

    return JsonResponse(accepted_attendances, safe=False)


@custom_authenticate
def get_suggested_attendances(request, backend):
    suggested_attendances = []
    for attendance in _get_future_attendances(request.user):
        if attendance.rsvp_status == 0:
            suggested_attendances.append(attendance.get_json())

    return JsonResponse(suggested_attendances, safe=False)


@custom_authenticate
def get_declined_attendances(request, backend):
    declined_attendances = []
    for attendance in _get_future_attendances(request.user):
        if attendance.rsvp_status == 2:
            declined_attendances.append(attendance.get_json())

    return JsonResponse(declined_attendances, safe=False)


@custom_authenticate
def get_past_attendances(request, backend):
    past_attendances = _get_past_attendances(request.user)
    returnable = []
    for att in past_attendances:
        returnable.append(att.get_json())
    
    return JsonResponse(returnable, safe=False)


@custom_authenticate
def set_attendance_rsvp(request, backend):
    if request.method == "GET":
        return JsonResponse({"That_isn't_a_post_request": "that's a GET request!"})
    
    data = json.loads(request.POST.get('payload'))

    attendance_id = int(data['id'])
    new_rsvp = int(data['rsvp'])

    attendance = Attendance.objects.get(id=attendance_id)
    attendance.rsvp_status = new_rsvp
    attendance.save()

    attendance = Attendance.objects.get(id=attendance_id)
    return JsonResponse(attendance.get_json())


@custom_authenticate
def my_children(request, backend):
    returnable = []
    for session in StudySession.objects.filter(author=request.user, start_time__gt=timezone.now()):
        att_list = Attendance.objects.filter(user=request.user, study_session=session)
        if len(att_list) < 1:
            new_att = Attendance(user=request.user, study_session=session, rsvp_status=0)
            new_att.save()
            returnable.append(new_att.get_json())
        else:
            returnable.append(att_list[0].get_json())

    print returnable
    return JsonResponse(returnable, safe=False)
