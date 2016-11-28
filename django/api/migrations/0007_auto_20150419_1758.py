# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0006_studenttoclass_is_ta'),
    ]

    operations = [
        migrations.AddField(
            model_name='attendance',
            name='start_time',
            field=models.DateTimeField(null=True, blank=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='studenttoclass',
            name='is_ta',
            field=models.BooleanField(default=False),
            preserve_default=True,
        ),
    ]
