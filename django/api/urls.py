from django.conf.urls import patterns, include, url
from api import attendance
from api import availability
from api import course
from api import review
from api import student_to_class
from api import study_session
from api import utils

urlpatterns = patterns('',
    url(r'^Attendance/future$',                     attendance.get_future_attendances,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Attendance/past$',                       attendance.get_past_attendances,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Attendance/accepted$',                   attendance.get_accepted_attendances,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Attendance/declined$',                   attendance.get_accepted_attendances,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Attendance/no_rsvp$',                    attendance.get_suggested_attendances,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Attendance/rsvp$',                       attendance.set_attendance_rsvp,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Attendance/my_children$',                attendance.my_children,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),
                                                    
    url(r'^Availability/current$',                  availability.get_user_availabilities,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Availability/set$',                      availability.set_availabilities,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Availability/optimal_times$',            availability.get_optimal_times,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Class/prefixes$',                        course.get_class_prefixes,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Class/my_classes$',                      course.get_my_classes,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^StudentToClass/add_class$',              student_to_class.add_class,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^StudentToClass/remove_class$',           student_to_class.remove_class,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^StudySession/dummy$',                    study_session.study_session_dummy),

    url(r'^StudySession/protected_dummy$',          study_session.study_session_protected_dummy, 
                                                    kwargs={'backend': 'google-oauth2'},
                                                    name='study_session_protected_dummy'),

    url(r'^StudySession/view$',                     study_session.study_session_view,
                                                    name='study_session_view'),

    url(r'^StudySession/create$',                   study_session.create_study_session, 
                                                    kwargs={'backend': 'google-oauth2'}, 
                                                    name='create_study_session'),

    url(r'^StudySession/search$',                   study_session.search, 
                                                    kwargs={'backend': 'google-oauth2'}
                                                    ),

    url(r'^StudySession/rsvp$',                     study_session.rsvp, 
                                                    kwargs={'backend': 'google-oauth2'}
                                                    ),

    url(r'^StudySession/my_children$',              study_session.get_child_study_sessions,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^StudySession/past_children$',            study_session.get_past_child_study_sessions,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^StudySession/remove$',                   study_session.remove,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Review/create$',                         review.create_review,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^Review/get$',                            review.get_reviews,
                                                    kwargs={'backend': 'google-oauth2'},
                                                    ),

    url(r'^oauth2/callback/(?P<backend>[^/]+)/$',   utils.oauth2_callback,      name='oauth2_callback'),
    url(r'^manual_login/(?P<backend>[^/]+)/$',      utils.register_manually,    name='oauth2_manual'),
)
