# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0003_userprofile'),
    ]

    operations = [
        migrations.AddField(
            model_name='studysession',
            name='is_ta',
            field=models.BooleanField(default=False),
            preserve_default=False,
        ),
    ]
