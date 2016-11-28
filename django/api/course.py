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


@custom_authenticate
def get_my_classes(request, backend):
    my_classes = []
    for link in StudentToClass.objects.filter(user=request.user):
        my_classes.append(link.course.get_json())
    return JsonResponse(my_classes, safe=False)


@custom_authenticate
def get_class_prefixes(request, backend):
    classes = []
    for element in Class.class_prefixes:
        classes.append(element[0])
    return JsonResponse(classes, safe=False)

