__author__ = 'androideka'

from django.conf.urls import patterns, url

from webapp import views

urlpatterns = patterns('',
    url(r'^$', views.index, name='index'),
    url(r'^create/', views.ss_create, name='create_study_session'),
    url(r'^availabilities/', views.manage_availabilities, name='manage_availabilities'),
    url(r'^classes/', views.manage_classes, name='manage_classes'),
    url(r'^review/', views.review_sessions, name='review_sessions'),
    url(r'^search/', views.search, name='search'),
    url(r'^children/', views.children, name='children'),
    url(r'^(?P<ss_id>\d+)/reviews/$', views.ss_review, name='reviews'),
)
