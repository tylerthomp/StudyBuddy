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
from api.availability import _user_is_available

# Python imports
from datetime import datetime
from datetime import timedelta
import json


def get_or_add_class(prefix, number):
    myclass = Class.objects.filter(prefix=prefix, number=int(number))

    if len(myclass) < 1:
        myclass = Class(prefix=prefix, number=int(number))
        myclass.save()
    else:
        myclass = myclass[0]

    return myclass


def register_for_class(user, myclass, is_ta=False):
    # If there are multiple StC entries, delete all but the first one.
    # If there are no StC entries, create one.

    existing_list = StudentToClass.objects.filter(user=user, course=myclass, is_ta=is_ta)
    if len(existing_list) >= 1:
        for i in range(len(existing_list)):
            if i is not 0:
                existing_list[i].delete()
        stc = existing_list[0]
    elif len(existing_list) == 0:
        stc = StudentToClass(user=user, course=myclass, is_ta=is_ta)
        stc.save()

    # Now we must find all future Study Sessions for this class, and create an attendance
    # If there are one or more existing attendances for a user and this SS, then
    # delete all of them except for the first one. Otherwise, create one.

    all_ss = StudySession.objects.filter(parent_class=myclass, start_time__gt=timezone.now())
    for ss in all_ss:
        existing_attendances = Attendance.objects.filter(user=user, study_session=ss)
        if len(existing_attendances) is 0 and _user_is_available(request.user, ss):
            Attendance(rsvp_status=0, user=user, study_session=ss).save()
        else:
            for i in range(len(existing_attendances)):
                if i is not 0:
                    existing_attendances[i].delete()
        # Push notification to user?

    return stc


@custom_authenticate
def add_class(request, backend):
    data = json.loads(request.POST.get('payload'))

    prefix = data['class_prefix']
    number = data['class_number']
    myclass = get_or_add_class(prefix, number)

    is_ta = False
    if data.has_key('is_ta'):
        is_ta = bool(data['is_ta'])

    stc = register_for_class(request.user, myclass, is_ta)

    return JsonResponse(stc.get_json())
    

@custom_authenticate
def remove_class(request, backend):
    data = json.loads(request.POST.get('payload'))
    myclass = Class.objects.filter(prefix=data['class_prefix'], number=data['class_number'])

    if len(myclass) < 1:
        return JsonResponse({"error": "Cannot remove a class that doesn't exist."})
    elif len(myclass) > 1:
        return JsonResponse({"error": "Shit is fucked up, there's two matching classes."})
        
    stc_list = StudentToClass.objects.filter(course=myclass[0], user=request.user)
    for stc in stc_list:
        stc.delete()

    return JsonResponse({"msg": "Successfully deleted the StudentToClass entries"})


