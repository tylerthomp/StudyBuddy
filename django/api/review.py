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


"""
{   "attendance_id" : 11,
    "description"   : "Text here, must be maximum 512 bytes",
    "rating"        : 3,
}
"""
def perform_create_review(user, data):
    attendance = Attendance.objects.get(id=data['attendance_id'])
    study_session = attendance.study_session

    if len(Review.objects.filter(author=user, study_session=study_session)) > 0:
        for review in Review.objects.filter(author=user, study_session=study_session):
            review.delete()

    Review(author=user, text=data['text'], rating=int(data['rating']), study_session=study_session).save()


@custom_authenticate
def create_review(request, data):
    data = json.loads(request.POST.get('payload'))
    perform_create_review(request.user, data)
    return JsonResponse({"message": "success"})


@custom_authenticate
def get_reviews(request, backend):
    reviews = []
    data = json.loads(request.POST.get('payload'))

    print "====== GET REVIEW ======"
    print "This is the POST value for study_session_id: " + str(data['study_session_id'])
    print "Fetching that study session...."

    study_session = StudySession.objects.get(id=int(data['study_session_id']))
    print "This is the study session I retrieved from the databse: " + str(study_session)
    print "This is the author of that study session: " + str(study_session.author)
    print "This is the ID of that study session: " + str(study_session.id)
    for review in Review.objects.filter(study_session=study_session):
        reviews.append(review.get_json())

    print "I found these reviews for that study session" + str(reviews)
    return JsonResponse(reviews, safe=False)


