from django.conf.urls import patterns, include, url
from django.contrib import admin

admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'studybuddy.views.home', name='home'),
    # url(r'^blog/', include('blog.urls')),

    url(r'^admin/', include(admin.site.urls)),
    url(r'^api/', include('api.urls')),
    #url(r'^complete/(?P<backend>[^/]+)/$', 'api.views.oauth2_callback', name='oauth2_callback'),
    url('', include('social.apps.django_app.urls', namespace='social')),
    url(r'^webapp/', include('webapp.urls')),
)
