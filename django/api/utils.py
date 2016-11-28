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

# Python imports
from datetime import datetime
from datetime import timedelta
import json


def custom_authenticate(function):
    @csrf_exempt
    @psa('social:complete')
    def inner(request, backend):
        access_token = request.POST.get('access_token')
        if access_token == "" or access_token == None:
            return JsonResponse({"error": "There was no access_token in POSTdata"})

        # If we fail below, I want to know why.
        print ""
        print ""
        print "===== POST DATA ====="
        print "===== POST DATA ====="
        print "===== POST DATA ====="
        print "===== POST DATA ====="
        print ""
        print ""
        print str(request.POST)
        print ""
        print ""
        print "===== POST DATA ====="
        print "===== POST DATA ====="
        print "===== POST DATA ====="
        print "===== POST DATA ====="
        user = request.backend.do_auth(access_token, ajax=True)

        login(request, user)
        return function(request, backend)
    return inner


# Yes, this works.
@psa('social:complete')
def oauth2_redirect(request, backend):
    return render(request, "api/create_study_session.html")


# Yes, this works.
@psa('social:complete')
def oauth2_callback(request, backend):
    if len(UserProfile.objects.filter(user=request.user)) < 1:
        UserProfile(user=request.user, agency=0).save()
    print str(backend)
    print dir(backend)
    return JsonResponse({"logged_in" : str(bool(request.user.is_authenticated())),
                         "username"  : str(request.user) })

                         
# TODO: Special case handling. What if the token isn't valid? No token in GET? login doesn't work?
@psa('social:complete')
def register_manually(request, backend):
    access_token = request.GET.get('access_token')
    user = request.backend.do_auth(access_token, ajax=True)
    login(request, user)

    if len(UserProfile.objects.filter(user=request.user)) < 1:
        UserProfile(user=request.user, agency=0).save()

    return JsonResponse({"logged_in" : str(bool(request.user.is_authenticated())),
                         "username"  : str(request.user) })

