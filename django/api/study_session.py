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


def study_session_dummy(request):
    to_json = {
        "class_name"  : "COMS 420",
        "start_time"  : "2015-01-28T16:00:00",
        "end_time"    : "2015-01-28T17:00:00",
        "description" : "Reviewing for exam 4",
        "location"    : "My crib at 4420 Frederiksen Ct.",
        "latitude"    : "42.033314", 
        "longitude"   : "-93.642519",
        "author"      : "mrose" 
        }
    return JsonResponse(to_json)


@custom_authenticate
def study_session_protected_dummy(request, backend):
    to_json = {
        "class_name"  : "MATH 420",
        "start_time"  : "2015-01-28T16:20:00",
        "end_time"    : "2015-01-28T20:40:00",
        "description" : "Protected dummy description.",
        "location"    : "Protected dummy location",
        "latitude"    : "42.033314", 
        "longitude"   : "-93.642519",
        "author"      : str(request.user) 
        }
    return JsonResponse(to_json)


@login_required
def study_session_view(request):
    return render(request, 'api/view_study_session.html', {'study_sessions': StudySession.objects.all()})


def _search(data):
    myclass = Class.objects.filter(prefix=data['class_prefix'],
                                   number=data['class_number'])
    return StudySession.objects.filter(parent_class=myclass[0], 
                                       start_time__gt=timezone.now())


@custom_authenticate
def search(request, backend):
    data = json.loads(request.POST.get('payload'))
    ss_list = _search(data)
    returnable = []
    for ss in ss_list:
        returnable.append(ss.get_json())
    return JsonResponse(returnable, safe=False)


@custom_authenticate
@csrf_exempt
def create_study_session(request, backend):
    # Two different request types handled
    if request.method == "GET":
        return render(request, "api/create_study_session.html", {'error_message': str(request.user)})

    if request.method == "POST":
        data = json.loads(request.POST.get('payload'))
        
        new_ss = create_session(request, data)

        return JsonResponse(new_ss.get_json())


@custom_authenticate
def get_child_study_sessions(request, backend):
    all_child_sessions = []
    for session in StudySession.objects.filter(author=request.user, start_time__gt=timezone.now()):
        all_child_sessions.append(session.get_json())
    print all_child_sessions
    return JsonResponse(all_child_sessions, safe=False)


@custom_authenticate
def get_past_child_study_sessions(request, backend):
    all_child_sessions = []
    for session in StudySession.objects.filter(author=request.user, start_time__lt=timezone.now()):
        all_child_sessions.append(session.get_json())
    print all_child_sessions
    return JsonResponse(all_child_sessions, safe=False)
    

"""
This takes a django http request and a python dictionary, and will create a study session.
The data in the dictionary must be well formed and sanitized, especially the start_time and end_time fields.
This method will return the StudySession that it created.
The dictionary must have all fields. The following is an example (note the nested dict for class)

{ “class”: { “class_prefix” : COMS, “class_number”: 420},
  “description”: “420 no-scope blaze it”,
  “location”: “Murdertown”,
  “latitude”: “”,
  “longitude”: “”,
  “start_time”: “2015-02-27T16:20:00”,
  “end_time”: “2015-02-27T17:20:00”,
  "is_ta": True,
}

"""
def create_session(request, data):
        # It's OK if we have nothing in these fields, but we must have these fields
        if data.has_key('longitude'):
            if data['longitude'] == u'' or data['longitude'] == u'0.0':
                longitude = None
            else:
                longitude = data['longitude'] 
        else:
            longitude = None

        if data.has_key('latitude'):
            if data['latitude'] == u'' or data['longitude'] == u'0.0':
                latitude = None
            else:
                latitude = data['latitude'] 
        else:
            latitude = None

        # Handle classes. Make a new class if need be
        myclass = Class.objects.filter( prefix=data['class']['class_prefix'], 
                                        number=data['class']['class_number'])
        print "Class filter: " + str(myclass)
        if len(myclass) == 0:
            myclass = Class(prefix=data['class']['class_prefix'],
                            number=int(data['class']['class_number']))
            myclass.save()
        elif len(myclass) > 1:
            raise Exception("That's not possible!")
        else:
            myclass = myclass[0]

        if data.has_key('is_ta'):
            if is_ta == u'':
                is_ta = False
            else:
                is_ta = bool(data['is_ta'])
        else:
            is_ta = False

        print myclass
        # This is where we might want to sanitize input, if we wanted to do so.
        # But we don't. We don't want to.

        # Make our new study session
        new_ss = StudySession(parent_class=myclass,
                                 author     = request.user,
                                 start_time = datetime.strptime(data['start_time'], "%Y-%m-%dT%H:%M:%S" ),
                                 end_time   = datetime.strptime(data['end_time'], "%Y-%m-%dT%H:%M:%S" ),
                                 description= data['description'],
                                 location   = data['location'],
                                 latitude   = latitude,
                                 longitude  = longitude,
                                 is_ta      = is_ta,
                                 )
        new_ss.save()

        # This is where we will have to 
        # * find all of the students in myclass
        # * If they are available, create an attendance
        # * Push a notification to these people

        for link in StudentToClass.objects.filter(course=myclass):
            if _user_is_available(request.user, new_ss):
                att = Attendance(user=link.user, study_session=new_ss, rsvp_status=0)
                att.save()
                # TODO: If available, push notification to user

        att_list = Attendance.objects.filter(user=request.user, study_session=new_ss)
        if len(att_list) < 1:
            Attendance(user=request.user, study_session=new_ss, rsvp_status=0).save()

        return new_ss


@custom_authenticate
def remove(request, backend):
    data = json.loads(request.POST.get('payload'))
    ss_id = data['study_session_id']
    print "\/ ====== REMOVE REQUEST ====== \/"
    print "POST value for study_session_id: " + str(ss_id)
    print "Fetching the StudySession with that ID from the database..."
    ss = StudySession.objects.get(id=ss_id)
    print "This is the StudySession that I got."
    print "The author is " + str(ss.author)
    print "The description is " + str(ss.description)
    print "The requesting party is " + str(request.user)

    print "Is the author the same as the requesting party? a new method " + str(bool(str(ss.author) == str(request.user)))

    if str(ss.author) != str(request.user):
        print "It looks like you are not the author"
        return JsonResponse({"error": "Cannot remove this Study Session because you are not the author"})

    print "The author is you! We should be deleting now..."

    # Find all attendances and delete them
    att_list = Attendance.objects.filter(study_session=ss)
    for att in att_list:
        print "    Deleting attendance: " + str(att.id)
        att.delete()

    # Find all reviews and delete them
    review_list = Review.objects.filter(study_session=ss)
    for review in review_list:
        print "    Deleting review: " + str(review.id)
        review.delete()

    # Delete the study session
    print "Deleting StudySession, ID: " + str(ss.id)
    ss.delete()
    print "/\ ====== REMOVE REQUEST ====== /"

    return JsonResponse({"message": "Success"})


@custom_authenticate
def rsvp(request, backend):
    data = json.loads(request.POST.get('payload'))
    ss_id = data['study_session_id']
    ss = StudySession.objects.get(id=ss_id)
    
    att_list = Attendance.objects.filter(study_session=ss, user=request.user)
    if len(att_list) > 0:
        att_list[0].rsvp_status = 1
        att_list[0].save()
        return JsonResponse({"message": "Found an existing attendance, and set RSVP status"})

    new_att = Attendance(study_session=ss, user=request.user, rsvp_status=1)
    new_att.save()
    new_att = Attendance.objects.filter(study_session=ss, user=request.user)[0]
    return JsonResponse({"new_att": new_att.get_json()})

