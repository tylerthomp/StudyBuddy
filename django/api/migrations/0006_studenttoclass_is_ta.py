# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0005_auto_20150418_2203'),
    ]

    operations = [
        migrations.AddField(
            model_name='studenttoclass',
            name='is_ta',
            field=models.BooleanField(default=True),
            preserve_default=True,
        ),
    ]
