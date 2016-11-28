from django.contrib import admin
from api.models import StudySession
from api.models import Class
from api.models import Review
from api.models import Attendance
from api.models import Availability
from api.models import StudentToClass
from api.models import UserProfile

# Register your models here.
admin.site.register(StudySession)
admin.site.register(Class)
admin.site.register(Review)
admin.site.register(Attendance)
admin.site.register(Availability)
admin.site.register(StudentToClass)
admin.site.register(UserProfile)
