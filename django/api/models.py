from django.db import models
from django.db.models.signals import post_init
from django.contrib.auth.models import User
import json

class Class(models.Model):
    class_prefixes = (
        ("COMS", "COMS"), ("CPRE", "CPRE"), ("MATH", "MATH"),
        ("STAT", "STAT"), ("ENGL", "ENGL"), ("PHIL", "PHIL"),
        ("AMIN", "AMIN"), ("PHYS", "PHYS"), ("CHEM", "CHEM"),
        )

    prefix      = models.CharField(max_length=4, choices=class_prefixes)
    number      = models.IntegerField()

    def __unicode__(self):
        return "{0} {1}".format(self.prefix, self.number)

    def get_json(self):
        data =  { "class_prefix" : self.prefix,
                  "class_number" : self.number }
        return data


class UserProfile(models.Model):
    choices = (
        (0, "Student"),
        (1, "TA"),
        (2, "Business"),
        )

    user            = models.ForeignKey(User)
    agency          = models.IntegerField(choices=choices)

    def __unicode__(self):
        return "{0}: {1}".format(self.user, self.agency)


class StudentToClass(models.Model):
    user            = models.ForeignKey(User)
    course          = models.ForeignKey(Class)    
    is_ta           = models.BooleanField(blank=False, default=False)

    def __unicode__(self):
        return "{0} -> {1}".format(self.user, self.course)

    def get_json(self):
        data =  {
                    "user"       : str(self.user),
                    "course"     : self.course.get_json(),
                    "is_ta"      : self.is_ta,
                }
        return data


class StudySession(models.Model):
    parent_class    = models.ForeignKey(Class)
    author          = models.ForeignKey(User)
    start_time      = models.DateTimeField()
    end_time        = models.DateTimeField()
    description     = models.CharField(max_length=256)
    location        = models.CharField(max_length=256)
    latitude        = models.FloatField(blank=True, null=True)
    longitude       = models.FloatField(blank=True, null=True)
    is_ta           = models.BooleanField(blank=False, default=False)

    def __unicode__(self):
        return "{0} by {1}".format(self.parent_class, self.author)

    def get_json(self):
        data =  { 
                    "parent_class" : self.parent_class.get_json(),
                    "author"       : str(self.author),
                    "start_time"   : self.start_time.isoformat(),
                    "end_time"     : self.end_time.isoformat(),
                    "description"  : self.description,
                    "location"     : self.location,
                    "latitude"     : self.latitude,
                    "longitude"    : self.longitude,
                    "id"           : self.id,
                    "is_ta"        : self.is_ta,
                }
        return data


class Review(models.Model):
    text            = models.CharField(max_length=512)
    rating          = models.IntegerField(choices=( (1,1), (2,2), (3,3), 
                                                    (4,4), (5,5) ))
    author          = models.ForeignKey(User)
    study_session   = models.ForeignKey(StudySession)

    def __unicode__(self):
        return "({0}) - {1}".format(self.rating, self.study_session)

    def get_json(self):
        data =  {
                    "text"          : self.text,
                    "rating"        : self.rating,
                    "author"        : str(self.author),
                    "study_session" : self.study_session.get_json(),
                }
        return data


class Attendance(models.Model):
    choices         = ( (0, "None"),
                        (1, "Accepted"),
                        (2, "Declined") )
    user            = models.ForeignKey(User)
    study_session   = models.ForeignKey(StudySession)
    rsvp_status     = models.IntegerField(choices=(choices))

    start_time      = models.DateTimeField(blank=True, null=True)

    def has_review(self):
        review_list = Review.objects.filter(author=self.user, study_session=self.study_session)
        return bool(len(review_list) > 0)

    def __unicode__(self):
        return "{0} - {1}".format(self.choices[self.rsvp_status][1], self.study_session)

    def get_json(self):
        data =  {
                    "user"          : str(self.user),
                    "study_session" : self.study_session.get_json(),
                    "rsvp_status"   : self.rsvp_status,
                    "id"            : self.id,
                    "has_review"    : self.has_review(),
                }
        return data


class Availability(models.Model):
    choices         = ( (0, "Monday"),
                        (1, "Tuesday"),
                        (2, "Wednesday"),
                        (3, "Thursday"),
                        (4, "Friday"),
                        (5, "Saturday"),
                        (6, "Sunday") )

    day             = models.IntegerField(choices=(choices))
    user            = models.ForeignKey(User)
    start           = models.TimeField()
    end             = models.TimeField()

    def __unicode__(self):
        return "{0} {1} - {2}".format( self.user, self.choices[self.day][1], self.start.isoformat())

    def get_json(self):
        data =  {
                    "day"   : self.day,
                    "user"  : str(self.user),
                    "start" : self.start.__format__("%H:%M"),
                    "end"   : self.end.__format__("%H:%M"),
                }
        return data


def set_attendance_start_time(sender, **kwargs):
    instance = kwargs['instance']
    instance.start_time = instance.study_session.start_time


post_init.connect(set_attendance_start_time, sender=Attendance)
