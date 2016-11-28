# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
from django.conf import settings


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='Attendance',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('rsvp_status', models.IntegerField(choices=[(0, b'None'), (1, b'Accepted'), (2, b'Declined')])),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Availability',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('day', models.IntegerField(choices=[(0, b'Monday'), (1, b'Tuesday'), (2, b'Wednesday'), (3, b'Thursday'), (4, b'Friday'), (5, b'Saturday'), (6, b'Sunday')])),
                ('start', models.TimeField()),
                ('end', models.TimeField()),
                ('user', models.ForeignKey(to=settings.AUTH_USER_MODEL)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Class',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('prefix', models.CharField(max_length=4, choices=[(b'COMS', b'COMS'), (b'CPRE', b'CPRE'), (b'MATH', b'MATH'), (b'STAT', b'STAT'), (b'ENGL', b'ENGL'), (b'PHIL', b'PHIL'), (b'AMIN', b'AMIN'), (b'PHYS', b'PHYS'), (b'CHEM', b'CHEM')])),
                ('number', models.IntegerField()),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Review',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('text', models.CharField(max_length=512)),
                ('rating', models.IntegerField(choices=[(1, 1), (2, 2), (3, 3), (4, 4), (5, 5)])),
                ('author', models.ForeignKey(to=settings.AUTH_USER_MODEL)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='StudySession',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('description', models.CharField(max_length=256)),
                ('location', models.CharField(max_length=256)),
                ('latitude', models.FloatField(null=True, blank=True)),
                ('longitude', models.FloatField(null=True, blank=True)),
                ('author', models.ForeignKey(to=settings.AUTH_USER_MODEL)),
                ('parent_class', models.ForeignKey(to='api.Class')),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.AddField(
            model_name='review',
            name='study_session',
            field=models.ForeignKey(to='api.StudySession'),
            preserve_default=True,
        ),
        migrations.AddField(
            model_name='attendance',
            name='study_session',
            field=models.ForeignKey(to='api.StudySession'),
            preserve_default=True,
        ),
        migrations.AddField(
            model_name='attendance',
            name='user',
            field=models.ForeignKey(to=settings.AUTH_USER_MODEL),
            preserve_default=True,
        ),
    ]
